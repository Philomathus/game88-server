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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.SANJIN2_PAY + "PayAgentProcessor" )
@Log4j2
public class SanJin2PayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "三斤代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "merchantNum", payAgentChannel.getMerId() );
        dataMap.put( "orderNo", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "amount", withdrawDetail.getWithdrawMoney().stripTrailingZeros().toPlainString() );
        dataMap.put( "cardNumber", withdrawDetail.getBankAccount().trim() );
        dataMap.put( "payType", AESCoder.decrypt( payAgentChannel.getHeaderValue() ) );
        dataMap.put( "account", withdrawDetail.getBankUserName() );
        dataMap.put( "bankName", withdrawDetail.getBankUserName() );
        dataMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String tempStr = payAgentChannel.getMerId() + withdrawDetail.getWithdrawOrderNo() + dataMap.get( "amount" ) + signMd5;
        dataMap.put( "sign", DigestUtils.md5Hex( tempStr ) );

        log.warn( payAgentPlatform.getName() + "下单请求参数{}", JsonUtil.object2Json( dataMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( dataMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                log.info( payAgentPlatform.getName() + "订单提交成功 - listResult:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
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

        String orderNo    = requestMap.getOrDefault( "orderNo", "" ).toString();
        String signTmp    = requestMap.remove( "sign" ).toString();
        int    orderState = Integer.parseInt( requestMap.getOrDefault( "state", -1 ).toString() );

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( orderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        // 解密后对签名验证

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr =
                String.valueOf( requestMap.get( "state" ) ) + requestMap.get( "merchantNum" ) + requestMap.get( "orderNo" )
                        + requestMap.get( "amount" ) + signMd5;
        log.info( signStr );
        String sign = DigestUtils.md5Hex( signStr );

        log.info( payAgentPlatform.getName() + "代付回调签名:" + sign + "_" + signTmp );
        if ( sign.equalsIgnoreCase( signTmp ) ) {
            MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( orderNo );
            if ( withdrawDetail == null ) {
                log.error( "提现相关记录丢失 - OrderNo:{}", orderNo );
                return "fail";
            }
            if ( withdrawDetail.getStatus() == 2 ) {
                log.error( "订单已拒绝，无需回调 - OrderNo:{}", orderNo );
                return "success";
            }
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - OrderNo:{}", orderNo );
                return "success";
            }
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderNo, payAgentChannel, orderState == 1 );
            return "success";
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
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "merchantNum", payAgentChannel.getMerId() );
        dataMap.put( "orderNo", withdrawDetail.getWithdrawOrderNo() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String tempStr = payAgentChannel.getMerId() + withdrawDetail.getWithdrawOrderNo() + signMd5;
        dataMap.put( "sign", DigestUtils.md5Hex( tempStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( dataMap ), null );

        log.info( payAgentPlatform.getName()
                + "查询结果 - 订单号:{} - result:{}", withdrawDetail.getWithdrawOrderNo(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( !"200".equals( code ) ) {
                return resultMap.getOrDefault( "msg", "" ).toString();
            }

            Map<String, Object> resultDataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            int                 orderStatus   = Integer.parseInt( resultDataMap.getOrDefault( "state", 0 ).toString() );

            // status 4代付中5代付失败6代付成功
            // 1待处理 2处理中 3已完成 4出款失败 5驳回待处理（不代表失败）

            int status = 4;
            switch ( orderStatus ) {
            case 3:
                status = 6;
                break;
            case 4:
                status = 5;
                break;
            default:
                break;
            }
            payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            return resultMap.getOrDefault( "msg", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }

}
