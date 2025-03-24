package tv.game88.pay.api.payAgent;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import java.math.RoundingMode;
import java.util.*;

@Repository( value = ConstantsPayAgent.ALI_PAY + "PayAgentProcessor" )
@Log4j2
public class AliPayAgentProcessor extends AbstractPayAgent {

    @Override
    public String getName() {
        return "支付宝钱包代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> payMap = new TreeMap<>();
        payMap.put( "busiAmount", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.DOWN ) );
        payMap.put( "reqId", withdrawDetail.getWithdrawOrderNo() );
        payMap.put( "accountAddr", withdrawDetail.getBankAccount() );
        payMap.put( "callbackUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        log.warn( JsonUtil.object2Json( payMap ) );

        String params = null;
        try {
            String signMd5       = AESCoder.decrypt( payAgentChannel.getSignMd5() );
            String signPublicKey = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );
            params = AESCoder.encryptBase64ByKeyIv( JsonUtil.object2Json( payMap ), signMd5, signPublicKey );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "pay", params );
        requestMap.put( "dc", payAgentChannel.getMerId() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.set( "TraceId", withdrawDetail.getWithdrawId() );
        httpHeaders.set( "uuid", IdWorker.get32UUID() );
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        log.warn( JsonUtil.object2Json( httpEntity ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), httpEntity, reqPayAgent );

        log.info( payAgentPlatform.getName() + "下单结果 - result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "C2".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", Collections.emptyMap() );
                String              status = result.getOrDefault( "status", "-1" ).toString();
                if ( "0".equals( status ) || "1".equals( status ) ) {
                    log.info( payAgentPlatform.getName() + "订单提交成功 - listResult:{}", JsonUtil.object2Json( resultMap ) );
                    return true;
                } else {
                    reqPayAgent.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
                }
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
            }
        }
        log.warn( payAgentPlatform.getName() + "订单提交失败 - result:{}", JsonUtil.object2Json( resultMap ) );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        String merchantOrderNo = requestMap.getOrDefault( "reqId", "" ).toString();
        String transferId      = requestMap.getOrDefault( "payId", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( merchantOrderNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", merchantOrderNo );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 2 ) {
            log.error( "订单已拒绝，无需回调 - merOrderNo:{}", merchantOrderNo );
            return "success";
        }
        if ( withdrawDetail.getStatus() == 6 ) {
            log.error( "已有代付记录 - merOrderNo:{}", merchantOrderNo );
            return "success";
        }

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( merchantOrderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String resultString = requestMap.getOrDefault( "result", "" ).toString();

        try {
            String signMd5       = AESCoder.decrypt( payAgentChannel.getSignMd5() );
            String signPublicKey = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );
            String resultJson    = AESCoder.decryptBase64ByKeyIv( resultString, signMd5, signPublicKey );
            log.warn( resultJson );
            Map<String, Object> resultMap = JsonUtil.json2Map( resultJson );
            if ( !CollectionUtils.isEmpty( resultMap ) ) {

                boolean isSuccess = "1".equals( resultMap.getOrDefault( "status", "-1" ).toString() );

                payAgentService.processOrderPay( withdrawDetail, payAgentLog, transferId, payAgentChannel, isSuccess );
                log.info( payAgentPlatform.getName() + "订单号:{},回调状态:{},", merchantOrderNo, isSuccess ? "成功" : "失败" );
                return "success";
            }
            return "fail";
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) throws Exception {
        return null;
    }

    @Override
    public String queryOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                                 PayAgentPlatform payAgentPlatform ) throws Exception {
        SortedMap<String, Object> payMap = new TreeMap<>();
        payMap.put( "reqId", withdrawDetail.getWithdrawOrderNo() );

        log.warn( JsonUtil.object2Json( payMap ) );

        String params = null;
        try {
            String signMd5       = AESCoder.decrypt( payAgentChannel.getSignMd5() );
            String signPublicKey = AESCoder.decrypt( payAgentChannel.getSignPublicKey() );
            params = AESCoder.encryptBase64ByKeyIv( JsonUtil.object2Json( payMap ), signMd5, signPublicKey );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "pay", params );
        requestMap.put( "dc", payAgentChannel.getMerId() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.set( "TraceId", withdrawDetail.getWithdrawId() );
        httpHeaders.set( "uuid", IdWorker.get32UUID() );
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), httpEntity, null );

        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "C2".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                Map<String, Object> result     = ( Map<String, Object> ) resultMap.getOrDefault( "result",
                        Collections.emptyMap() );
                int                 statusCode = Integer.parseInt( result.getOrDefault( "status", -1 ).toString() );
                //  0待支付1已完成2失败
                int status = switch ( statusCode ) {
                    case 1 -> 6;
                    case 2 -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "message", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
