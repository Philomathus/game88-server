package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RSACoder;
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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Repository( value = ConstantsPayAgent.BAXI_PAY + "PayAgentProcessor" )
@Log4j2
public class BaxiPayAgentProcessor extends AbstractPayAgent {

    @Override
    public String getName() {
        return "巴西代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel, PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "merId" , payAgentChannel.getMerId()  );
        dataMap.put( "orderId" , withdrawDetail.getWithdrawOrderNo());
        dataMap.put( "money" , withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );
        dataMap.put( "acc_name" , withdrawDetail.getBankUserName() );
        dataMap.put( "acc_no" , withdrawDetail.getBankAccount() );
        dataMap.put( "acc_code" , "0000" );
        dataMap.put( "notifyUrl" , configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        dataMap.put( "nonceStr", UUID.randomUUID().toString().replace( "-", "" ) );
        dataMap.put( "otherpara1", "PIX" );
        dataMap.put( "otherpara2", "PIX" );

        String tempStr        = this.assemblyUrl( dataMap );
        String signPrivateKey = AESCoder.decrypt( payAgentChannel.getSignPrivateKey() );
        String sign           = RSACoder.signSha1Rsa( tempStr, signPrivateKey );
        dataMap.put( "sign", sign );

        log.warn( payAgentPlatform.getName() + "下单请求参数{}", JsonUtil.object2Json( dataMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageJson( dataMap ), reqPayAgent );

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
        String merchantOrderNo = requestMap.getOrDefault( "orderId", "" ).toString();
        String status          = requestMap.getOrDefault( "status", "" ).toString();
        String sign            = requestMap.remove( "sign" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( merchantOrderNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", merchantOrderNo );
            return "fail";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( merchantOrderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        Map<String, Object> treeMap = new TreeMap<>( requestMap );

        String tempStr = this.assemblyUrl( treeMap );

        String signPublicKey = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );

        if ( RSACoder.verifySha1Rsa( tempStr, signPublicKey, sign ) ) {

            if ( withdrawDetail.getStatus() == 2 ) {
                log.error( "订单已拒绝，无需回调 - merOrderNo:{}", merchantOrderNo );
                return "success";
            }
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - merOrderNo:{}", merchantOrderNo );
                return "success";
            }

            payAgentService.processOrderPay( withdrawDetail, payAgentLog, "", payAgentChannel, "1".equals( status ) );
            log.info( payAgentPlatform.getName()
                    + "订单号:{},回调状态:{},", merchantOrderNo, "1".equals( status ) ? "成功" : "失败" );
            return "success";
        }
        log.warn( payAgentChannel.getName() + "验签失败" );
        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        return null;
    }

    @Override
    public String queryOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel, PayAgentPlatform payAgentPlatform ) throws Exception {
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "merId", payAgentChannel.getMerId() );
        dataMap.put( "nonceStr", UUID.randomUUID().toString().replace( "-", "" ) );
        dataMap.put( "orderId", withdrawDetail.getWithdrawOrderNo() );

        String tempStr        = this.assemblyUrl( dataMap );
        String signPrivateKey = AESCoder.decrypt( payAgentChannel.getSignPrivateKey() );
        String sign           = RSACoder.signSha1Rsa( tempStr, signPrivateKey );
        dataMap.put( "sign", sign );

        log.warn( payAgentPlatform.getName() + "查询请求参数{}", JsonUtil.object2Json( dataMap ) );
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

            log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );

            if ( !CollectionUtils.isEmpty( resultMap )) {
                Map<String,Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );

                String statusCode = data.getOrDefault( "status", "" ).toString();
                if ( "1".equals( statusCode ) || "2".equals( statusCode ) || "3".equals( statusCode ) ) {
                    //  4代付中 5代付失败 6代付成功
                    int status = switch ( statusCode ) {
                        case "1" -> 6;
                        case "2", "3" -> 5;
                        default -> 4;
                    };
                    payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
                }
                return resultMap.getOrDefault( "msg", "" ).toString();
            }

        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}

