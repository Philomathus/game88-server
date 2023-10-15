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
import org.springframework.web.client.HttpServerErrorException;
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
import tv.game88.pay.api.type.BankCodeLianFuBaoType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

@Repository( value = ConstantsPayAgent.LIAN_FU_BAO + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class LianFuBaoAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "联付宝代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        BankCodeLianFuBaoType     bankCodeType = BankCodeLianFuBaoType.getCodeByBankId( withdrawDetail.getBankId() );
        SortedMap<String, Object> bodyMap      = new TreeMap<>();
        bodyMap.put( "merOrderNo", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "amount", withdrawDetail.getWithdrawMoney().setScale( 0, RoundingMode.HALF_UP ) );
        bodyMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "bankCode", bankCodeType.name() );
        bodyMap.put( "submitTime", Timestamp.valueOf( reqPayAgent.getCurrentTime() ).getTime() );
        bodyMap.put( "bankAccountNo", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "bankAccountName", withdrawDetail.getBankUserName().trim() );
        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + signMd5;

        String sign = DigestUtils.md5Hex( signStr ).toUpperCase();
        bodyMap.put( "sign", sign );

        String orderJson = JsonUtil.object2Json( bodyMap );
        log.info( "非对称加密加密前:" + orderJson );

        // 使用非对称加密加密此dataMap
        String signPublicKey = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );
        String data          = RSACoder.encryptByPublicKey( orderJson, signPublicKey );
        log.info( "非对称加密加密后:" + data );
        // 封装请求协议
        Map<String, String> dataMap = new LinkedHashMap<>();
        dataMap.put( "merId", payAgentChannel.getMerId() );
        dataMap.put( "version", "1.1" );
        dataMap.put( "data", data );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( dataMap, httpHeaders );

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
            if ( e instanceof HttpServerErrorException ) {
                reqPayAgent.setFailReason( "三方网络异常:" + e.getMessage() );

                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
                return false;
            }
        }
        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
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

        String               dataStr        = requestMap.getOrDefault( "data", "" ).toString();
        String               merOrderNo     = requestMap.getOrDefault( "merOrderNo", "" ).toString();
        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( merOrderNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", merOrderNo );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 6 ) {
            log.error( "已有代付记录 - merOrderNo:{}", merOrderNo );
            return "success";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( merOrderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String signPrivateKey = AESCoder.decrypt( payAgentChannel.getSignPrivateKey() );
        String data           = RSACoder.decryptByPrivateKey( dataStr, signPrivateKey );
        log.info( data );
        Map<String, Object> resultMap = JsonUtil.json2Map( data );


        String orderNo    = resultMap.getOrDefault( "orderNo", "" ).toString();
        int    orderState = Integer.parseInt( resultMap.getOrDefault( "orderState", -1 ).toString() );

        // 解密后对签名验证
        SortedMap<String, Object> signMap = new TreeMap<>();
        signMap.put( "merOrderNo", merOrderNo );
        signMap.put( "orderState", orderState );
        signMap.put( "orderNo", orderNo );
        signMap.put( "amount", resultMap.get( "amount" ) );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String signStr = this.assemblyUrl( signMap ) + "&key=" + signMd5;
        log.info( signStr );
        String sign = DigestUtils.md5Hex( signStr );
        log.warn( sign + " : " + resultMap.get( "sign" ).toString() );
        if ( sign.equalsIgnoreCase( resultMap.get( "sign" ).toString() ) ) {
            if ( orderState > 0 ) {

                payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderNo, payAgentChannel, orderState == 1 );
            }
            return "success";
        }
        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return null;
        }
        SortedMap<String, Object> signMap              = new TreeMap<>();
        String                    merOrderNo           = requestMap.getOrDefault( "merOrderNo", "" ).toString();
        MemberWithdrawDetail      memberWithdrawDetail = withdrawDetailMapper.selectById( merOrderNo );
        if ( memberWithdrawDetail == null ) {
            signMap.put( "code", "1002" );
            signMap.put( "message", "订单不存在" );
            return signMap;
        }
        PayAgentLog payAgentLog = payAgentLogMapper.selectById( merOrderNo );
        if ( payAgentLog == null ) {
            signMap.put( "code", "1002" );
            signMap.put( "message", "代付记录不存在" );
            return signMap;
        }
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        SortedMap<String, Object> requestSignMap = new TreeMap<>( requestMap );

        String sign    = requestSignMap.remove( "sign" ).toString();
        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = this.assemblyUrl( requestSignMap ) + "&key=" + signMd5;
        String mySign  = DigestUtils.md5Hex( signStr );

        String     merId         = requestSignMap.getOrDefault( "merId", "" ).toString();
        BigDecimal amount        = new BigDecimal( requestSignMap.getOrDefault( "amount", "0" ).toString() );
        String     bankAccountNo = requestSignMap.getOrDefault( "bankAccountNo", "" ).toString();


        signMap.put( "submitTime", String.valueOf( System.currentTimeMillis() ) );
        signMap.put( "code", "1001" );
        signMap.put( "message", "签名错误" );
        signMap.put( "merId", payAgentChannel.getMerId() );
        signMap.put( "merOrderNo", merOrderNo );
        if ( StringUtils.equalsIgnoreCase( sign, mySign ) ) {
            if ( amount.compareTo( memberWithdrawDetail.getWithdrawMoney() ) != 0 ) {
                signMap.put( "code", "1004" );
                signMap.put( "message", "金额不匹配" );
                return signMap;
            } else if ( !bankAccountNo.equals( memberWithdrawDetail.getBankAccount() ) ) {
                signMap.put( "code", "1003" );
                signMap.put( "message", "银行卡号不匹配" );
                return signMap;
            } else if ( !merId.equals( payAgentChannel.getMerId() ) ) {
                signMap.put( "code", "9999" );
                signMap.put( "message", "商户号错误" );
                return signMap;
            } else {
                signMap.put( "code", "0" );
                signMap.put( "message", "成功" );
            }
        }
        String resultSignStr = this.assemblyUrl( signMap ) + "&key=" + signMd5;
        signMap.put( "sign", DigestUtils.md5Hex( resultSignStr ) );
        return signMap;
    }

    @Override
    public String queryOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                                 PayAgentPlatform payAgentPlatform ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merOrderNo", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "submitTime", System.currentTimeMillis() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        // 生成签名信息
        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + signMd5;
        String sign    = DigestUtils.md5Hex( signStr );
        bodyMap.put( "sign", sign );

        // 使用非对称加密加密此dataMap
        String signPublicKey = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );
        String data          = RSACoder.encryptByPublicKey( JsonUtil.object2Json( bodyMap ), signPublicKey );

        // 封装请求协议
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put( "merId", payAgentChannel.getMerId() );
        dataMap.put( "version", "1.1" );
        dataMap.put( "data", data );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( dataMap, httpHeaders );

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
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        log.info( "联付宝代付查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                log.info( "联付宝代付订单查询成功" );
                Map<String, Object> resultDataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                int                 orderState    = Integer.parseInt( resultDataMap.getOrDefault( "orderState", 0 ).toString() );
                // status 4代付中5代付失败6代付成功
                // orderState (0=处理中，1=成功，2=失败)
                int status = switch ( orderState ) {
                    case 1 -> 6;
                    case 2 -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "message", "" ).toString();
        }
        return "联付宝代付查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
