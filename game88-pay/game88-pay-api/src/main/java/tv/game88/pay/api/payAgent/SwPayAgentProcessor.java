package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.SW_PAY + "PayAgentProcessor" )
@Log4j2
public class SwPayAgentProcessor extends AbstractPayAgent {

    @Override
    public String getName() {
        return "SW代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "Timestamp", System.currentTimeMillis() / 1000 );
        bodyMap.put( "AccessKey", payAgentChannel.getMerId() );
        bodyMap.put( "PayChannelId", "12" );
        bodyMap.put( "Payee", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "PayeeNo", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "PayeeAddress", withdrawDetail.getBankAddress() );
        bodyMap.put( "OrderNo", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "Amount", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );
        bodyMap.put( "CallbackUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        String signStr = this.assemblyUrl( bodyMap ) + "&SecretKey=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );

        bodyMap.put( "Sign", DigestUtils.md5Hex( signStr ) );

        log.warn( payAgentPlatform.getName() + "下单请求参数{}", JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageJson( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName() + "下单结果 - result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "Code", "" ).toString() ) ) {
                log.info( "代付订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "Message", "" ).toString() );
                // 回滚订单
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName() + "订单提交失败 - result:{}", JsonUtil.object2Json( resultMap ) );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return "fail";
        }

        String orderNo    = requestMap.getOrDefault( "OrderNo", "" ).toString();
        String signTmp    = requestMap.remove( "Sign" ).toString();
        int    orderState = Integer.parseInt( requestMap.getOrDefault( "Status", -1 ).toString() );

        // 解密后对签名验证

        SortedMap<String, Object> signMap = new TreeMap<>( requestMap );
        if ( ObjectUtils.isEmpty( signMap.get( "Ext" ) ) ) {
            signMap.remove( "Ext" );
        }

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( orderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String signStr = this.assemblyUrl( signMap ) + "&SecretKey=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        log.info( signStr );
        String sign = DigestUtils.md5Hex( signStr ).toLowerCase();

        log.info( payAgentPlatform.getName() + "代付回调签名:" + sign + "_" + signTmp );
        if ( sign.equalsIgnoreCase( signTmp ) ) {
            MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( orderNo );
            if ( withdrawDetail == null ) {
                log.error( "提现相关记录丢失 - OrderNo:{}", orderNo );
                return "fail";
            }
            if ( withdrawDetail.getStatus() == 2 ) {
                log.error( "订单已拒绝，无需回调 - OrderNo:{}", orderNo );
                return "ok";
            }
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - OrderNo:{}", orderNo );
                return "ok";
            }
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderNo, payAgentChannel, orderState == 4 );

            log.info( payAgentPlatform.getName() + "订单号:{},回调状态:{},", orderNo, orderState == 4 ? "成功" : "失败" );
            return "ok";
        }
        log.error( payAgentPlatform.getName() + "回调验签失败" );
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
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "Timestamp", System.currentTimeMillis() / 1000 );
        bodyMap.put( "AccessKey", payAgentChannel.getMerId() );
        bodyMap.put( "OrderNo", withdrawDetail.getWithdrawOrderNo() );

        // 生成签名信息
        String signStr = this.assemblyUrl( bodyMap ) + "&SecretKey=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String sign    = DigestUtils.md5Hex( signStr ).toLowerCase();
        bodyMap.put( "Sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageJson( bodyMap ), null );

        log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "Code", "" ).toString();
            if ( "0".equals( code ) ) {
                Map<String, Object> resultDataMap = ( Map<String, Object> ) resultMap.getOrDefault( "Data", new HashMap<>() );
                int                 orderStatus   = Integer.parseInt( resultDataMap.getOrDefault( "Status", 0 ).toString() );

                // status 4代付中5代付失败6代付成功
                // orderState (0=处理中，1=成功，2=失败)

                int status = switch ( orderStatus ) {
                    case 4 -> 6;
                    case 16 -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
                return resultMap.getOrDefault( "Message", "" ).toString();
            }
        }
        log.warn( "代付订单查询失败 - result:{}", JsonUtil.object2Json( resultMap ) );
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
