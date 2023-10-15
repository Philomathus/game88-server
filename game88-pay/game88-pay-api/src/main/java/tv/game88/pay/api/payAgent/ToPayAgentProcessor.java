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
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
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
import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.TO_PAY + "PayAgentProcessor" )
@Log4j2
public class ToPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "ToPay代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "sendid", payAgentChannel.getMerId() );
        dataMap.put( "orderid", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "amount", withdrawDetail.getWithdrawMoney() );
        dataMap.put( "address", withdrawDetail.getBankAccount().trim() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String tempStr =
                payAgentChannel.getMerId() + withdrawDetail.getWithdrawOrderNo() + withdrawDetail.getWithdrawMoney() + signMd5;
        String sign = DigestUtils.md5Hex( tempStr );
        dataMap.put( "sign", sign );

        log.warn( payAgentPlatform.getName() + "下单请求参数{}", JsonUtil.object2Json( dataMap ) );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity( dataMap, httpHeaders );

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
            if ( e.getMessage().contains( "failed to respond" ) ) {
                reqPayAgent.setFailReason( "三方网络异常:" + e.getMessage() );

                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
                return false;
            }
        }
        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "1".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map         data        = JsonUtil.json2Map( resultMap.getOrDefault( "data", "" ).toString() );
                String      id          = data.getOrDefault( "id", "" ).toString();
                PayAgentLog update      = new PayAgentLog();
                update.setWithdrawOrderNo( withdrawDetail.getWithdrawOrderNo() );
                update.setAgentOrderNo( id );
                payAgentLogMapper.updateById( update );

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
        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return null;
        }

        SortedMap<String, Object> signMap        = new TreeMap<>();
        SortedMap<String, Object> requestSignMap = new TreeMap<>( requestMap );
        String                    sign           = requestSignMap.remove( "sign" ).toString();

        String     merId      = requestSignMap.getOrDefault( "sendid", "" ).toString();
        String     merOrderNo = requestSignMap.getOrDefault( "orderid", "" ).toString();
        BigDecimal amount     = new BigDecimal( requestSignMap.getOrDefault( "amount", "0" ).toString() );
        String     address    = requestSignMap.getOrDefault( "address", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( merOrderNo );
        if ( withdrawDetail == null ) {
            signMap.put( "code", 1002 );
            signMap.put( "msg", "订单不存在" );
            return signMap;
        }
        PayAgentLog payAgentLog = payAgentLogMapper.selectById( merOrderNo );
        if ( payAgentLog == null ) {
            signMap.put( "code", 1002 );
            signMap.put( "msg", "订单不存在" );
            return signMap;
        }
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String tempSign = merId + merOrderNo + amount + address + AESCoder.decrypt( payAgentChannel.getSignPublicKey() );
        String mySign   = DigestUtils.md5Hex( tempSign );

        if ( org.apache.commons.lang3.StringUtils.equalsIgnoreCase( sign, mySign ) ) {
            if ( amount.compareTo( withdrawDetail.getWithdrawMoney() ) != 0 ) {
                signMap.put( "code", 1004 );
                signMap.put( "msg", "充币数量不匹配" );
                return signMap;
            } else if ( !merId.equals( payAgentChannel.getMerId() ) ) {
                signMap.put( "code", 9999 );
                signMap.put( "msg", "商户号错误" );
            } else {
                signMap.put( "code", 1 );
                signMap.put( "msg", "success" );
            }
        }
        String resultSignStr = sign + AESCoder.decrypt( payAgentChannel.getSignPrivateKey() );
        signMap.put( "retsign", DigestUtils.md5Hex( resultSignStr ) );
        return signMap;
    }

    @Override
    public String queryOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                                 PayAgentPlatform payAgentPlatform ) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( null, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( payAgentPlatform.getOrderQueryUrl() + "?id="
                    + withdrawDetail.getPayAgentOrderNo(), HttpMethod.GET, restTemplate.httpEntityCallback( httpEntity ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
            log.info( payAgentPlatform.getName()
                    + "查询结果 - 订单号:{} - result:{}", withdrawDetail.getWithdrawOrderNo(), JsonUtil.object2Json( resultMap ) );

            if ( !CollectionUtils.isEmpty( resultMap ) ) {
                String code = resultMap.getOrDefault( "code", "" ).toString();
                if ( "467".equals( code ) ) {
                    return resultMap.getOrDefault( "msg", "" ).toString();
                }

                //  status 4代付中 5代付失败 6代付成功
                int status = 4;
                //  statusCode 1-已创建,4-已转币,8-已取消,99-错误
                Map    data       = JsonUtil.json2Map( resultMap.getOrDefault( "data", "" ).toString() );
                String statusCode = data.getOrDefault( "state", "" ).toString();
                if ( !"1".equals( code ) ) {
                    statusCode = "99";
                }
                if ( "4".equals( statusCode ) || "8".equals( statusCode ) || "99".equals( statusCode ) ) {
                    if ( "4".equals( statusCode ) ) {
                        status = 6;
                    } else {
                        status = 5;
                    }
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
