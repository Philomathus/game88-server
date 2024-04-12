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
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.constants.ConstantsPay;
import tv.game88.pay.api.dto.ReqVipPayDeposit;
import tv.game88.pay.api.dto.RspVipPayLogin;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.mapper.MemberRechargeOnlineMapper;
import tv.game88.pay.api.service.EmbeddedPayService;

import jakarta.annotation.Resource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@Log4j2
public class EmbeddedPayServiceImpl implements EmbeddedPayService {
    @Resource
    private MemberCardMapper           memberCardMapper;
    @Resource
    private MemberRechargeOnlineMapper memberRechargeOnlineMapper;
    @Resource
    private MemberInfoMapper           memberInfoMapper;
    @Resource
    private RestTemplate               restTemplate;
    @Resource
    private ConfigEnvCacheUtil         configEnvCacheUtil;
    @Resource
    private PayCacheUtil               payCacheUtil;

    @Value( "${spring.profiles.active}" )
    private String profile;

    @Override
    public RspBase<RspVipPayLogin> vipPayLogin( String memberId ) {
        // 68是vipPay的银行ID
        MemberCard memberCard = new QueryChainWrapper<>( memberCardMapper )
                .eq( "member_id", memberId )
                .eq( "bank_id", ConstantsPay.VIPPAY_BANK_ID )
                .one();
        // 24是vipPay支付平台ID
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( ConstantsPay.VIPPAY_PAY_PLATFORM_ID );

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
        log.info( "vipPay登录信息:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                Map<String, Object> result   = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
                String              token    = result.getOrDefault( "token", "" ).toString();
                Map<String, Object> userInfo = ( Map<String, Object> ) result.getOrDefault( "userInfo", new HashMap<>() );
                Map<String, Object> data     = ( Map<String, Object> ) result.getOrDefault( "data", new HashMap<>() );
                if ( StringUtils.isNotBlank( token ) && !CollectionUtils.isEmpty( userInfo )
                        && !CollectionUtils.isEmpty( data ) ) {
                    String     h5WebAddress  = data.getOrDefault( "h5WebAddress", "" ).toString();
                    String     walletAddress = userInfo.getOrDefault( "walletAddress", "" ).toString();
                    String     realName      = userInfo.getOrDefault( "realName", "" ).toString();
                    BigDecimal balance       = new BigDecimal( userInfo.getOrDefault( "balance", "0" ).toString() );

                    if ( StringUtils.isNotBlank( walletAddress ) ) {
                        if ( memberCard == null ) {
                            MemberCard newInsert = new MemberCard();
                            newInsert.setBankId( ConstantsPay.VIPPAY_BANK_ID );
                            newInsert.setMemberId( memberId );
                            newInsert.setBankAccount( walletAddress );
                            newInsert.setCreateTime( LocalDateTime.now() );
                            if ( StringUtils.isNotBlank( realName ) ) {
                                newInsert.setRealName( realName );
                            }
                            memberCardMapper.insert( newInsert );
                        } else if ( StringUtils.isNotBlank( realName ) ) {
                            MemberCard update = new MemberCard();
                            update.setId( memberCard.getId() );
                            update.setRealName( realName );
                            memberCardMapper.updateById( update );
                        }
                    }
                    RspVipPayLogin rspVipPayLogin = new RspVipPayLogin();
                    rspVipPayLogin.setBalance( balance.setScale( 2, RoundingMode.DOWN ) );
                    rspVipPayLogin.setWalletAddress( walletAddress );
                    rspVipPayLogin.setUrl( h5WebAddress + "?t=" + token );
                    return RspBase.ok( rspVipPayLogin );
                }
            } else {
                return RspBase.businessError( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        log.error( "vipPay登录失败 - 会员:{} - 钱包地址:{} - 结果:{}", memberId, reqMap.get( "walletAddress" ),
                JsonUtil.object2Json( resultMap ) );
        return RspBase.businessError( "vipPay登录失败,请重试" );
    }

    public static void main( String[] args ) {
        String d = "20000.7200";
        new BigDecimal( d );
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
                .eq( "bank_id", ConstantsPay.VIPPAY_BANK_ID )
                .one();
        if ( memberCard == null ) {
            return RspBase.businessError( "未注册vipPay,请登录后重试" );
        }
        String orderId = GenerateOrderCacheUtils.me.getOrderId( "P", 2 );
        // 先保存 MemberRechargeOnline
        MemberRechargeOnline memberRechargeOnline = new MemberRechargeOnline();
        memberRechargeOnline.setOrderNo( orderId );
        memberRechargeOnline.setMemberId( memberId );
        memberRechargeOnline.setPlatformId( ConstantsPay.VIPPAY_PAY_PLATFORM_ID );
        memberRechargeOnline.setMoney( reqVipPayDeposit.getAmount() );
        memberRechargeOnline.setFirst( false );
        memberRechargeOnline.setPayTime( LocalDateTime.now() );
        memberRechargeOnline.setStatus( -1 );
        memberRechargeOnline.setRate( configEnvCacheUtil.getConfBd( "vippay_platform_rate" ) );
        memberRechargeOnline.setUpdateTime( memberRechargeOnline.getPayTime() );
        int i = memberRechargeOnlineMapper.insert( memberRechargeOnline );
        if ( i <= 0 ) {
            return RspBase.businessError( "新建充值订单失败,请重试" );
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( ConstantsPay.VIPPAY_PAY_PLATFORM_ID );

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

        log.warn( "memberId:{} walletAddr:{} result:{}", memberId, memberCard.getBankAccount(),
                JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( result ) ) {
                    return RspBase.ok( "请求成功,请前往vipPay支付中心确认", result.getOrDefault( "redirectUrl", "" ).toString() );
                }
            } else {
                return RspBase.businessError( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return RspBase.businessError( "访问vipPay支付中心失败,请重试或者联系客服" );
    }

    @Override
    public RspBase<RspVipPayLogin> qdPayLogin( String memberId ) {
        MemberCard memberCard = new QueryChainWrapper<>( memberCardMapper )
                .eq( "member_id", memberId )
                .eq( "bank_id", ConstantsPay.QDPAY_BANK_ID )
                .one();
        // 50是vipPay支付平台ID
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( ConstantsPay.QDPAY_PAY_PLATFORM_ID );
        String      userPhone   = memberInfoMapper.getUserPhone( memberId );
        log.info( "user phone number {} " , userPhone );
        if ( StringUtils.isBlank( userPhone ) ) {
            return RspBase.businessError( "请绑定手机后登录" );
        }

        Map<String, Object> reqMap = new TreeMap<>();
        reqMap.put( "merchantId", payPlatform.getMerId() );
        reqMap.put( "phone", userPhone );
        if ( memberCard != null ) {
            reqMap.put( "walletAddress", memberCard.getBankAccount().trim() );
        }
        if ( memberCard != null && StringUtils.isNotBlank( memberCard.getRealName() ) ) {
            reqMap.put( "realName", memberCard.getRealName() );
        }
        reqMap.put( "userId", profile + "_" + memberId );
        String signTemp = this.assemblyUrl( reqMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        reqMap.put( "sign", DigestUtils.md5Hex( signTemp ).toUpperCase() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( reqMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( payPlatform.getPayUrl()
                + "/api/common/embeddedLogin", HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.info( "QDPay登录信息:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                String              token  = result.getOrDefault( "token", "" ).toString();
                if ( StringUtils.isNotBlank( token ) ) {
                    String     walletAddress = result.getOrDefault( "walletAddress", "" ).toString();
                    String     url           = result.getOrDefault( "url", "" ).toString();
                    Object     realNameObj   = result.get( "realName" );
                    String     realName      = realNameObj == null ? "" : realNameObj.toString();
                    Object     balanceObj    = result.get( "balance" );
                    BigDecimal balance       = new BigDecimal( balanceObj == null ? "0" : balanceObj.toString() );

                    if ( StringUtils.isNotBlank( walletAddress ) ) {
                        if ( memberCard == null ) {
                            MemberCard newInsert = new MemberCard();
                            newInsert.setBankId( ConstantsPay.QDPAY_BANK_ID );
                            newInsert.setMemberId( memberId );
                            newInsert.setBankAccount( walletAddress );
                            newInsert.setCreateTime( LocalDateTime.now() );
                            if ( StringUtils.isNotBlank( realName ) ) {
                                newInsert.setRealName( realName );
                            }
                            memberCardMapper.insert( newInsert );
                        } else if ( StringUtils.isNotBlank( realName ) ) {
                            MemberCard update = new MemberCard();
                            update.setId( memberCard.getId() );
                            update.setRealName( realName );
                            memberCardMapper.updateById( update );
                        }
                    }
                    RspVipPayLogin rspVipPayLogin = new RspVipPayLogin();
                    rspVipPayLogin.setUrl( url );
                    rspVipPayLogin.setBalance( balance );
                    rspVipPayLogin.setWalletAddress( walletAddress );
                    rspVipPayLogin.setToken( token );
                    return RspBase.ok( rspVipPayLogin );
                }
            } else {
                return RspBase.businessError( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        log.error( "QDPay登录失败 - 会员:{} - 钱包地址:{} - 结果:{}", memberId, reqMap.get( "walletAddress" ),
                JsonUtil.object2Json( resultMap ) );
        return RspBase.businessError( "QDPay登录失败,请重试" );
    }

    @Override
    public RspBase<?> qdPayDeposit( ReqVipPayDeposit reqVipPayDeposit, String memberId ) {
        if ( reqVipPayDeposit.getAmount().compareTo( BigDecimal.TEN ) < 0 ) {
            return RspBase.businessError( "充值金额最低10" );
        }
        MemberCard memberCard = new QueryChainWrapper<>( memberCardMapper )
                .eq( "member_id", memberId )
                .eq( "bank_id", ConstantsPay.QDPAY_BANK_ID )
                .one();
        if ( memberCard == null ) {
            return RspBase.businessError( "未注册QDPay,请登录后重试" );
        }
        String orderId = GenerateOrderCacheUtils.me.getOrderId( "P", 2 );
        // 先保存 MemberRechargeOnline
        MemberRechargeOnline memberRechargeOnline = new MemberRechargeOnline();
        memberRechargeOnline.setOrderNo( orderId );
        memberRechargeOnline.setMemberId( memberId );
        memberRechargeOnline.setPlatformId( ConstantsPay.QDPAY_PAY_PLATFORM_ID );
        memberRechargeOnline.setMoney( reqVipPayDeposit.getAmount() );
        memberRechargeOnline.setFirst( false );
        memberRechargeOnline.setPayTime( LocalDateTime.now() );
        memberRechargeOnline.setStatus( -1 );
        memberRechargeOnline.setRate( configEnvCacheUtil.getConfBd( "qdpay_platform_rate" ) );
        memberRechargeOnline.setUpdateTime( memberRechargeOnline.getPayTime() );
        int i = memberRechargeOnlineMapper.insert( memberRechargeOnline );
        if ( i <= 0 ) {
            return RspBase.businessError( "新建充值订单失败,请重试" );
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( ConstantsPay.QDPAY_PAY_PLATFORM_ID );

        Map<String, Object> reqMap = new TreeMap<>();
        reqMap.put( "merchantId", payPlatform.getMerId() );
        reqMap.put( "orderNo", orderId );
        reqMap.put( "amount", reqVipPayDeposit.getAmount() );
        reqMap.put( "walletAddress", memberCard.getBankAccount().trim() );
        reqMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        String signTemp = this.assemblyUrl( reqMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        reqMap.put( "sign", DigestUtils.md5Hex( signTemp ).toUpperCase() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( reqMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( payPlatform.getPayUrl()
                + "/api/common/depositOrder", HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.warn( "memberId:{} walletAddr:{} result:{}", memberId, memberCard.getBankAccount(),
                JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( result ) ) {
                    return RspBase.ok( "请求成功,请前往QDPay支付中心确认", result.getOrDefault( "payUrl", "" ).toString() );
                }
            } else {
                return RspBase.businessError( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return RspBase.businessError( "访问QDPay支付中心失败,请重试或者联系客服" );
    }
}
