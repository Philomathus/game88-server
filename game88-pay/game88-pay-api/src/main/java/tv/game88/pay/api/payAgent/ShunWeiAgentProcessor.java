package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.*;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.type.BankCodeShunWeiType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Repository( value = ConstantsPayAgent.SHUN_WEI + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class ShunWeiAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "顺为代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        BankCodeShunWeiType bankCodeType = BankCodeShunWeiType.getCodeByBankId( withdrawDetail.getBankId() );
        Map<String, String> dataMap      = new TreeMap<>();
        dataMap.put( "client_num", payAgentChannel.getMerId() );
        dataMap.put( "order_num", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "amount", withdrawDetail
                .getWithdrawMoney()
                .multiply( new BigDecimal( 100 ) )
                .setScale( 0, RoundingMode.HALF_EVEN )
                .toString() );
        dataMap.put( "bank_account_name", withdrawDetail.getBankUserName().trim() );
        dataMap.put( "bank_account_no", withdrawDetail.getBankAccount().trim() );
        dataMap.put( "bank_code", bankCodeType.name() );
        String randStr = this.generateRandNum( dataMap.size() + 1 );
        dataMap.put( "random_str", randStr );
        // 签名
        Map<String, String> paramMap = paramSort( dataMap, randStr );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String sign    = DigestUtils.md5Hex( JsonUtil.object2Json( paramMap ).concat( signMd5 ) );
        paramMap.put( "request_sign", sign );
        paramMap.put( "callback_url", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        // 参数加密
        String signPublicKey = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );
        String encryptData   = RSACoder.encryptByPublicKey( JsonUtil.object2Json( paramMap ), signPublicKey );
        // 请求参数封装
        Map<String, String> params = new HashMap<>();
        params.put( "request_body", URLEncoder.encode( encryptData, StandardCharsets.UTF_8 ) );
        String headerValue = AESCoder.decrypt( payAgentChannel.getHeaderValue() );
        params.put( "interface_version", DigestUtils.md5Hex( "1.0.0".concat( headerValue ) ) );

        String paramsRequest = this.assemblyUrl( params );
        String result        = null;
        try {
            result = request( payAgentPlatform.getOrderUrl(), paramsRequest, headerValue );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            if ( e instanceof IOException && e.getMessage().contains( "Server returned HTTP response code" ) ) {
                reqPayAgent.setFailReason( "三方网络异常:" + e.getMessage() );

                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
                return false;
            }
        }
        log.warn( payAgentPlatform.getName() + "下单结果:{},订单号:{}", result, withdrawDetail.getWithdrawOrderNo() );
        Map<String, String> resultMap = JsonUtil.json2Map( result );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.get( "state_code" ) ) ) {
                resultMap.remove( "state_code" );
                resultMap.remove( "message" );
                String              resultSign = resultMap.remove( "sign" );
                String              randNum    = resultMap.get( "random_str" );
                Map<String, String> param      = paramSort( resultMap, randNum );
                String              temp       = JsonUtil.object2Json( param );
                String              reSign     = DigestUtils.md5Hex( temp.concat( signMd5 ) );
                if ( !org.apache.commons.lang3.StringUtils.equalsIgnoreCase( resultSign, reSign ) ) {
                    return false;
                }
                log.info( payAgentPlatform.getName() + "订单提交成功，orderNo：{}", withdrawDetail.getWithdrawOrderNo() );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "message", "" ) );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName()
                + "订单提交失败 - orderNo:{},result:{}", withdrawDetail.getWithdrawOrderNo(), JsonUtil.object2Json( resultMap ) );
        return false;
    }

    private String generateRandNum( int size ) {
        StringBuilder randStr = new StringBuilder();
        Random        randDom = new Random();
        do {
            String tmpChar = String.valueOf( randDom.nextInt( size ) );
            if ( randStr.indexOf( tmpChar ) == -1 ) {
                randStr.append( tmpChar );
            }
        } while ( randStr.length() < size );
        return randStr.toString();
    }

    private Map<String, String> paramSort( Map<String, ?> map, String indexStr ) {
        Map<String, String> sortMap = new LinkedHashMap<>();
        String[]            keys    = map.keySet().toArray( new String[] {} );
        Arrays.sort( keys );
        char[] indexs = indexStr.toCharArray();
        for ( char i : indexs ) {
            int index = Integer.parseInt( String.valueOf( i ) );
            sortMap.put( keys[ index ], map.get( keys[ index ] ).toString() );
        }
        return sortMap;
    }

    private String request( String url, String params, String headerKey ) throws Exception {
        URL               urlObj = new URL( url );
        HttpURLConnection conn   = ( HttpURLConnection ) urlObj.openConnection();
        conn.setRequestMethod( "POST" );
        conn.setDoOutput( true );
        conn.setDoInput( true );
        conn.setUseCaches( false );
        conn.setConnectTimeout( 5000 );
        conn.setRequestProperty( "Charset", "UTF-8" );
        conn.setRequestProperty( "security_header_key", headerKey );
        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
        conn.setRequestProperty( "Content-Length", String.valueOf( params.length() ) );
        OutputStream outStream = conn.getOutputStream();
        outStream.write( params.getBytes( StandardCharsets.UTF_8 ) );
        outStream.flush();
        outStream.close();
        return getResponseBodyAsString( conn.getInputStream() );
    }

    private String getResponseBodyAsString( InputStream in ) {
        try {
            BufferedInputStream buf    = new BufferedInputStream( in );
            byte[]              buffer = new byte[ 1024 ];
            StringBuilder       data   = new StringBuilder();
            int                 readDataLen;
            while ( ( readDataLen = buf.read( buffer ) ) != -1 ) {
                data.append( new String( buffer, 0, readDataLen, StandardCharsets.UTF_8 ) );
            }
            return data.toString();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return "fail";
        }

        String               order_num      = ( String ) requestMap.get( "order_num" );
        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( order_num );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", order_num );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 2 ) {
            log.error( "订单已拒绝，无需回调 - merOrderNo:{}", order_num );
            return "success";
        }
        if ( withdrawDetail.getStatus() == 6 ) {
            log.error( "已有代付记录 - merOrderNo:{}", order_num );
            return "success";
        }
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( withdrawDetail.getPayAgentChannelId() );

        String signPrivateKey = AESCoder.decrypt( payAgentChannel.getSignPrivateKey() );
        String data           = requestMap.getOrDefault( "data", "" ).toString();
        String str            = RSACoder.decryptByPrivateKey( data, signPrivateKey );

        log.info( payAgentPlatform.getName() + "回调数据:" + str );

        Map<String, Object>       resultMap = JsonUtil.json2Map( str );
        String                    reSign    = resultMap.remove( "sign" ).toString();
        SortedMap<String, Object> signMap   = new TreeMap<>( resultMap );
        Map<String, String>       map       = paramSort( signMap, signMap.get( "random_str" ).toString() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String sign = DigestUtils.md5Hex( JsonUtil.object2Json( map ).concat( signMd5 ) );

        log.info( payAgentPlatform.getName() + "回调签名字符串:" + reSign + "_" + sign );
        if ( ( reSign ).equalsIgnoreCase( sign ) ) {

            String remit_result = ( String ) signMap.get( "remit_result" );

            if ( "SUCCESS".equals( remit_result ) || "FAILED".equals( remit_result ) ) {
                PayAgentLog payAgentLog = payAgentLogMapper.selectById( order_num );
                payAgentService.processOrderPay( withdrawDetail, payAgentLog, "", payAgentChannel,
                        "SUCCESS".equals( remit_result ) );
                log.info( payAgentPlatform.getName()
                        + "订单号:{},回调状态:{},", order_num, "SUCCESS".equals( remit_result ) ? "成功" : "失败" );
            }
            return "ok";
        }
        log.info( payAgentPlatform.getName() + "回调验签失败" );
        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put( "dateTime", LocalDateTimeUtils.format( LocalDateTime.now(),
                LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        resultMap.put( "sign", "" );
        resultMap.put( "code", "99" );
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            resultMap.put( "msg", "请求非白名单" );
            return resultMap;
        }
        log.warn( "反查数据:" + JsonUtil.object2Json( requestMap ) );
        String clientOrderNo = requestMap.get( "clientOrderNo" ).toString();

        MemberWithdrawDetail memberWithdrawDetail = withdrawDetailMapper.selectById( clientOrderNo );
        if ( memberWithdrawDetail == null ) {
            resultMap.put( "msg", "订单不存在" );
            return resultMap;
        }
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( memberWithdrawDetail.getPayAgentChannelId() );

        String reSign = requestMap.remove( "sign" ).toString();

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String mySign = DigestUtils.md5Hex( this.assemblyUrl( requestMap ) + "&key=" + signMd5 );
        if ( ( reSign ).equalsIgnoreCase( mySign ) ) {
            BigDecimal amount        = new BigDecimal( requestMap.remove( "amount" ).toString() );
            String     bankAccountNo = requestMap.remove( "bankAccountNo" ).toString();
            String     clientCode    = requestMap.get( "clientCode" ).toString();
            if ( amount.compareTo( memberWithdrawDetail.getWithdrawMoney() ) != 0 || !bankAccountNo.equals( memberWithdrawDetail
                    .getBankAccount()
                    .trim() ) || !clientCode.equals( payAgentChannel.getMerId() ) ) {
                resultMap.put( "msg", "订单不匹配" );
                return resultMap;
            }
            resultMap.put( "code", "00" );
            resultMap.put( "msg", "验证成功" );

            resultMap.put( "sign", DigestUtils.md5Hex( this.assemblyUrl( resultMap ) + "&key=" + signMd5 ) );
            return resultMap;
        }
        resultMap.put( "msg", "验签失败" );
        return resultMap;
    }

    @Override
    public String queryOrderPay( PayAgentLog payAgentLog ) throws Exception {
        MemberWithdrawDetail withdrawDetail   = withdrawDetailMapper.selectById( payAgentLog.getWithdrawOrderNo() );
        PayAgentChannel      payAgentChannel  = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );
        PayAgentPlatform     payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );

        Map<String, String> dataMap = new TreeMap<>();
        dataMap.put( "client_num", payAgentChannel.getMerId() );
        dataMap.put( "order_num", withdrawDetail.getWithdrawOrderNo() );
        String randStr = generateRandNum( dataMap.size() + 1 );
        dataMap.put( "random_str", randStr );
        Map<String, String> paramMap = paramSort( dataMap, randStr );
        log.info( "签名原文串：{}", JsonUtil.object2Json( paramMap ) );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String sign = DigestUtils.md5Hex( JsonUtil.object2Json( paramMap ).concat( signMd5 ) );
        paramMap.put( "request_sign", sign );

        String signPrivateKey = AESCoder.decrypt( payAgentChannel.getSignPrivateKey() );
        // 参数加密
        String encryptData = RSACoder.encryptByPublicKey( JsonUtil.object2Json( paramMap ), signPrivateKey );
        // 请求参数封装
        Map<String, String> params = new HashMap<>();
        params.put( "request_body", URLEncoder.encode( encryptData, StandardCharsets.UTF_8 ) );
        String headerValue = AESCoder.decrypt( payAgentChannel.getHeaderValue() );
        params.put( "interface_version", DigestUtils.md5Hex( "1.0.0".concat( headerValue ) ) );
        String paramsRequest = this.assemblyUrl( params );

        String result = null;
        try {
            result = request( payAgentPlatform.getOrderQueryUrl(), paramsRequest, headerValue );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", result );
        if ( StringUtils.isNotBlank( result ) ) {
            Map<String, String> jsonObject = JsonUtil.json2Map( result );
            if ( !CollectionUtils.isEmpty( jsonObject ) ) {
                String stateCode = jsonObject.remove( "state_code" );
                if ( org.apache.commons.lang3.StringUtils.equals( "200", stateCode ) ) {
                    String              resultSign = jsonObject.remove( "sign" );
                    Map<String, String> signMap    = new TreeMap<>( jsonObject );
                    String              randNum    = signMap.getOrDefault( "random_str", "" );
                    Map<String, String> param      = paramSort( signMap, randNum );
                    String              signStr    = JsonUtil.object2Json( param );
                    String              reSign     = DigestUtils.md5Hex( signStr.concat( signMd5 ) );
                    if ( StringUtils.equals( resultSign, reSign ) ) {
                        String remit_state_code = jsonObject.getOrDefault( "remit_state_code", "" );
                        // status 4代付中5代付失败6代付成功
                        // orderState (0=处理中，1=成功，2=失败)
                        int status     = 4;
                        int orderState = 0;
                        if ( "SUCCESS".equals( remit_state_code ) ) {
                            status     = 6;
                            orderState = 1;
                        }
                        if ( "FAILED".equals( remit_state_code ) ) {
                            status     = 5;
                            orderState = 2;
                        }
                        payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status,
                                orderState );
                    }
                }
                return jsonObject.getOrDefault( "msg", "" );
            }
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
