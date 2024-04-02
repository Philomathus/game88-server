package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPay;
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
import java.util.*;

@Repository( value = ConstantsPayAgent.QD_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class QDPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "QDPay";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        if ( !Objects.equals( withdrawDetail.getBankId(), ConstantsPay.QDPAY_BANK_ID ) ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "此代付无法支持的银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "此代付无法支持的银行类型：" + withdrawDetail.getBankId() );
        }
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merchantId", payAgentChannel.getMerId() );
        bodyMap.put( "orderNo", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "amount", withdrawDetail.getWithdrawMoney().setScale( 0, RoundingMode.HALF_UP ) );
        bodyMap.put( "walletAddress", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        bodyMap.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        log.warn( JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( payAgentPlatform.getOrderUrl(), HttpMethod.POST,
                    restTemplate.httpEntityCallback( packageJson( bodyMap ) ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );

            if ( e.getMessage().contains( "No route to host (Host unreachable)" ) ) {
                reqPayAgent.setFailReason( "代付IP需加白:" + e.getMessage() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
                return false;
            }
            reqPayAgent.setFailReason( e.getMessage() );
        }

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( dataMap ) ) {
                    log.info( payAgentPlatform.getName() + "订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                    int status = Integer.parseInt( dataMap.getOrDefault( "status", "0" ).toString() );
                    return status == 1 || status == 2;
                }
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );

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
        String orderNo = requestMap.getOrDefault( "orderNo", "" ).toString();
        String status    = requestMap.getOrDefault( "status", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( orderNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", orderNo );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 6 ) {
            log.error( "已有代付记录 - merOrderNo:{}", orderNo );
            return "success";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( orderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        // 去除空值
        requestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String mySign  = DigestUtils.md5Hex( signStr ).toUpperCase();
        if ( mySign.equalsIgnoreCase( sign ) ) {
            boolean isSuccess = "1".equals( status );
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderNo, payAgentChannel, isSuccess );
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
    public String queryOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                                 PayAgentPlatform payAgentPlatform ) throws Exception {
        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantId", payAgentChannel.getMerId() );
        params.put( "orderNo", withdrawDetail.getWithdrawOrderNo() );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        params.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        log.info( payAgentPlatform.getName()
                + "查询请求参数 - orderNo:{},request:{}", withdrawDetail.getWithdrawOrderNo(), JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageJson( params ), null );

        log.info( payAgentPlatform.getName()
                + "查询结果 - orderNo:{},result:{}", withdrawDetail.getWithdrawOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) && "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( dataMap ) ) {
                int orderState = Integer.parseInt( dataMap.getOrDefault( "status", 0 ).toString() );
                // status 4代付中5代付失败6代付成功
                // orderState (0处理失败，1处理成功，2处理中)
                int status = switch ( orderState ) {
                    case 1 -> 6;
                    case 0 -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "msg", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
