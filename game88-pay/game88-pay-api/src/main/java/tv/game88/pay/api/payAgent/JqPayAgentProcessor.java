package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.RoundingMode;
import java.util.*;

@Repository ( value = ConstantsPayAgent.JQ_PAY + "PayAgentProcessor" )
@Log4j2
public class JqPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "JQ代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merchId", payAgentChannel.getMerId() );
        bodyMap.put( "outTradeNo", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "amount", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );
        List<RspConfigBankList> effectList = configBankListCache.getEffectList();
        for ( RspConfigBankList rspConfigBank : effectList ) {
            if ( Objects.equals( rspConfigBank.getId(), withdrawDetail.getBankId() ) ) {
                bodyMap.put( "accBankName", rspConfigBank.getBankName() );
            }
        }
        bodyMap.put( "accName", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "accNo", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + signMd5;
        log.warn( tempStr );
        String sign = DigestUtils.md5Hex( tempStr ).toUpperCase();
        bodyMap.put( "sign", sign );

        log.warn( JsonUtil.object2Json( bodyMap ) );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( bodyMap, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( payAgentPlatform.getOrderUrl(), HttpMethod.POST,
                    restTemplate.httpEntityCallback( httpEntity ), response -> {
                        InputStream bodyStream = response.getBody();
                        String      text;
                        try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                            text = IOUtils.toString( reader );
                        }
                        return JsonUtil.json2Map( text );
                    } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            reqPayAgent.setFailReason( payAgentPlatform.getName() + "下单报错原因:" + e );

            if ( e.getMessage().contains( "failed to respond" ) ) {
                reqPayAgent.setFailReason( "三方网络异常:" + e.getMessage() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
                return false;
            }
        }
        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "success".equals( resultMap.getOrDefault( "message", "" ).toString() )
                    && resultMap.get( "data" ) != null ) {
                log.info( payAgentPlatform.getName() + "订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName() + "订单提交失败 - orderNo:{}", withdrawDetail.getWithdrawOrderNo() );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return "fail";
        }

        String signRes         = requestMap.remove( "sign" ).toString();
        String withdrawOrderId = requestMap.getOrDefault( "outTradeNo", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( withdrawOrderId );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", withdrawOrderId );
            return "fail";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( withdrawOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        Map<String, Object> dataMap = new TreeMap<>( requestMap );
        String              signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + signMd5;
        String sign    = DigestUtils.md5Hex( tempStr );
        if ( signRes.equalsIgnoreCase( sign ) ) {
            if ( withdrawDetail.getStatus() == 0 ) {
                log.error( "已有代付记录 - merOrderNo:{}", withdrawOrderId );
                return "success";
            }

            String status = dataMap.getOrDefault( "status", "" ).toString();
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, requestMap.getOrDefault( "order_no", "" )
                    .toString(), payAgentChannel, "OK_ORDER".equals( status ) );

            log.info( payAgentPlatform.getName()
                    + "订单号:{},回调状态:{},", withdrawOrderId, "OK_ORDER".equals( status ) ? "成功" : "失败" );
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
    public String queryOrderPay( PayAgentLog payAgentLog ) throws Exception {
        MemberWithdrawDetail withdrawDetail   = withdrawDetailMapper.selectById( payAgentLog.getWithdrawOrderNo() );
        PayAgentChannel      payAgentChannel  = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );
        PayAgentPlatform     payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );

        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "outTradeNo", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "merchId", payAgentChannel.getMerId() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + signMd5;
        dataMap.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        log.warn( JsonUtil.object2Json( dataMap ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( dataMap, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( payAgentPlatform.getOrderQueryUrl(), HttpMethod.POST,
                    restTemplate.httpEntityCallback( httpEntity ), response -> {
                        InputStream bodyStream = response.getBody();
                        String      text;
                        try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                            text = IOUtils.toString( reader );
                        }
                        return JsonUtil.json2Map( text );
                    } );
            log.info( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
            if ( !CollectionUtils.isEmpty( resultMap ) ) {
                String              success    = resultMap.getOrDefault( "message", "" ).toString();
                Map<String, Object> dataResMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                if ( "success".equals( success ) && !CollectionUtils.isEmpty( dataResMap ) ) {
                    String refCode = dataResMap.getOrDefault( "status", "" ).toString();
                    // status 4代付中 5代付失败 6代付成功
                    // refCode 1成功 2失败 3处理中 4待处理
                    int status     = 4;
                    int orderState = 0;
                    if ( "OK_ORDER".equals( refCode ) ) {
                        status = 6;
                        orderState = 1;
                    } else if ( "CLOSE_ORDER".equals( refCode ) ) {
                        status = 5;
                        orderState = 2;
                    }
                    payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status, orderState );
                }
                return resultMap.getOrDefault( "message", "" ).toString();
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
