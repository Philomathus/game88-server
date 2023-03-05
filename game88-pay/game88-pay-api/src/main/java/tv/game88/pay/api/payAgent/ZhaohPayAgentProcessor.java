package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
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
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merchantNo", reqPayAgent.getWithdrawOrderNo() );
        bodyMap.put( "merchantCode", payAgentChannel.getMerId() );
        bodyMap.put( "userId", AESCoder.decrypt( payAgentChannel.getHeaderValue() ) );
        bodyMap.put( "channelGroup", payAgentChannel.getId() );
        bodyMap.put( "amount", withdrawDetail.getWithdrawMoney().setScale( 0, RoundingMode.HALF_UP ) );
        bodyMap.put( "coinUnit", "CNY" );
        bodyMap.put( "callbackUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "issueBankCode", withdrawDetail.getBankId() );
        bodyMap.put( "name", withdrawDetail.getBankUserName() );
        bodyMap.put( "bankNo", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "callbackDataFormat", "JSON" );
        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        bodyMap.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        String orderJson = JsonUtil.object2Json( bodyMap );
        log.info( "非对称加密加密前:" + orderJson );

        log.warn( JsonUtil.object2Json( bodyMap ) );
        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageJson( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String return_code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "0".equals( return_code ) ) {
                log.info( payAgentPlatform.getName() + "代付订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
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
        String               merchNo        = requestMap.getOrDefault( "merchNo", "" ).toString();
        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( merchNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", merchNo );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 6 ) {
            log.error( "已有代付记录 - merOrderNo:{}", merchNo );
            return "success";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( merchNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String signPrivateKey = AESCoder.decrypt( payAgentChannel.getSignPrivateKey() );
        String data           = RSACoder.decryptByPrivateKey( dataStr, signPrivateKey );
        log.info( data );
        Map<String, Object> resultMap = JsonUtil.json2Map( data );

        String orderNo    = resultMap.getOrDefault( "orderNo", "" ).toString();
        int    orderState = Integer.parseInt( resultMap.getOrDefault( "status", 1 ).toString() );

        // 解密后对签名验证
        SortedMap<String, Object> signMap = new TreeMap<>();
        signMap.put( "merchantNo", orderNo );
        signMap.put( "merchantCode", merchNo );
        signMap.put( "status", orderState );
        signMap.put( "amount", resultMap.get( "amount" ) );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = this.assemblyUrl( signMap ) + "&key=" + signMd5;
        log.info( signStr );
        String sign = DigestUtils.md5Hex( signStr );
        log.warn( sign + " : " + resultMap.get( "sign" ).toString() );
        if ( sign.equalsIgnoreCase( resultMap.get( "sign" ).toString() ) ) {
            if ( orderState == 2 ) {
                payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderNo, payAgentChannel, true );
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

        signMap.put( "merId", merId );
        signMap.put( "merOrderNo", merOrderNo );
        signMap.put( "code", "1001" );
        signMap.put( "message", "签名错误" );

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
    public String queryOrderPay( PayAgentLog payAgentLog ) throws Exception {
        MemberWithdrawDetail withdrawDetail   = withdrawDetailMapper.selectById( payAgentLog.getWithdrawOrderNo() );
        PayAgentChannel      payAgentChannel  = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );
        PayAgentPlatform     payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );

        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantNo", payAgentLog.getWithdrawOrderNo() );
        params.put( "merchantCode", payAgentLog.getChannelId() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        // 生成签名信息
        String signStr = this.assemblyUrl( params ) + signMd5;
        String sign    = DigestUtils.md5Hex( signStr );
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageJson( params ), null );

        log.info( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( dataMap ) ) {
                // orderState (1下发中、2下发完成、3下发失败)
                int orderState = Integer.parseInt( resultMap.getOrDefault( "status", 0 ).toString() );
                // status 4代付中 5代付失败 6代付成功
                int status = switch ( orderState ) {
                    case 2 -> 6;
                    case 3 -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status,
                        orderState );
            }
            return resultMap.getOrDefault( "message", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}