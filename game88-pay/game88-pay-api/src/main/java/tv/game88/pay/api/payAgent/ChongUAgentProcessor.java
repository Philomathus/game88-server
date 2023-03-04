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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.CHONG_U + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class ChongUAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "充u代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {

        SortedMap<String, Object> bodyMap      = new TreeMap<>();
        bodyMap.put( "Amount", withdrawDetail.getWithdrawMoney().setScale( 0, RoundingMode.HALF_UP ) );
        bodyMap.put( "BankCardBankName", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "BankCardNumber", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "BankCardRealName", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "MerchantId", reqPayAgent.getPayAgentChannelId() );
        bodyMap.put( "MerchantUniqueOrderId", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "NotifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "Timestamp", LocalDateTimeUtils.format( reqPayAgent.getCurrentTime(),
                LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        bodyMap.put( "WithdrawTypeId", 0);

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + signMd5;

        String sign = DigestUtils.md5Hex( signStr ).toLowerCase();
        bodyMap.put( "Sign", sign );

        String orderJson = JsonUtil.object2Json( bodyMap );
        log.info( "非对称加密加密前:" + orderJson );

        log.warn( JsonUtil.object2Json( bodyMap ) );
        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageJson( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String return_code = resultMap.getOrDefault("Code", "").toString();
            if ("0".equals(return_code)) {
                log.info(payAgentPlatform.getName() + "代付订单提交成功 - result:{}", JsonUtil.object2Json(resultMap));
                return true;
            } else {
                reqPayAgent.setFailReason(resultMap.getOrDefault("Message", "").toString());
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
        String sign      = requestMap.remove( "sign" ).toString();
        String withdrawOrderId = requestMap.getOrDefault( "MerchantUniqueOrderId", "" ).toString();
        String status    = requestMap.getOrDefault( "Status", "" ).toString();

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( withdrawOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        // 去除空值
        requestMap.entrySet().removeIf( me -> me.getValue() == null || org.apache.commons.lang3.StringUtils.isBlank( me.getValue().toString() ) );
        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String mySign = DigestUtils.md5Hex( signStr ).toLowerCase();
        if ( mySign.equalsIgnoreCase( sign ) ) {

            MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( withdrawOrderId );
            if ( withdrawDetail == null ) {
                log.error( "提现相关记录丢失 - withdrawOrderId:{}", withdrawOrderId );
                return "fail";
            }
            if ( withdrawDetail.getStatus() == 0 ) {
                log.error( "已有代付记录 - withdrawOrderId:{}", withdrawOrderId );
                return "SUCCESS";
            }

            payAgentService.processOrderPay( withdrawDetail, payAgentLog, requestMap.getOrDefault("WithdrawOrderId", "").toString(), payAgentChannel,
                        "SUCCESS".equals( status ) );
            log.info( payAgentPlatform.getName()
                        + "订单号:{},回调状态:{},", withdrawOrderId, "SUCCESS".equals( status ) ? "成功" : "失败" );
            return "SUCCESS";
        }

        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) {
       return null;
    }

    @Override
    public String queryOrderPay( PayAgentLog payAgentLog ) {
        MemberWithdrawDetail withdrawDetail   = withdrawDetailMapper.selectById( payAgentLog.getWithdrawOrderNo() );
        PayAgentChannel      payAgentChannel  = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );
        PayAgentPlatform     payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );

        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put("MerchantId", payAgentPlatform.getId());
        dataMap.put("Timestamp", LocalDateTimeUtils.format(LocalDateTime.now(),
                LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        dataMap.put("MerchantUniqueOrderId", withdrawDetail.getWithdrawOrderNo());

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        // 生成签名信息
        String signStr = this.assemblyUrl( dataMap ) + signMd5;
        String sign    = DigestUtils.md5Hex( signStr );
        dataMap.put( "Sign", sign );

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll(dataMap);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestMap, httpHeaders);

        Map<String, Object> resultMap;
        try {
            resultMap = restTemplate.execute(payAgentPlatform.getOrderQueryUrl(), HttpMethod.POST,
                    restTemplate.httpEntityCallback(httpEntity), response -> {
                        InputStream bodyStream = response.getBody();
                        String      text;
                        try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                            text = IOUtils.toString( reader );
                        }
                        return JsonUtil.json2Map( text );
                    });
            log.warn(payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json(resultMap));

            if (!CollectionUtils.isEmpty(resultMap)) {
                String return_code = resultMap.getOrDefault("Code", "").toString();
                if ("0".equals(return_code)) {
                    String trade_state = resultMap.getOrDefault("WithdrawOrderStatus", "").toString();
                    if ("100".equals(trade_state) || "0".equals(trade_state) || "-90".equals(trade_state)) {
                        // status 4代付中 5代付失败 6代付成功
                        // trade_state  100成功 -90失败 0 處理中,需繼續查詢
                        int status = 4;
                        int orderStatus = 0;
                        if ("100".equals(trade_state)) {
                            status = 6;
                            orderStatus = 1;
                        } else if ("-90".equals(trade_state)) {
                            status = 5;
                            orderStatus = 2;
                        }
                        payAgentService.processOrder(payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status, orderStatus);
                    }
                }
            }
            return resultMap.getOrDefault( "Message", "" ).toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
