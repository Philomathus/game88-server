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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Repository( value = ConstantsPayAgent.SANJIN_PAY + "PayAgentProcessor" )
@Log4j2
public class SanJinPayAgentProcessor extends AbstractPayAgent {
    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "action", "AgentPay" );
        bodyMap.put( "mer_no", payAgentChannel.getMerId() );
        Map<String, String> cardInfoMap = new HashMap<>();
        cardInfoMap.put( "bankName", withdrawDetail.get );
        cardInfoMap.put( "cardNo", withdrawDetail.getBankAccount().trim() );
        cardInfoMap.put( "name", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "card_info", JsonUtil.object2Json( cardInfoMap ) );
        bodyMap.put( "pay_amt", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );
        bodyMap.put( "notify_url", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "mer_order", withdrawDetail.getWithdrawOrderNo() );
        String ts = LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( bodyMap ) + "&ts=" + ts + "&key=" + signMd5;
        String sign    = DigestUtils.md5Hex( tempStr ).toUpperCase();
        bodyMap.put( "ts", ts );
        bodyMap.put( "hmac", sign );

        log.warn( JsonUtil.object2Json( bodyMap ) );

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( bodyMap );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity( requestMap, httpHeaders );

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
        }
        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "result", "" ).toString();
            if ( "0".equals( code ) ) {
                log.info( payAgentPlatform.getName() + "订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );

                payAgentService.callBackOrder( withdrawDetail, payAgentPlatform );
            }
        }
        log.warn( payAgentPlatform.getName() + "订单提交失败 - orderNo:{}", withdrawDetail.getWithdrawOrderNo() );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        String sign    = requestMap.remove( "hmac" ).toString();
        String ts      = requestMap.remove( "ts" ).toString();
        String dataStr = requestMap.getOrDefault( "data", "" ).toString();

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( bodyMap ) + "&ts=" + ts + "&key=" + signMd5;
        String signStr = DigestUtils.md5Hex( tempStr ).toUpperCase();

        log.info( payAgentPlatform.getName() + "回调签名字符串:" + sign + "_" + signStr );
        if ( sign.equalsIgnoreCase( signStr ) ) {
            Map<String, Object> dataMap  = JsonUtil.json2Map( dataStr );
            String              merOrder = dataMap.getOrDefault( "mer_order", "" ).toString();
            String              status   = dataMap.getOrDefault( "status", "" ).toString();
            String              tradeNo  = dataMap.getOrDefault( "trade_no", "" ).toString();
            if ( StringUtils.isBlank( merOrder ) ) {
                log.error( "提现相关记录丢失 - merOrderNo:{}", merOrder );
                return "fail";
            }
            MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectByOrderNo( merOrder );
            if ( withdrawDetail == null ) {
                log.error( "提现相关记录丢失 - merOrderNo:{}", merOrder );
                return "fail";
            }
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - merOrderNo:{}", merOrder );
                return "success";
            }
            PayAgentLog payAgentLog = payAgentLogMapper.selectByWithdrawOrderNo( merOrder );
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, tradeNo, payAgentPlatform,
                    "TRADE_SUCCESS".equals( status ) );
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
        MemberWithdrawDetail withdrawDetail   = withdrawDetailMapper.selectByOrderNo( payAgentLog.getWithdrawOrderNo() );
        PayAgentPlatform     payAgentPlatform =
                payAgentPlatformMapper.selectPayAgentPlatformById( payAgentLog.getPayAgentPlatId() );

        Map<String, Object> paramsMap = new TreeMap<>();
        paramsMap.put( "action", "OrderQuery" );
        paramsMap.put( "order_no", withdrawDetail.getOrderNo() );
        paramsMap.put( "mer_no", payAgentChannel.getMerId() );

        String ts = DateFormatUtils.formate( new Date(), DateFormatUtils.TIGHT_PATTERN_DATETIME );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( paramsMap ) + "&ts=" + ts + "&key=" + signMd5;
        String sign    = DigestUtils.md5Hex( tempStr ).toUpperCase();
        paramsMap.put( "ts", ts );
        paramsMap.put( "hmac", sign );

        log.warn( JsonUtil.object2Json( paramsMap ) );

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( paramsMap );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( payAgentPlatform.getPayOrderQueryAddr(), HttpMethod.POST,
                    restTemplate.httpEntityCallback( httpEntity ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = CharStreams.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
            log.info( payAgentPlatform.getName() + "查询结果- result:{}", JsonUtil.object2Json( resultMap ) );
            if ( !CollectionUtils.isEmpty( resultMap ) ) {
                String              code    = resultMap.getOrDefault( "result", "" ).toString();
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                if ( "0".equals( code ) && !CollectionUtils.isEmpty( dataMap ) ) {
                    String state = dataMap.getOrDefault( "trade_status", "" ).toString();
                    // status 4代付中 5代付失败 6代付成功
                    int status = 4;
                    if ( "TRADE_SUCCESS".equals( state ) ) {
                        status = 6;
                    } else if ( "TRADE_FAIL".equals( state ) ) {
                        status = 5;
                    }
                    payAgentService.processOrder( payAgentPlatform, withdrawDetail, withdrawDetail.getUpdateTime(), status, 0 );
                }
                return resultMap.getOrDefault( "msg", "" ).toString();
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getOrderNo();
    }
}
