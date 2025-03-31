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

@Repository( value = ConstantsPayAgent.GONG_YING_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class GongYingPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "共赢代付";
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
        bodyMap.put( "out_trade_no", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "money", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );
        bodyMap.put( "bankname", configBank.getBankName() );
        bodyMap.put( "subbranch", withdrawDetail.getBankAddress() );
        bodyMap.put( "accountname", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "cardnumber", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "notifyurl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        bodyMap.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );
        bodyMap.put( "cardtype", "0" );

        log.warn( JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = resultMap.getOrDefault( "status", "" ).toString();
            if ( "success".equals( status ) ) {
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
        requestMap.remove( "success_time" );
        String rspSign         = requestMap.remove( "sign" ).toString();
        String refCode         = requestMap.getOrDefault( "refCode", "" ).toString();
        String withdrawOrderId = requestMap.getOrDefault( "out_trade_no", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( withdrawOrderId );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", withdrawOrderId );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 6 ) {
            log.error( "已有代付记录 - merOrderNo:{}", withdrawOrderId );
            return "OK";
        }

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( withdrawOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        Map<String, Object> dataMap = new TreeMap<>( requestMap );

        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr );

        log.info( payAgentPlatform.getName() + "回调签名:" + rspSign + "_" + sign );
        if ( rspSign.equalsIgnoreCase( sign ) ) {
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, requestMap.getOrDefault( "transaction_id", "" )
                    .toString(), payAgentChannel, "3".equals( refCode ) );
            return "OK";
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
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "out_trade_no", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "mchid", payAgentChannel.getMerId() );

        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        dataMap.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        log.warn( JsonUtil.object2Json( dataMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( dataMap ), null );

        log.info( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String success = resultMap.getOrDefault( "status", "" ).toString();
            int    refCode = Integer.parseInt( resultMap.getOrDefault( "refCode", "-1" ).toString() );
            if ( "success".equals( success ) ) {
                // status 4代付中 5代付失败 6代付成功
                // refCode 1成功 2失败 3处理中 4待处理
                int status = switch ( refCode ) {
                    case 3 -> 6;
                    case 4, 5 -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "msg", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
