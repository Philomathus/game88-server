package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.NO_COIN_PAY + "AgentProcessor" )
@Log4j2
public class NoCoinPayAgentProcessor extends AbstractPayAgent {

    @Override
    public String getName() {
        return "No数字货币代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        if ( withdrawDetail.getBankId() != 139L ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "未知银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "未知银行类型：" + withdrawDetail.getBankId() );
        }
        Map<String, Object> dataMap                  = new TreeMap<>();
        BigDecimal          usdtWithdrawExchangeRate = configEnvCacheUtil.getConfBd( "usdt_withdraw_exchange_rate" );
        dataMap.put( "appId", payAgentChannel.getMerId() );
        dataMap.put( "merchantOrderNo", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "merchantMemberNo", profile + "_" + withdrawDetail.getWithdrawId() );
        dataMap.put( "amount", withdrawDetail.getWithdrawMoney().divide( usdtWithdrawExchangeRate, 6, RoundingMode.HALF_DOWN ) );
        dataMap.put( "rate", usdtWithdrawExchangeRate.stripTrailingZeros().toPlainString() );
        dataMap.put( "language", "zh" );
        dataMap.put( "coin", "USDT" );
        dataMap.put( "protocol", "TRC20" );
        dataMap.put( "rateType", 1 );
        dataMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        dataMap.put( "toAddress", withdrawDetail.getBankAccount() );
        dataMap.put( "timestamp", System.currentTimeMillis() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = this.assemblyUrl( dataMap ) + "&key=" + signMd5;
        log.warn( signStr );
        dataMap.put( "sign", DigestUtils.sha256Hex( signStr ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set( "version", "V1" );
        httpHeaders.set( "appId", payAgentChannel.getMerId() );
        httpHeaders.set( "language", "zh_CN" );
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( dataMap, httpHeaders );

        log.warn( payAgentPlatform.getName() + "下单请求参数{}", JsonUtil.object2Json( dataMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), httpEntity, reqPayAgent );

        log.info( payAgentPlatform.getName() + "下单结果 - result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "0".equals( code ) ) {
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
        String               merchantOrderNo = requestMap.getOrDefault( "merchantOrderNo", "" ).toString();
        MemberWithdrawDetail withdrawDetail  = withdrawDetailMapper.selectById( merchantOrderNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", merchantOrderNo );
            return "fail";
        }

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( merchantOrderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String state = requestMap.getOrDefault( "state", "" ).toString();
        String sign  = requestMap.remove( "sign" ).toString();

        Map<String, Object> treeMap = new TreeMap<>( requestMap );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = this.assemblyUrl( treeMap ) + "&key=" + signMd5;
        String tempStr = DigestUtils.sha256Hex( signStr );

        log.info( payAgentPlatform.getName() + "回调签名字符串:" + sign + "_" + tempStr );
        if ( tempStr.equalsIgnoreCase( sign ) ) {
            if ( withdrawDetail.getStatus() == 2 ) {
                log.error( "订单已拒绝，无需回调 - merOrderNo:{}", merchantOrderNo );
                return "SUCCESS";
            }
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - merOrderNo:{}", merchantOrderNo );
                return "SUCCESS";
            }
            boolean isSuccess = "3".equals( state );
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, requestMap.getOrDefault( "order_no", "" )
                    .toString(), payAgentChannel, isSuccess );
            log.info( payAgentPlatform.getName() + "订单号:{},回调状态:{},", merchantOrderNo, isSuccess ? "成功" : "失败" );
            return "SUCCESS";
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
        Map<String, Object> params = new TreeMap<>();
        params.put( "appId", payAgentChannel.getMerId() );
        params.put( "merchantOrderNo", withdrawDetail.getWithdrawOrderNo() );
        params.put( "merchantMemberNo", profile + "_" + withdrawDetail.getWithdrawId() );
        params.put( "timestamp", System.currentTimeMillis() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = this.assemblyUrl( params ) + "&key=" + signMd5;
        log.warn( signStr );
        params.put( "sign", DigestUtils.sha256Hex( signStr ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set( "version", "V1" );
        httpHeaders.set( "appId", payAgentChannel.getMerId() );
        httpHeaders.set( "language", "zh_CN" );
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), httpEntity, null );

        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              code    = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "0".equals( code ) && !CollectionUtils.isEmpty( dataMap ) ) {
                String state = dataMap.getOrDefault( "state", "" ).toString();

                //  4代付中 5代付失败 6代付成功
                int status = switch ( state ) {
                    case "3" -> 6;
                    case "4", "5", "6", "7" -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "msg", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}

