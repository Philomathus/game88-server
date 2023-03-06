package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RSACoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.type.BankCodeZhaoHType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.ZHAOH + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class ZhaohPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "找换宝代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        BankCodeZhaoHType         bankCodeType = BankCodeZhaoHType.getCodeByBankId( withdrawDetail.getBankId() );
        SortedMap<String, Object> bodyMap      = new TreeMap<>();
        bodyMap.put( "merchantNo", reqPayAgent.getWithdrawOrderNo() );
        bodyMap.put( "merchantCode", payAgentChannel.getMerId() );
        bodyMap.put( "userId", "0" );
        bodyMap.put( "channelGroup", "0" );
        bodyMap.put( "amount", withdrawDetail.getWithdrawMoney().setScale( 0, RoundingMode.HALF_UP ) );
        bodyMap.put( "coinUnit", "CNY" );
        bodyMap.put( "callbackUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "issueBankCode", bankCodeType.name() );
        bodyMap.put( "name", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "bankNo", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "callbackDataFormat", "JSON" );
        String headerValue = AESCoder.decrypt( payAgentChannel.getHeaderValue() );
        String signStr = this.assemblyUrl( bodyMap ) + "&token=" + headerValue + "&sign="
                + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        bodyMap.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        log.warn( JsonUtil.object2Json( bodyMap ) );

        String publicKey = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "data", RSACoder.encryptByPublicKey( this.assemblyUrl( bodyMap ), publicKey ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.set( "token", headerValue );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), httpEntity, reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String dataStr = resultMap.getOrDefault( "data", "" ).toString();
            Map<String, Object> resDataMap = JsonUtil.json2Map( RSACoder.decryptByPublicKey( dataStr, publicKey ) );
            log.warn( "解密数据:" + JsonUtil.object2Json( resDataMap ) );
            if ( "0".equals( resDataMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> data = JsonUtil.json2Map( resDataMap.getOrDefault( "data", "" ).toString() );
                if ( "SUCCESS".equals( data.getOrDefault( "result", "" ) ) ) {
                    log.info( payAgentPlatform.getName() + "订单提交成功 - result:{}", resDataMap.getOrDefault( "data", "" )
                                                                                                 .toString() );
                    return true;
                }
            }
            reqPayAgent.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
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

        String dataStr = requestMap.getOrDefault( "data", "" ).toString();
        String orderNo = requestMap.getOrDefault( "orderNo", "" ).toString();

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( orderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String signMd5     = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String privateKey  = AESCoder.decrypt( payAgentChannel.getSignPrivateKey() );
        String headerValue = AESCoder.decrypt( payAgentChannel.getHeaderValue() );

        Map<String, Object> dataMap = JsonUtil.json2Map( RSACoder.decryptByPrivateKey( dataStr, privateKey ) );

        log.warn( "解密数据:" + JsonUtil.object2Json( dataMap ) );

        // 解密后对签名验证
        SortedMap<String, Object> signMap = new TreeMap<>( dataMap );

        String sign    = signMap.remove( "sign" ).toString();
        String tempStr = this.assemblyUrl( signMap ) + "&token=" + headerValue + "&sign=" + signMd5;
        if ( StringUtils.equalsIgnoreCase( DigestUtils.md5Hex( tempStr ), sign ) ) {
            int status = Integer.parseInt( dataMap.getOrDefault( "status", -1 ).toString() );
            if ( status == 2 || status == 3 ) {
                MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( orderNo );
                if ( withdrawDetail == null ) {
                    log.error( "提现相关记录丢失 - merOrderNo:{}", orderNo );
                    return "fail";
                }
                if ( withdrawDetail.getStatus() == 6 ) {
                    log.error( "已有代付记录 - merOrderNo:{}", orderNo );
                    return "success";
                }
                payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderNo, payAgentChannel, status == 2 );
                log.info( payAgentPlatform.getName() + "订单号:{},回调状态:{},", orderNo, status == 2 ? "成功" : "失败" );
            }
            return "success";
        }
        log.info( payAgentPlatform.getName() + "回调验签失败" );
        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) {
        Map<String, Object> resultMap = new TreeMap<>();
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            resultMap.put( "message", "请求ip非白名单:" + realIp );
            resultMap.put( "code", "9999" );
            return resultMap;
        }
        Map<String, Object>  dataMap           = JsonUtil.json2Map( requestMap.get( "data" ).toString() );
        String               orderNo           = dataMap.get( "merOrderNo" ).toString();
        MemberWithdrawDetail memberWithdrawLog = withdrawDetailMapper.selectById( orderNo );
        if ( memberWithdrawLog == null ) {
            resultMap.put( "message", "订单不存在" );
            resultMap.put( "code", "1002" );
            return resultMap;
        }
        PayAgentLog payAgentLog = payAgentLogMapper.selectById( orderNo );
        if ( payAgentLog == null ) {
            resultMap.put( "code", "1002" );
            resultMap.put( "message", "代付记录不存在" );
            return resultMap;
        }
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        // 解密后对签名验证
        SortedMap<String, Object> signMap     = new TreeMap<>( dataMap );
        String                    signMd5     = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String                    headerValue = AESCoder.decrypt( payAgentChannel.getHeaderValue() );

        String sign    = signMap.remove( "sign" ).toString();
        String tempStr = this.assemblyUrl( signMap ) + "&token=" + headerValue + "&sign=" + signMd5;

        if ( StringUtils.equalsIgnoreCase( DigestUtils.md5Hex( tempStr ), sign ) ) {
            String     bankAccountNo = dataMap.get( "bankAccountNo" ).toString();
            String     merchNo       = requestMap.get( "merchNo" ).toString();
            String     merId         = dataMap.get( "merId" ).toString();
            BigDecimal amount        = new BigDecimal( dataMap.get( "amount" ).toString() );

            if ( amount.compareTo( memberWithdrawLog.getWithdrawMoney() ) != 0 ) {
                signMap.put( "code", "1004" );
                signMap.put( "message", "金额不匹配" );
                return resultMap;
            }
            if ( !bankAccountNo.equals( memberWithdrawLog.getBankAccount() ) ) {
                signMap.put( "code", "1003" );
                signMap.put( "message", "银行卡不匹配" );
                return resultMap;
            }
            if ( !merchNo.equals( payAgentChannel.getMerId() ) ) {
                signMap.put( "code", "9999" );
                signMap.put( "message", "商户号错误" );
                return resultMap;
            }
            resultMap.put( "code", "0" );
            resultMap.put( "message", "验证成功" );
            resultMap.put( "merId", merId );
            resultMap.put( "merOrderNo", orderNo );
            String signRes = this.assemblyUrl( resultMap ) + "&token=" + headerValue + "&sign=" + signMd5;
            resultMap.put( "sign", DigestUtils.md5Hex( signRes ) );
            return resultMap;
        }
        resultMap.put( "message", "验签失败" );
        resultMap.put( "code", "1001" );
        return resultMap;
    }

    @Override
    public String queryOrderPay( PayAgentLog payAgentLog ) throws Exception {
        MemberWithdrawDetail withdrawDetail   = withdrawDetailMapper.selectById( payAgentLog.getWithdrawOrderNo() );
        PayAgentChannel      payAgentChannel  = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );
        PayAgentPlatform     payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );

        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantNo", withdrawDetail.getWithdrawOrderNo() );
        params.put( "merchantCode", payAgentChannel.getMerId() );

        String signMd5     = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String headerValue = AESCoder.decrypt( payAgentChannel.getHeaderValue() );
        String publicKey   = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );
        // 生成签名信息
        String signStr = this.assemblyUrl( params ) + "&token=" + headerValue + "&sign=" + signMd5;
        String sign    = DigestUtils.md5Hex( signStr );
        params.put( "sign", sign );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "data", RSACoder.encryptByPublicKey( this.assemblyUrl( params ), publicKey ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.set( "token", headerValue );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), httpEntity, null );

        log.info( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              dataStr    = resultMap.getOrDefault( "data", "" ).toString();
            Map<String, Object> resDataMap = JsonUtil.json2Map( RSACoder.decryptByPublicKey( dataStr, publicKey ) );
            log.warn( "解密数据:" + JsonUtil.object2Json( resDataMap ) );
            if ( "0".equals( resDataMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> data = JsonUtil.json2Map( resDataMap.getOrDefault( "data", "" ).toString() );
                if ( !CollectionUtils.isEmpty( data ) ) {
                    int orderStatus = Integer.parseInt( data.getOrDefault( "status", 0 ).toString() );
                    // status 4代付中5代付失败6代付成功
                    // orderState (0=处理中，1=成功，2=失败)
                    int status;
                    int orderState;
                    switch ( orderStatus ) {
                    case 2 -> {
                        status     = 6;
                        orderState = 1;
                    }
                    case 3 -> {
                        status     = 5;
                        orderState = 2;
                    }
                    default -> {
                        status     = 4;
                        orderState = 0;
                    }
                    }
                    payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status,
                            orderState );
                }
            }
            return resultMap.getOrDefault( "message", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}