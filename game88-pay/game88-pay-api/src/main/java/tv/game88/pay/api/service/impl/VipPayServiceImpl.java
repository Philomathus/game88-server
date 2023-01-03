package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.mapper.MemberCardMapper;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.dto.ReqVipPayDeposit;
import tv.game88.pay.api.dto.RspVipPayLogin;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.mapper.MemberRechargeOnlineMapper;
import tv.game88.pay.api.service.VipPayService;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@Log4j2
public class VipPayServiceImpl implements VipPayService {
    @Resource
    private MemberCardMapper           memberCardMapper;
    @Resource
    private MemberRechargeOnlineMapper memberRechargeOnlineMapper;
    @Resource
    private RestTemplate               restTemplate;
    @Resource
    private ConfigEnvCacheUtil         configEnvCacheUtil;
    @Resource
    private PayCacheUtil               payCacheUtil;

    @Value( "${spring.profiles.active}" )
    private String profile;

    private static final Long VIPPAY_BANK_ID         = 68L;
    private static final Long VIPPAY_PAY_PLATFORM_ID = 24L;

    @Override
    public RspBase<RspVipPayLogin> vipPayLogin( String memberId ) {
        // 68是vipPay的银行ID
        MemberCard memberCard = new QueryChainWrapper<>( memberCardMapper )
                .eq( "member_id", memberId )
                .eq( "bank_id", VIPPAY_BANK_ID )
                .one();
        // 24是vipPay支付平台ID
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( VIPPAY_PAY_PLATFORM_ID );

        Map<String, Object> reqMap = new TreeMap<>();
        reqMap.put( "merchantNo", payPlatform.getMerId() );
        reqMap.put( "account", profile + "_" + memberId );
        if ( memberCard != null ) {
            reqMap.put( "walletAddress", memberCard.getBankAccount().trim() );
        }
        String signTemp = this.assemblyUrl( reqMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        reqMap.put( "sign", DigestUtils.md5Hex( signTemp ).toUpperCase() );
        if ( memberCard == null ) {
            reqMap.put( "walletAddress", "" );
        }

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( reqMap );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( payPlatform.getPayUrl()
                + "/Api/Covert/Account/login", HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( result ) ) {
                    String              token    = result.getOrDefault( "token", "" ).toString();
                    Map<String, Object> userInfo = ( Map<String, Object> ) result.getOrDefault( "userInfo", new HashMap<>() );
                    Map<String, Object> data     = ( Map<String, Object> ) result.getOrDefault( "data", new HashMap<>() );
                    if ( StringUtils.isNotBlank( token ) && !CollectionUtils.isEmpty( userInfo )
                            && !CollectionUtils.isEmpty( data ) ) {
                        String     h5WebAddress  = data.getOrDefault( "h5WebAddress", "" ).toString();
                        String     walletAddress = userInfo.getOrDefault( "walletAddress", "" ).toString();
                        BigDecimal balance       = new BigDecimal( userInfo.getOrDefault( "balance", "0" ).toString() );

                        if ( memberCard == null && StringUtils.isNotBlank( walletAddress ) ) {
                            MemberCard newInsert = new MemberCard();
                            newInsert.setBankId( VIPPAY_BANK_ID );
                            newInsert.setMemberId( memberId );
                            newInsert.setBankAccount( walletAddress );
                            newInsert.setCreateTime( LocalDateTime.now() );
                            memberCardMapper.insert( newInsert );
                        }
                        RspVipPayLogin rspVipPayLogin = new RspVipPayLogin();
                        rspVipPayLogin.setBalance( balance );
                        rspVipPayLogin.setWalletAddress( walletAddress );
                        rspVipPayLogin.setUrl( h5WebAddress + "?t=" + token );
                        return RspBase.ok( rspVipPayLogin );
                    }
                }
            }
        }
        log.error( "vipPay登录失败结果:{}", JsonUtil.object2Json( resultMap ) );
        return RspBase.businessError( "vipPay登录失败,请重试" );
    }

    private String assemblyUrl( Map<String, ?> bodyMap ) {
        //字典顺序a----z
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }

    @Override
    public RspBase<?> vipPayDeposit( ReqVipPayDeposit reqVipPayDeposit, String memberId ) {
        if ( reqVipPayDeposit.getAmount().compareTo( BigDecimal.TEN ) < 0 ) {
            return RspBase.businessError( "充值金额最低10" );
        }
        MemberCard memberCard = new QueryChainWrapper<>( memberCardMapper )
                .eq( "member_id", memberId )
                .eq( "bank_id", VIPPAY_BANK_ID )
                .one();
        if ( memberCard == null ) {
            return RspBase.businessError( "未注册vipPay,请登录后重试" );
        }
        String orderId = GenerateOrderCacheUtils.me.getOrderId( "P", 2 );
        // 先保存 MemberRechargeOnline
        MemberRechargeOnline memberRechargeOnline = new MemberRechargeOnline();
        memberRechargeOnline.setOrderNo( orderId );
        memberRechargeOnline.setMemberId( memberId );
        memberRechargeOnline.setPlatformId( VIPPAY_PAY_PLATFORM_ID );
        memberRechargeOnline.setMoney( reqVipPayDeposit.getAmount() );
        memberRechargeOnline.setFirst( false );
        memberRechargeOnline.setPayTime( LocalDateTime.now() );
        memberRechargeOnline.setStatus( -1 );
        memberRechargeOnline.setRate( new BigDecimal( "0.01" ) );
        memberRechargeOnline.setUpdateTime( memberRechargeOnline.getPayTime() );
        int i = memberRechargeOnlineMapper.insert( memberRechargeOnline );
        if ( i <= 0 ) {
            return RspBase.businessError( "新建充值订单失败,请重试" );
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( VIPPAY_PAY_PLATFORM_ID );

        Map<String, Object> reqMap = new TreeMap<>();
        reqMap.put( "merchantNo", payPlatform.getMerId() );
        reqMap.put( "depositNo", orderId );
        reqMap.put( "account", profile + "_" + memberId );
        reqMap.put( "amount", reqVipPayDeposit.getAmount() );
        reqMap.put( "walletAddress", memberCard.getBankAccount().trim() );
        reqMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        String signTemp = this.assemblyUrl( reqMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        reqMap.put( "sign", DigestUtils.md5Hex( signTemp ).toUpperCase() );

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( reqMap );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( payPlatform.getPayUrl()
                + "/Api/Covert/WalletDeposit", HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) && "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                return RspBase.ok( "请求成功,请前往vipPay支付中心确认", result.getOrDefault( "redirectUrl", "" ).toString() );
            }
        }
        return RspBase.businessError( "访问vipPay支付中心失败,请重试或者联系客服" );
    }
}
