package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
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

@Repository( value = ConstantsPayAgent.BO_BI_PAY + "PayAgentProcessor" )
@Log4j2
public class BoBiPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "波币代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();

        bodyMap.put( "currency_id", "1" );
        bodyMap.put( "money", withdrawDetail.getWithdrawMoney().setScale( 0, RoundingMode.HALF_UP ) );
        bodyMap.put( "callback_url", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "cp_order_id", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "mch_id", payAgentChannel.getMerId() );
        bodyMap.put( "user_adress", withdrawDetail.getBankAccount().trim() );

        String tempStr = this.assemblyUrl( bodyMap ) + "&pri_key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );

        log.warn( tempStr );

        bodyMap.put( "sign", DigestUtils.md5Hex( tempStr ).toLowerCase() );

        log.warn( JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName() + "下单结果- result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "0".equals( code ) ) {
                log.info( payAgentPlatform.getName() + "代付订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        String sign = requestMap.remove( "sign" ).toString();

        String withdrawOrderId = requestMap.getOrDefault( "cp_order_id", "" ).toString();
        String status          = requestMap.getOrDefault( "status", "" ).toString();

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( withdrawOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String tempStr = this.assemblyUrl( bodyMap ) + "&pri_key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = DigestUtils.md5Hex( tempStr );

        log.info( payAgentPlatform.getName() + "代付回调签名:" + sign + "_" + signStr );
        if ( sign.equalsIgnoreCase( signStr ) ) {
            MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( withdrawOrderId );
            if ( withdrawDetail == null ) {
                log.error( "提现相关记录丢失 - merOrderNo:{}", withdrawOrderId );
                return "FAIL";
            }
            if ( withdrawDetail.getStatus() == 1 ) {
                log.error( "已有代付记录 - merOrderNo:{}", withdrawOrderId );
                return "SUCCESS";
            }
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, "", payAgentChannel, "1".equals( status ) );
            return "ok";
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
        dataMap.put( "mch_id", payAgentChannel.getMerId() );
        dataMap.put( "cp_order_id", withdrawDetail.getWithdrawOrderNo() );

        String tempStr = this.assemblyUrl( dataMap ) + "&pri_key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr ).toLowerCase();
        dataMap.put( "sign", sign );

        Map<String, Object> resultMap = null;
        resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( dataMap ), null );
        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {

                int orderStatus = Integer.parseInt( resultMap.getOrDefault( "status", 0 ).toString() );

                // status 4代付中5代付失败6代付成功
                // orderState (0=处理中，1=成功，2=失败)
                int status = switch ( orderStatus ) {
                    case 1 -> 6;
                    case 2 -> 5;
                    default -> 4;
                };

                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
                return resultMap.getOrDefault( "msg", "" ).toString();
            }
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
