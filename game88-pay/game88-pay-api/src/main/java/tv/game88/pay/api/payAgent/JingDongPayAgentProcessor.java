package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.JING_DONG_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class JingDongPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "京东代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "pay_memberid", payAgentChannel.getMerId() );
        dataMap.put( "pay_order_id", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "pay_bank_no", withdrawDetail.getBankAccount().trim() );
        dataMap.put( "pay_user_name", withdrawDetail.getBankUserName().trim() );
        for ( RspConfigBankList rspConfigBank : configBankListCache.getEffectList() ) {
            if ( Objects.equals( rspConfigBank.getId(), withdrawDetail.getBankId() ) ) {
                dataMap.put( "pay_bank_name", rspConfigBank.getBankName() );
            }
        }
        dataMap.put( "pay_money", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_DOWN ) );
        dataMap.put( "pay_notify_url", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr );
        dataMap.put( "pay_sign", sign );

        log.warn( payAgentPlatform.getName() + "下单请求参数 - {}", JsonUtil.object2Json( dataMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( dataMap ),
                reqPayAgent );

        log.info( payAgentPlatform.getName() + "下单结果 - result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                log.info( payAgentPlatform.getName() + "订单提交成功 - listResult:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
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

        String rspSign = requestMap.remove( "pay_sign" ).toString();
        String orderNo = requestMap.getOrDefault( "pay_order_id", "" ).toString();
        String orderSn = requestMap.getOrDefault( "pay_order_sn", "" ).toString();
        String status  = requestMap.getOrDefault( "pay_status", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( orderNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", orderNo );
            return "fail";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( orderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr );

        log.info( payAgentPlatform.getName() + "回调签名:" + rspSign + "_" + sign );
        if ( rspSign.equalsIgnoreCase( sign ) ) {
            if ( withdrawDetail.getStatus() == 2 ) {
                log.error( "订单已拒绝，无需回调 - merOrderNo:{}", orderNo );
                return "OK";
            }
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - merOrderNo:{}", orderNo );
                return "OK";
            }
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderSn, payAgentChannel, "SUCCESS".equals( status ) );
            log.info( payAgentPlatform.getName()
                    + "订单号:{},回调状态:{},", orderNo, "SUCCESS".equals( status ) ? "成功" : "失败" );
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
        dataMap.put( "pay_memberid", payAgentChannel.getMerId() );
        dataMap.put( "pay_order_id", withdrawDetail.getWithdrawOrderNo() );

        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr );
        dataMap.put( "pay_sign", sign );

        log.warn( payAgentPlatform.getName() + "查询代付状态接口请求参数{}", JsonUtil.object2Json( dataMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( dataMap ), null );

        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            //  status 4代付中 5代付失败 6代付成功
            //  statusCode 2表示已代付 3表示回退请求 其余皆是处理中
            int statusCode = Integer.parseInt( resultMap.getOrDefault( "status", "0" ).toString() );

            int status = switch ( statusCode ) {
                case 2 -> 6;
                case 3 -> 5;
                default -> 4;
            };
            payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            return resultMap.getOrDefault( "message", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }

}
