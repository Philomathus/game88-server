package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.core.config.entity.ConfigBankList;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Repository( value = ConstantsPayAgent.AFGHAN_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class AfghanPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "阿富汗代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "mchNo", payAgentChannel.getMerId() );
        bodyMap.put( "appId", AESCoder.decrypt( payAgentChannel.getHeaderValue() ) );
        bodyMap.put( "mchOrderNo", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "entryType", "BANK_CARD" );
        bodyMap.put( "amount", withdrawDetail
                .getWithdrawMoney()
                .multiply( new BigDecimal( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP ) );
        bodyMap.put( "currency", "cny" );
        bodyMap.put( "accountNo", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "accountName", withdrawDetail.getBankUserName().trim() );
        ConfigBankList configBank = configBankListCache.getConfigBank( withdrawDetail.getBankId() );
        if ( configBank == null ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "未知银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "未知银行类型：" + withdrawDetail.getBankId() );
        }
        bodyMap.put( "bankName", configBank.getBankName() );
        bodyMap.put( "clientIp", "127.0.0.1" );
        bodyMap.put( "transferDesc", "无" );
        bodyMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "reqTime", System.currentTimeMillis() );
        bodyMap.put( "version", "1.0" );
        bodyMap.put( "signType", "MD5" );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        log.warn( tempStr );
        bodyMap.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        log.warn( payAgentPlatform.getName() + "下单请求参数 - {}", JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageJson( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              retCode = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            if ( "0".equals( retCode ) && withdrawDetail.getWithdrawOrderNo().equals( dataMap.get( "mchOrderNo" ) ) ) {
                log.info( payAgentPlatform.getName() + "订单提交成功 - listResult:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( !CollectionUtils.isEmpty( dataMap ) ? dataMap
                        .getOrDefault( "errMsg", "" )
                        .toString() : resultMap.getOrDefault( "msg", "" ).toString() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName() + "订单提交失败 - result:{}", JsonUtil.object2Json( resultMap ) );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return "fail";
        }
        String sign       = requestMap.remove( "sign" ).toString();
        String mchOrderNo = requestMap.getOrDefault( "mchOrderNo", "" ).toString();
        String transferId = requestMap.getOrDefault( "transferId", "" ).toString();
        int    status     = Integer.parseInt( requestMap.getOrDefault( "state", "-1" ).toString() );

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( mchOrderNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", mchOrderNo );
            return "fail";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( mchOrderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String rspSign = DigestUtils.md5Hex( tempStr );

        log.info( payAgentPlatform.getName() + "回调签名:" + rspSign + "_" + sign );
        if ( sign.equalsIgnoreCase( rspSign ) ) {
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - merOrderNo:{}", mchOrderNo );
                return "success";
            }
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, transferId, payAgentChannel, status == 2 );
            log.info( payAgentPlatform.getName() + "订单号:{},回调状态:{},", mchOrderNo, status == 2 ? "成功" : "失败" );
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
        Map<String, Object> paramsMap = new TreeMap<>();
        paramsMap.put( "mchNo", payAgentChannel.getMerId() );
        paramsMap.put( "appId", AESCoder.decrypt( payAgentChannel.getHeaderValue() ) );
        paramsMap.put( "mchOrderNo", withdrawDetail.getWithdrawOrderNo() );
        paramsMap.put( "reqTime", System.currentTimeMillis() );
        paramsMap.put( "version", "1.0" );
        paramsMap.put( "signType", "MD5" );
        String tempStr = this.assemblyUrl( paramsMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        paramsMap.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageJson( paramsMap ), null );

        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              retCode = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            if ( "0".equals( retCode ) && !CollectionUtils.isEmpty( dataMap ) ) {
                int state = Integer.parseInt( dataMap.getOrDefault( "state", "-1" ).toString() );
                //  4代付中 5代付失败 6代付成功
                int status = switch ( state ) {
                    case 2 -> 6;
                    case 3, 4 -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "msg", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
