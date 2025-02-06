package tv.game88.pay.api.payAgent;

import com.google.common.io.CharStreams;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RSACoder;
import tv.game88.core.config.entity.ConfigBankList;
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
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.A1_PAY + "PayAgentProcessor" )
@Log4j2
public class A1PayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "A1代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail memberWithdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();

        bodyMap.put( "apikey", payAgentChannel.getMerId() );
        bodyMap.put( "orderid", memberWithdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "money", memberWithdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );
        bodyMap.put( "alirealname", memberWithdrawDetail.getBankUserName() );
        bodyMap.put( "aliaccount", memberWithdrawDetail.getBankAccount() );
        bodyMap.put( "timestamp", System.currentTimeMillis() / 1000 );
        bodyMap.put( "callbackurl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "isbank", "1" );
        ConfigBankList configBank = configBankListCache.getConfigBank( memberWithdrawDetail.getBankId() );
        if ( configBank == null ) {
            payAgentService.callBackOrder( memberWithdrawDetail, payAgentChannel.getName() );
            log.warn( "未知银行类型 - 银行类型:{}", memberWithdrawDetail.getBankId() );
            throw new BusinessException( "未知银行类型：" + memberWithdrawDetail.getBankId() );
        }
        bodyMap.put( "bankname", configBank.getBankName() );

        String tempStr = this.assemblyUrl( bodyMap ) + "&secretkey=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        log.warn( tempStr );
        bodyMap.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        log.warn( JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName() + "下单结果- result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                log.info( payAgentPlatform.getName() + "代付订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
            }
        }
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        String sign = requestMap.remove( "sign" ).toString();

        String withdrawOrderId = requestMap.getOrDefault( "orderid", "" ).toString();
        String status          = requestMap.getOrDefault( "status", "" ).toString();

        MemberWithdrawDetail memberWithdrawDetail = withdrawDetailMapper.selectById( withdrawOrderId );
        if ( memberWithdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", withdrawOrderId );
            return "fail";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( withdrawOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String tempStr = this.assemblyUrl( bodyMap ) + "&secretkey=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = DigestUtils.md5Hex( tempStr );

        log.info( payAgentPlatform.getName() + "代付回调签名:" + sign + "_" + signStr );
        if ( sign.equalsIgnoreCase( signStr ) ) {

            if ( memberWithdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - merOrderNo:{}", withdrawOrderId );
                return "success";
            }
            boolean isSuccess = "1".equals( status );
            payAgentService.processOrderPay( memberWithdrawDetail, payAgentLog, requestMap.getOrDefault( "sysorderid", "" )
                    .toString(), payAgentChannel, isSuccess );
            log.info( payAgentPlatform.getName() + "订单号:{},回调状态:{},", withdrawOrderId, isSuccess ? "成功" : "失败" );
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
    public String queryOrderPay( MemberWithdrawDetail memberWithdrawDetail, PayAgentChannel payAgentChannel,
                                 PayAgentPlatform payAgentPlatform ) throws Exception {
        Map<String, String> dataMap = new TreeMap<>();
        dataMap.put( "apikey", payAgentChannel.getMerId() );
        dataMap.put( "timestamp", String.valueOf( System.currentTimeMillis() / 1000 ) );
        dataMap.put( "orderid", memberWithdrawDetail.getWithdrawOrderNo() );

        String tempStr = this.assemblyUrl( dataMap ) + "&secretkey=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );

        log.warn( tempStr );

        String sign = DigestUtils.md5Hex( tempStr ).toUpperCase();
        dataMap.put( "sign", sign );
        log.warn( payAgentPlatform.getName() + "查询代付状态接口请求参数{}", JsonUtil.object2Json( dataMap ) );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( dataMap );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( payAgentPlatform.getOrderQueryUrl() )
                .queryParams( requestMap ).build();

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null ),
                    response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = CharStreams.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String return_code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( return_code ) ) {
                String trade_state = resultMap.getOrDefault( "status", "" ).toString();

                if ( Arrays.asList( "0", "1", "2", "3", "4" ).contains( trade_state ) ) {
                    int status = switch ( trade_state ) {
                        case "1" -> 6;
                        case "3", "4" -> 5;
                        default -> 4;
                    };
                    payAgentService.processOrder( payAgentChannel, memberWithdrawDetail, memberWithdrawDetail.getUpdateTime(),
                            status );
                }
            }
            return resultMap.getOrDefault( "message", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + memberWithdrawDetail.getWithdrawOrderNo();
    }
}
