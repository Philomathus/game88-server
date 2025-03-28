package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RSACoder;
import tv.game88.core.config.entity.ConfigBankList;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.math.RoundingMode;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.YIAN2_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class YiAn2PayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "易安2代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        ConfigBankList configBank = configBankListCache.getConfigBank( withdrawDetail.getBankId() );
        if ( configBank == null ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "未知银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "未知银行类型：" + withdrawDetail.getBankId() );
        }
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "mchid", payAgentChannel.getMerId() );
        bodyMap.put( "send", "1" );
        bodyMap.put( "channel", "801" );
        bodyMap.put( "tradeid", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "type", "cny" );
        bodyMap.put( "accountname", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "cardnumber", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "bankname", configBank.getBankName() );
        bodyMap.put( "subbranch", "123" );
        bodyMap.put( "money", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ).toString() );
        bodyMap.put( "paydate", System.currentTimeMillis() / 1000 );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        bodyMap.put( "sign", DigestUtils.md5Hex( tempStr ) );
        bodyMap.put( "notifyurl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        log.warn( JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "1".equals( code ) ) {
                log.info( payAgentPlatform.getName() + "订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName() + "订单提交失败 - orderNo:{}", withdrawDetail.getWithdrawOrderNo() );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        String sign      = requestMap.remove( "sign" ).toString();
        String status    = requestMap.getOrDefault( "status", "" ).toString();
        String shOrderId = ( String ) requestMap.get( "tradeid" );

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( shOrderId );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", shOrderId );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 6 ) {
            log.error( "已有代付记录 - merOrderNo:{}", shOrderId );
            return "success";
        }

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( shOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = DigestUtils.md5Hex( tempStr );

        log.info( payAgentPlatform.getName() + "回调签名:" + sign + "_" + signStr );
        if ( sign.equalsIgnoreCase( signStr ) ) {
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, ( String ) requestMap.get( "orderid" ),
                    payAgentChannel, "88".equals( status ) );
            return "success";
        }
        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) throws Exception {
        return null;
    }

    @Override
    public String queryOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                                 PayAgentPlatform payAgentPlatform ) throws Exception {
        Map<String, Object> paramsMap = new TreeMap<>();
        paramsMap.put( "tradeid", withdrawDetail.getWithdrawOrderNo() );
        paramsMap.put( "mchid", payAgentChannel.getMerId() );
        paramsMap.put( "paydate", System.currentTimeMillis() / 1000 );

        String tempStr = this.assemblyUrl( paramsMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        paramsMap.put( "sign", DigestUtils.md5Hex( tempStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( paramsMap ), null );

        log.info( payAgentPlatform.getName() + "查询结果- result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int code = Integer.parseInt( resultMap.getOrDefault( "code", 0 ).toString() );
            if ( code == 1 ) {
                Map<String, Object> dataMap    = ( Map<String, Object> ) resultMap.get( "data" );
                int                 statusType = Integer.parseInt( dataMap.getOrDefault( "status", 0 ).toString() );
                int status = switch ( statusType ) {
                    case 88 -> 6;
                    case 22 -> 5;
                    default -> 4;
                };

                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "msg", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
