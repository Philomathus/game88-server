package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.SmsPhoneCacheUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.member.cache.ConfigVipCacheUtils;
import tv.game88.core.member.dto.*;
import tv.game88.core.member.entity.*;
import tv.game88.core.member.enums.EnumDev;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.LogMoneyMapper;
import tv.game88.core.member.mapper.MemberBcodeMapper;
import tv.game88.core.member.mapper.MemberCardMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.core.utils.SmsApi;
import tv.game88.platform.api.dto.*;
import tv.game88.platform.api.entity.MemberVipGift;
import tv.game88.platform.api.entity.MobileLimit;
import tv.game88.platform.api.mapper.MemberVipGiftMapper;
import tv.game88.platform.api.mapper.MobileLimitMapper;
import tv.game88.platform.api.service.MemberInfoService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Log4j2
@Service( "memberInfoService" )
public class MemberInfoServiceImpl extends ServiceImpl<MemberInfoMapper, MemberInfo> implements MemberInfoService {
    @Resource
    private RedisUtils            redisUtils;
    @Resource
    private ConfigEnvCacheUtil    configEnvCacheUtil;
    @Resource
    private RestTemplate          restTemplate;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private SmsPhoneCacheUtil     smsPhoneCacheUtil;
    @Resource
    private ConfigVipCacheUtils   configVipCacheUtils;
    @Resource
    private SmsApi                smsApi;
    @Resource
    private MemberCardMapper      memberCardMapper;
    @Resource
    private MemberBcodeMapper     memberBcodeMapper;
    @Resource
    private MemberVipGiftMapper   memberVipGiftMapper;
    @Resource
    private MemberMoneyManager    memberMoneyManager;
    @Resource
    private LogMoneyMapper        logMoneyMapper;
    @Resource
    private MobileLimitMapper     mobileLimitMapper;
    @Resource
    private MemberInfoMapper      memberInfoMapper;

    @Value( "${im.tokenUrl:null}" )
    private String getImTokenUrl;
    @Value( "${spring.profiles.active}" )
    private String profile;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public RspInit getLoginInit( Integer dev, String version ) {
        RspInit      res  = new RspInit();
        List<Object> keys = new ArrayList<>( Arrays.asList( "customer_url", "customer_url2", "web_url", "star_pic" ) );

        if ( dev == EnumDev.IOS.getType() ) {
            keys.addAll( Arrays.asList( "ios_version", "ios_force_update", "ios_down_url", "ios_update_text" ) );
        } else {
            keys.addAll( Arrays.asList( "android_version", "android_force_update", "android_down_url", "android_update_text" ) );
        }
        keys.addAll( Arrays.asList( "163action_captchaId", "163action_switch", "163action_Product_id", "first_recharge_url" ,
                "app_link","app_link_tech_spark") );
        List<String> valueList = configEnvCacheUtil.getConf( keys );
        res.setCustomerUrl( valueList.get( 0 ) );
        res.setCustomerUrl2( valueList.get( 1 ) );
        res.setWebUrl( valueList.get( 2 ) );
        String starPic = valueList.get( 3 );
        res.setStarPic( starPic == null ? "" : starPic );

        String latestVersion = valueList.get( 4 );
        res.setLatestVersion( latestVersion );
        res.setHasNew( AppVersionUtils.hasNewVersion( version, latestVersion ) );

        res.setLatestFore( valueList.get( 5 ) );
        res.setDownUrl( valueList.get( 6 ) );
        res.setUpdateText( valueList.get( 7 ) );

        res.setCaptchaId( valueList.get( 8 ) );
        res.setActionSwitch( valueList.get( 9 ) );
        res.setProductId( valueList.get( 10 ) );
        res.setFirstRechargeUrl( valueList.get( 11 ) );
        res.setAppLink( valueList.get( 12 ) );
        res.setAppLinkTechSpark( valueList.get( 13 ) );
        return res;
    }

    @Override
    public RspManUpdateVersion checkManUpdateVersion( Integer dev, String version ) {
        RspManUpdateVersion rsp  = new RspManUpdateVersion();
        List<Object>        keys = new ArrayList<>( 3 );
        if ( dev == EnumDev.IOS.getType() ) {
            keys.addAll( Arrays.asList( "ios_man_version", "ios_update_content", "ios_man_version_url" ) );
        } else {
            keys.addAll( Arrays.asList( "android_man_version", "android_update_content", "android_man_version_url" ) );
        }
        List<String> valueList = configEnvCacheUtil.getConf( keys );

        String manVersion = valueList.get( 0 );
        rsp.setManVersion( StringUtils.isBlank( manVersion ) ? "3.8.11.1" : manVersion );
        //更新内容
        rsp.setUpdateContent( valueList.get( 1 ) );
        if ( AppVersionUtils.hasNewVersion( version, rsp.getManVersion() ) ) {
            rsp.setDownUrl( valueList.get( 2 ) );
        }
        return rsp;
    }

    @Override
    public RspBase<RspMember> login( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) {
        if ( StringUtils.isBlank( mobileLogin.getMobile() ) ) {
            return RspBase.businessError( "请输入手机号码" );
        }
        if ( mobileLogin.getMobile().length() != 11 ) {
            return RspBase.businessError( "请输入正确的手机号" );
        }
        if ( StringUtils.isBlank( mobileLogin.getPasswd() ) ) {
            return RspBase.businessError( "请输入登陆密码" );
        }

        if ( configEnvCacheUtil.getConfBool( "163action_switch" ) ) {
            //行为式验证码校验
            if ( !verification( mobileLogin.getValidate() ) ) {
                return RspBase.businessError( "验证不通过" );
            }
        }

        MemberInfo memberInfo  = new QueryChainWrapper<>( this.baseMapper ).eq( "phone", mobileLogin.getMobile() ).one();
        MemberInfo oldm        = null;
        boolean    isNewMember = false;
        if ( memberInfo == null ) {
            //检查是不是归档会员回归
            oldm = this.baseMapper.findMemberHistoryByMobile( mobileLogin.getMobile() );
            if ( oldm == null ) {
                return RspBase.businessError( "手机号不存在/密码错误" );
            }
            memberInfo = oldm;
        }
        if ( memberInfo.getStatus() == 0 ) {
            return RspBase.businessError( "您被限制登录,请联系客服" );
        }

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken( memberInfo.getId(), mobileLogin.getPasswd() );
            AuthContextHolderUtils.setContext( authenticationToken );
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authenticationManager.authenticate( authenticationToken );
        } catch ( Exception e ) {
            if ( e instanceof BadCredentialsException ) {
                log.error( "密码错误:{} ", JsonUtil.object2Json( mobileLogin ) );
            } else {
                log.error( e.getMessage(), e );
            }
            AuthContextHolderUtils.clearContext();
            return RspBase.businessError( "手机号不存在/密码错误" );
        } finally {
            AuthContextHolderUtils.clearContext();
        }

        String ip = ServletUtil.getIp();
        log.info( "会员{}手机号密码登录IP:{}", memberInfo.getId(), ip );

        MemberInfo update = new MemberInfo();
        update.setId( memberInfo.getId() );
        update.setDeviceId( mobileLogin.getDeviceId() );
        this.setMemberLoginParam( mobileLogin, dev, version, loginUrl, memberInfo.getLoginProvince(), update );

        if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getMobile(), 5 ) ) {
            return RspBase.businessError( "请勿重复登录" );
        }

        if ( oldm != null ) {
            this.baseMapper.insert( memberInfo );
            this.baseMapper.deleteByHistoryKey( oldm.getId() );
            isNewMember = true;
        } else {
            this.baseMapper.updateById( update );
        }

        RspMember rspMember = new RspMember();
        rspMember.setNewAccount( isNewMember );
        BeanUtils.copyProperties( memberInfo, rspMember );
        setNextLevelIntegral( memberInfo, rspMember );
        return RspBase.ok( "登录成功", rspMember );
    }

    @Override
    public RspBase<RspMember> loginDevice( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) {
        if ( StringUtils.isBlank( mobileLogin.getDeviceId() ) ) {
            return RspBase.businessError( "设备号不能为空" );
        }
        String login_restrict_ip = configEnvCacheUtil.getConf( "login_restrict_ip", null );
        if ( StringUtils.isNotBlank( mobileLogin.getIp() ) && StringUtils.isNotBlank( login_restrict_ip ) ) {
            for ( String dip : login_restrict_ip.split( "," ) ) {
                if ( mobileLogin.getIp().equals( dip ) ) {
                    return RspBase.businessError( "您已被限制登录,请联系客服" );
                }
            }
        }
        if ( configEnvCacheUtil.getConfBool( "163action_switch" ) ) {
            //行为式验证码校验
            if ( !verification( mobileLogin.getValidate() ) ) {
                return RspBase.businessError( "验证不通过" );
            }
        }

        boolean isNewMember = false;

        //设备号查询
        MemberInfo memberInfo = this.baseMapper.findMemberByDeviceId( mobileLogin.getDeviceId() );
        if ( memberInfo != null ) {
            if ( memberInfo.getStatus() == 0 ) {
                return RspBase.businessError( "您已被限制登录,请联系客服" );
            }
            if ( StringUtils.isNotBlank( memberInfo.getPhone() ) ) {
                return RspBase.businessError( "该设备已绑定手机号，请使用手机号登录" );
            }
            MemberInfo update = new MemberInfo();
            update.setId( memberInfo.getId() );

            this.setMemberLoginParam( mobileLogin, dev, version, loginUrl, memberInfo.getLoginProvince(), update );

            this.baseMapper.updateById( update );
        } else {
            //检查是不是归档会员回归
            MemberInfo oldm = this.baseMapper.findMemberHistoryByDeviceId( mobileLogin.getDeviceId() );
            if ( oldm == null ) {
                memberInfo = this.newMemberInfoReg( mobileLogin );
                memberInfo.setRegisterType( 0 );
                isNewMember = true;
            } else {
                if ( oldm.getStatus() == 0 ) {
                    return RspBase.businessError( "您已被限制登录,请联系客服" );
                }
                if ( !StringUtil.isEmpty( oldm.getPhone() ) ) {
                    return RspBase.businessError( "该设备已绑定手机号，请使用手机号登录" );
                }
                memberInfo = oldm;
            }
            this.setMemberLoginParam( mobileLogin, dev, version, loginUrl, memberInfo.getLoginProvince(), memberInfo );

            if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getDeviceId(), 5 ) ) {
                return RspBase.businessError( "请勿重复登录" );
            }

            this.baseMapper.insert( memberInfo );
            if ( oldm != null ) {
                this.baseMapper.deleteByHistoryKey( oldm.getId() );
            } else {
                //渠道邀请码注册通知(归档会员回归不通知)
                regChannelNotice( mobileLogin, dev, memberInfo.getId() );
            }
        }
        RspMember rspMember = new RspMember();
        rspMember.setNewAccount( isNewMember );
        BeanUtils.copyProperties( memberInfo, rspMember );
        return RspBase.ok( "登录成功", rspMember );
    }

    //行为校验
    private boolean verification( String validate ) {
        Map<String, String> params = new TreeMap<>();
        params.put( "captchaId", configEnvCacheUtil.getConf( "163action_captchaId" ) );
        params.put( "validate", validate );
        params.put( "user", "qwer" );
        params.put( "secretId", configEnvCacheUtil.getConf( "163action_secretId" ) );
        params.put( "version", "v2" );
        params.put( "timestamp", String.valueOf( System.currentTimeMillis() ) );
        params.put( "nonce", IdWorker.get32UUID() );
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = params.keySet().toArray( new String[ 0 ] );
        Arrays.sort( keys );
        // 2. 按照排序拼接参数名与参数值
        StringBuilder sb = new StringBuilder();
        for ( String key : keys ) {
            sb.append( key ).append( params.get( key ) );
        }
        // 3. 将secretKey拼接到最后
        sb.append( configEnvCacheUtil.getConf( "163action_secretkey" ) );
        String sign = DigestUtils.md5Hex( sb.toString().getBytes( StandardCharsets.UTF_8 ) );
        params.put( "signature", sign );
        log.warn( JsonUtil.object2Json( params ) );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( params );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.postForObject( configEnvCacheUtil.getConf( "163action_url" ), httpEntity, Map.class );
        } catch ( RestClientException e ) {
            log.warn( "会员行为验证失败validate:{},msg:{}", validate, e.getMessage() );
        }
        log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            return ( boolean ) resultMap.get( "result" );
        }
        return false;
    }

    @Override
    public RspBase<RspMember> register( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) throws Exception {
        if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getMobile(), 5 ) ) {
            return RspBase.businessError( "请勿重复注册" );
        }
        String login_restrict_ip = configEnvCacheUtil.getConf( "login_restrict_ip", null );
        if ( StringUtils.isNotBlank( mobileLogin.getIp() ) && StringUtils.isNotBlank( login_restrict_ip ) ) {
            for ( String dip : login_restrict_ip.split( "," ) ) {
                if ( mobileLogin.getIp().equals( dip ) ) {
                    return RspBase.businessError( "您已被限制登录,请联系客服" );
                }
            }
        }
        if ( StringUtils.isBlank( mobileLogin.getPasswd() ) ) {
            return RspBase.businessError( "请输入登陆密码" );
        }
        RspBase rspBase = this.verificationPhoneCode( mobileLogin.getMobile(), mobileLogin.getCode() );
        if ( rspBase != null ) {
            return rspBase;
        }
        boolean    isNewMember = false;
        MemberInfo memberInfo  = new QueryChainWrapper<>( this.baseMapper ).eq( "phone", mobileLogin.getMobile() ).one();
        if ( memberInfo != null ) {
            return RspBase.businessError( "您已注册过该手机号,请勿重复注册" );
        }
        MemberInfo oldm = null;
        //检查是不是归档会员回归
        oldm = this.baseMapper.findMemberHistoryByMobile( mobileLogin.getMobile() );
        if ( oldm != null ) {
            return RspBase.businessError( "您已注册过该手机号,请勿重复注册" );
        }
        //检查有没有相同设备号
        memberInfo = this.baseMapper.findMemberByDeviceId( mobileLogin.getDeviceId() );
        if ( memberInfo != null ) {
            MemberInfo update = new MemberInfo();
            update.setId( memberInfo.getId() );
            update.setPassword( mobileLogin.getPasswordEncrypt() );
            update.setPhone( mobileLogin.getMobile() );

            this.setMemberLoginParam( mobileLogin, dev, version, loginUrl, memberInfo.getLoginProvince(), update );

            this.baseMapper.updateById( update );
        } else {
            memberInfo = this.newMemberInfoReg( mobileLogin );
            memberInfo.setRegisterType( 1 );

            this.setMemberLoginParam( mobileLogin, dev, version, loginUrl, memberInfo.getLoginProvince(), memberInfo );
            isNewMember = true;
            this.baseMapper.insert( memberInfo );
            try {
                //渠道邀请码注册通知(归档会员回归不通知)
                regChannelNotice( mobileLogin, dev, memberInfo.getId() );
            } catch ( Exception e ) {
                try {
                    regChannelNotice( mobileLogin, dev, memberInfo.getId() );
                } catch ( Exception p ) {
                    log.error( "反作弊注册成功，通知推广渠道失败 account:{},errMsg:{}", memberInfo.getId(), p.getMessage() );
                }
            }
        }
        RspMember rspMember = new RspMember();
        rspMember.setNewAccount( isNewMember );
        BeanUtils.copyProperties( memberInfo, rspMember );
        return RspBase.ok( "注册成功", rspMember );
    }

    @Override
    public RspBase<RspMember> usernameRegister( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) {
        if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getUsername(), 5 ) ) {
            return RspBase.businessError( "请勿重复注册" );
        }
        if ( mobileLogin.getUsername().length() < 6 || mobileLogin.getUsername().length() > 25 ) {
            return RspBase.businessError( "用户名长度为6-25位" );
        }
        String login_restrict_ip = configEnvCacheUtil.getConf( "login_restrict_ip", null );
        if ( StringUtils.isNotBlank( mobileLogin.getIp() ) && StringUtils.isNotBlank( login_restrict_ip ) ) {
            for ( String dip : login_restrict_ip.split( "," ) ) {
                if ( mobileLogin.getIp().equals( dip ) ) {
                    return RspBase.businessError( "您已被限制登录,请联系客服" );
                }
            }
        }
        if ( StringUtils.isBlank( mobileLogin.getPasswd() ) ) {
            return RspBase.businessError( "请输入登陆密码" );
        }
        if ( configEnvCacheUtil.getConfBool( "163action_switch" ) ) {
            //行为式验证码校验
            if ( !verification( mobileLogin.getValidate() ) ) {
                return RspBase.businessError( "验证不通过" );
            }
        }
        MemberInfo memberInfo = new QueryChainWrapper<>( this.baseMapper ).eq( "nick_name", mobileLogin.getUsername() ).one();
        if ( memberInfo != null ) {
            return RspBase.businessError( "用户名已存在" );
        }
        //检查是不是归档会员回归
        MemberInfo oldm = this.baseMapper.findMemberHistoryByUsername( mobileLogin.getUsername() );
        if ( oldm != null ) {
            return RspBase.businessError( "用户名已存在" );
        }
        memberInfo = this.newMemberInfoReg( mobileLogin );
        memberInfo.setNickName( mobileLogin.getUsername() );
        memberInfo.setRegisterType( 1 );

        this.setMemberLoginParam( mobileLogin, dev, version, loginUrl, memberInfo.getLoginProvince(), memberInfo );

        this.baseMapper.insert( memberInfo );
        try {
            //渠道邀请码注册通知(归档会员回归不通知)
            regChannelNotice( mobileLogin, dev, memberInfo.getId() );
        } catch ( Exception e ) {
            try {
                regChannelNotice( mobileLogin, dev, memberInfo.getId() );
            } catch ( Exception p ) {
                log.error( "通知推广渠道失败 account:{},errMsg:{}", memberInfo.getId(), p.getMessage() );
            }
        }
        RspMember rspMember = new RspMember();
        rspMember.setNewAccount( true );
        BeanUtils.copyProperties( memberInfo, rspMember );
        return RspBase.ok( "注册成功", rspMember );
    }

    @Override
    public RspBase<RspMember> usernameLogin( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) {
        if ( StringUtils.isBlank( mobileLogin.getUsername() ) ) {
            return RspBase.businessError( "请输入用户名" );
        }
        if ( mobileLogin.getUsername().length() < 6 || mobileLogin.getUsername().length() > 25 ) {
            return RspBase.businessError( "用户名长度为6-25位" );
        }
        if ( StringUtils.isBlank( mobileLogin.getPasswd() ) ) {
            return RspBase.businessError( "请输入登陆密码" );
        }

        if ( configEnvCacheUtil.getConfBool( "163action_switch" ) ) {
            //行为式验证码校验
            if ( !verification( mobileLogin.getValidate() ) ) {
                return RspBase.businessError( "验证不通过" );
            }
        }

        MemberInfo memberInfo  = new QueryChainWrapper<>( this.baseMapper ).eq( "nick_name", mobileLogin.getUsername() ).one();
        MemberInfo oldm        = null;
        boolean    isNewMember = false;
        if ( memberInfo == null ) {
            //检查是不是归档会员回归
            oldm = this.baseMapper.findMemberHistoryByMobile( mobileLogin.getMobile() );
            if ( oldm == null ) {
                return RspBase.businessError( "用户名不存在/密码错误" );
            }
            memberInfo = oldm;
        }
        if ( memberInfo.getStatus() == 0 ) {
            return RspBase.businessError( "您被限制登录,请联系客服" );
        }

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken( memberInfo.getId(), mobileLogin.getPasswd() );
            AuthContextHolderUtils.setContext( authenticationToken );
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authenticationManager.authenticate( authenticationToken );
        } catch ( Exception e ) {
            if ( e instanceof BadCredentialsException ) {
                log.error( "密码错误:{} ", JsonUtil.object2Json( mobileLogin ) );
            } else {
                log.error( e.getMessage(), e );
            }
            AuthContextHolderUtils.clearContext();
            return RspBase.businessError( "用户名不存在/密码错误" );
        } finally {
            AuthContextHolderUtils.clearContext();
        }

        String ip = ServletUtil.getIp();
        log.info( "会员{}用户名{}密码登录IP:{}", memberInfo.getId(), memberInfo.getNickName(), ip );

        MemberInfo update = new MemberInfo();
        update.setId( memberInfo.getId() );
        update.setDeviceId( mobileLogin.getDeviceId() );
        this.setMemberLoginParam( mobileLogin, dev, version, loginUrl, memberInfo.getLoginProvince(), update );

        if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getUsername(), 5 ) ) {
            return RspBase.businessError( "请勿重复登录" );
        }

        if ( oldm != null ) {
            this.baseMapper.insert( memberInfo );
            isNewMember = true;
            this.baseMapper.deleteByHistoryKey( oldm.getId() );
        } else {
            this.baseMapper.updateById( update );
        }

        RspMember rspMember = new RspMember();
        rspMember.setNewAccount( isNewMember );
        BeanUtils.copyProperties( memberInfo, rspMember );
        setNextLevelIntegral( memberInfo, rspMember );
        return RspBase.ok( "登录成功", rspMember );
    }

    private void setMemberLoginParam( MobileLogin mobileLogin, Integer dev, String version, String loginUrl,
                                      String loginProvince, MemberInfo memberInfo ) {
        memberInfo.setLoginDev( dev );
        memberInfo.setLoginIp( mobileLogin.getIp() );
        memberInfo.setLoginTime( LocalDateTime.now() );
        memberInfo.setVersion( version );
        //手机型号
        if ( StringUtils.isNotBlank( mobileLogin.getPhoneModel() ) ) {
            memberInfo.setPhoneModel( mobileLogin.getPhoneModel() );
        }
        if ( StringUtils.isBlank( loginProvince ) ) {
            try {
                memberInfo.setLoginProvince( this.baseMapper.funGetaddressProvinces( mobileLogin.getIp() ) );
            } catch ( Exception e ) {
                log.error( "获取ip所属省份失败，失败原因：{}", e.getMessage() );
            }
        }
        if ( StringUtils.isNotBlank( loginUrl ) ) {
            memberInfo.setLinkUrl( loginUrl );
        }
        if ( StringUtils.isNotBlank( mobileLogin.getPasswd() ) ) {
            memberInfo.setPassword( mobileLogin.getPasswordEncrypt() );
        }
    }

    private MemberInfo newMemberInfoReg( MobileLogin mobileLogin ) {
        MemberInfo m = new MemberInfo();
        m.setHeadImg( String.valueOf( RandomUtils.randomIntWithMax( 1, 14 ) ) );
        m.setId( makeMemberCode() );
        if ( StringUtils.isNotBlank( mobileLogin.getInviterCode() )
                && StringUtils.isAlphanumeric( mobileLogin.getInviterCode() ) ) {
            try {
                m.setInviterCode( mobileLogin.getInviterCode() );
            } catch ( Exception e ) {
                log.error( "推广码异常，inviter_code:{}", mobileLogin.getInviterCode() );
                mobileLogin.setInviterCode( null );
            }
        }
        m.setStatus( 1 );
        m.setVip( 1 );//默认vip1
        m.setRegisterTime( LocalDateTime.now() );
        m.setRegisterIp( mobileLogin.getIp() );
        BigDecimal registerMemberMoney = configEnvCacheUtil.getConfBd( "register_member_money" );
        if ( registerMemberMoney.compareTo( BigDecimal.ZERO ) < 0 ) {
            registerMemberMoney = BigDecimal.ZERO;
        }
        m.setAccountNow( registerMemberMoney );
        m.setAccountCharge( BigDecimal.ZERO );
        m.setBoxAccount( BigDecimal.ZERO );
        m.setCodeNow( BigDecimal.ZERO );
        m.setCodeWill( BigDecimal.ZERO );
        m.setCodeTotal( BigDecimal.ZERO );
        if ( StringUtils.isNotBlank( mobileLogin.getDeviceId() ) ) {
            m.setDeviceId( mobileLogin.getDeviceId() );
        }
        if ( StringUtils.isNotBlank( mobileLogin.getMobile() ) ) {
            m.setPhone( mobileLogin.getMobile() );
        }
        m.setNickName( m.getId() );
        m.setLoginNum( 0 );
        BigDecimal registerMemberBcodeMultiple = configEnvCacheUtil.getConfBd( "register_member_bcode_multiple" );
        if ( registerMemberBcodeMultiple.compareTo( BigDecimal.ZERO ) < 0 ) {
            registerMemberBcodeMultiple = BigDecimal.ZERO;
        }
        if ( registerMemberBcodeMultiple.compareTo( BigDecimal.ZERO ) > 0
                && registerMemberMoney.compareTo( BigDecimal.ZERO ) > 0 ) {
            MemberBcode code = new MemberBcode();
            code.setIncome( registerMemberMoney.multiply( registerMemberBcodeMultiple ).setScale( 2, RoundingMode.DOWN ) );
            code.setCharge( registerMemberMoney );
            code.setCreateTime( LocalDateTime.now() );
            code.setCur( BigDecimal.ZERO );
            code.setStatus( 0 );
            code.setUserId( m.getId() );
            code.setDes( EnumMoney.ACTIVITY.getDes() );
            memberBcodeMapper.insert( code );

            m.setCodeWill( code.getIncome() );
            // m.setAccountCharge( registerMemberMoney );
        }
        return m;
    }


    private void regChannelNotice( MobileLogin mobileLogin, Integer dev, String userId ) {
        String noticeUrl = configEnvCacheUtil.getConf( "channel_reg_notice" );
        if ( StringUtils.isBlank( noticeUrl ) ) {
            log.error( "平台无法找到环境变量channel_reg_notice,userId:{}", userId );
            return;
        }
        String ip = mobileLogin.getIp();
        Thread.ofVirtual().start( () -> {
            Map<String, Object> params = new HashMap<>();
            params.put( "channel_id", mobileLogin.getChannelCode() );
            params.put( "invitation_code", mobileLogin.getInviterCode() );
            params.put( "account", userId );
            params.put( "device_type", dev == 2 ? "android" : "ios" );
            params.put( "ip", ip );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType( MediaType.APPLICATION_JSON );
            headers.set( "agent", profile );
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>( params, headers );
            try {
                restTemplate.exchange( noticeUrl, HttpMethod.POST, requestEntity, Object.class );
            } catch ( Exception e ) {
                try {
                    restTemplate.exchange( noticeUrl, HttpMethod.POST, requestEntity, Object.class );
                } catch ( Exception ex ) {
                    log.error( ex.getMessage(), ex );
                }
            }
        } );
    }

    /**
     * 生成会员编号
     */
    private String makeMemberCode() {
        if ( !redisUtils.exists( Constants.MEMBER_CODE ) ) {
            String maxCode = this.baseMapper.selectMaxMemberCode();
            long mysqlMaxCode = "0".equals( maxCode ) ? Constants.MEMBER_CODE_INIT :
                    Long.parseLong( maxCode.replaceFirst( "M", "" ) ) - Constants.MEMBER_CODE_INIT;
            redisUtils.strSet( Constants.MEMBER_CODE, String.valueOf( mysqlMaxCode ) );
            return "M" + mysqlMaxCode;
        }
        RedisAtomicLong entityIdCounter = new RedisAtomicLong( Constants.MEMBER_CODE, redisUtils.getConnectionFactory() );
        return "M" + ( Constants.MEMBER_CODE_INIT + entityIdCounter.getAndIncrement() );
    }

    @Override
    public RspBase<?> sendSmsVerifyCode( Phone phone ) {
        if ( StringUtils.isBlank( phone.getPhone() ) ) {
            return RspBase.businessError( "请输入你的手机号" );
        }
        if ( ValidatorUtil.isMobile( phone.getPhone() ) ) {
            return RspBase.businessError( "手机号码不正确" );
        }
        if ( Objects.nonNull( smsPhoneCacheUtil.getSmsPhoneExpire( phone.getPhone() ) ) ) {
            return RspBase.businessError( "发送验证码频繁,请稍后发送" );
        }
        try {
            String code     = smsPhoneCacheUtil.getPhoneCode( phone.getPhone() );
            String indexStr = smsPhoneCacheUtil.getPhoneIndex( phone.getPhone() );
            int    index    = Integer.parseInt( indexStr == null ? "-1" : indexStr ) + 1;
            code = smsApi.sendSms( phone.getPhone(), index, code );
            smsPhoneCacheUtil.setSmsPhoneCache( phone.getPhone(), code, String.valueOf( index ) );
            return RspBase.ok();
        } catch ( Exception e ) {
            log.error( "发送短信失败phone:{}", phone.getPhone(), e );
            return RspBase.businessError( e.getMessage() );
        }
    }

    @Override
    public RspBase<?> addMemberMoneyOnly( String ip, String userName, ReqAddScore req ) {
        String     userId        = req.getId();
        String     moneydes      = req.getMoneydes();
        BigDecimal money         = req.getScore();
        BigDecimal beatNum       = req.getBeatNum();
        String     Mk            = req.getMk() + ",操作人:" + userName;
        String     markorder     = req.getOrdermk();
        MemberInfo oldmemberInfo = this.baseMapper.selectById( userId );
        BigDecimal total         = oldmemberInfo.getAccountNow();

        if ( money.compareTo( BigDecimal.ZERO ) > 0 ) {
            String addMoney = configEnvCacheUtil.getConf( "addMoney", "1000000" );
            if ( money.compareTo( new BigDecimal( Integer.parseInt( addMoney ) ) ) > 0 ) {
                return RspBase.businessError( "最大金额为" + Integer.parseInt( addMoney ) );
            }
        } else if ( money.compareTo( BigDecimal.ZERO ) < 0 ) {
            BigDecimal lat = total.add( money );
            if ( lat.compareTo( BigDecimal.ZERO ) < 0 ) {
                return RspBase.businessError( "余额" + money + "不足扣除" );
            }
            beatNum = BigDecimal.ZERO;
        }

        if ( !"0".equals( markorder ) ) {
            List<LogMoney> markList = null;
            if ( money.compareTo( BigDecimal.ZERO ) > 0 ) {
                markList = logMoneyMapper.findMark( userId, markorder, money, null, userId.substring(
                        userId.length() - 1 ), null );
            } else {
                BigDecimal negate = money.negate();
                markList = logMoneyMapper.findMark( userId, markorder, null, negate, userId.substring(
                        userId.length() - 1 ), null );
            }
            if ( !CollectionUtils.isEmpty( markList ) ) {
                return RspBase.businessError( "请查看此笔金额是否已经入款过，如否请输入其他订单备注" );
            }
        }

        if ( total != null ) {
            if ( beatNum == null || beatNum.compareTo( BigDecimal.ZERO ) < 1 ) {
                beatNum = BigDecimal.ZERO;
            }
            memberMoneyManager.addMemberMoney( userId, money, "1".equals( moneydes ) ? EnumMoney.GM : EnumMoney.ACTIVITY,
                    beatNum, Mk, null, markorder );
        } else {
            return RspBase.businessError( "该成员未初始化金额，或者您输入的金额有误" );
        }
        return RspBase.ok();
    }

    @Override
    public List<MemberInfo> selectMemberInfoList( MemberInfo memberInfo ) {
        //有其他搜索条件的时候，忽略时间
        if ( StringUtils.isNotBlank( memberInfo.getSearchValue() ) || StringUtils.isNotBlank( memberInfo.getLoginIp() )
                || StringUtils.isNotBlank( memberInfo.getInviterCode() ) ) {
            memberInfo.setSelectDate( null );
        }
        String[] selectDate = memberInfo.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            memberInfo.setSelectStartDate( selectDate[ 0 ] );
            memberInfo.setSelectEndDate( selectDate[ 1 ] );
        }
        List<MemberInfo> memberInfoList = this.baseMapper.selectMemberInfoList( memberInfo );
        Set<String>      memberIds      = memberInfoList.stream().map( MemberInfo::getId ).collect( Collectors.toSet() );
        if ( !CollectionUtils.isEmpty( memberIds ) ) {
            List<MemberCard> memberCards = memberCardMapper.selectRealNameByMemberIds( memberIds );
            for ( MemberInfo info : memberInfoList ) {
                if ( StringUtils.isNotBlank( info.getPhone() ) ) {
                    info.setPhone( info.getPhone().substring( 0, 3 ) + "****" + info.getPhone().substring( 7 ) );
                }
                for ( MemberCard memberCard : memberCards ) {
                    if ( info.getId().equals( memberCard.getMemberId() ) ) {
                        info.setCardRealName( memberCard.getRealName() );
                    }
                }
                info.setPassword( null );
                info.setBoxPass( null );
                info.setWithdrawalPass( null );
            }
        }
        return memberInfoList;
    }

    @Override
    public Map listCount( MemberInfo memberInfo ) {
        //有其他搜索条件的时候，忽略时间
        if ( StringUtils.isNotBlank( memberInfo.getSearchValue() ) || StringUtils.isNotBlank( memberInfo.getLoginIp() )
                || StringUtils.isNotBlank( memberInfo.getInviterCode() ) ) {
            memberInfo.setSelectDate( null );
        }
        String[] selectDate = memberInfo.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            memberInfo.setSelectStartDate( selectDate[ 0 ] );
            memberInfo.setSelectEndDate( selectDate[ 1 ] );
        }
        return this.baseMapper.listCount( memberInfo );
    }

    @Override
    public RspBase<?> updateMobile( String oldMobile, String newMobile, String memberId ) {
        if ( this.baseMapper.exists( new QueryWrapper<MemberInfo>().eq( "phone", newMobile ) ) ) {
            return RspBase.businessError( "此手机号已经存在" );
        }
        MemberInfo memberInfo = new MemberInfo();
        if ( newMobile.length() != 11 ) {
            return RspBase.businessError( "新手机号码长度不能是非11位" );
        }
        memberInfo.setPhone( newMobile );
        memberInfo.setId( memberId );
        int i = this.baseMapper.updateById( memberInfo );
        return i > 0 ? RspBase.ok( "更新成功" ) : RspBase.businessError( "更新失败" );
    }

    @Override
    public List<MemberCard> selectMemberCardList( String memberId ) {
        return memberCardMapper.selectMemberCard( memberId );
    }

    @Override
    public BigDecimal getHistoryRecharge( String memberId ) {
        BigDecimal money = this.baseMapper.selectMemberInfoHistoryRechargeById( memberId );
        return money == null ? BigDecimal.ZERO : money;
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void repairMemberBcode( String memberId ) {
        memberBcodeMapper.updateMemberBcodeStatus( memberId );
        memberBcodeMapper.repairMemberInfo( memberId );
    }

    @Override
    public RspBase<?> unbindCard( MemberCard member ) {
        Long             id             = member.getId();
        String           memberId       = member.getMemberId();
        List<MemberCard> memberCardList = memberCardMapper.selectMemberCard( memberId );
        MemberCard       memberCard     = memberCardMapper.selectById( id );
        if ( Objects.isNull( memberCard ) ) {
            return RspBase.businessError( "卡号不存在" );
        }
        if ( memberCardList.size() > 1 && BooleanUtils.isTrue( memberCard.getDv() ) ) {
            return RspBase.businessError( "请先解绑副卡" );
        }
        memberCardMapper.deleteById( id );
        return RspBase.ok( "解绑成功" );
    }

    @Override
    public RspBase<?> changeBank( MemberCard member ) {
        Long id = member.getId();
        //判断用户是否已经绑定该银行卡
        MemberCard memberCard1 = new MemberCard();
        memberCard1.setBankAccount( member.getBankAccount() );
        memberCard1.setMemberId( member.getMemberId() );

        List<MemberCard> memberCardList = new QueryChainWrapper<>( memberCardMapper )
                .eq( "bank_account", member.getBankAccount() )
                .list();

        List<MemberCard> memberCardListFiltered = memberCardList
                .stream()
                .filter( ( mc ) -> !mc.getId().equals( member.getId() ) && mc.getBankAccount().equals( member.getBankAccount() ) )
                .toList();
        if ( !memberCardListFiltered.isEmpty() && memberCardListFiltered.get( 0 ) != null ) {
            return RspBase.businessError( "卡已绑定帐号" + memberCardListFiltered.get( 0 ).getMemberId() );
        }

        MemberCard memberCard = memberCardMapper.selectById( id );
        memberCard.setRealName( member.getRealName() );
        memberCard.setBankId( member.getBankId() );
        memberCard.setBankAddress( member.getBankAddress() );
        memberCard.setBankAccount( member.getBankAccount() );
        memberCardMapper.updateById( memberCard );
        return RspBase.ok( "修改银行卡信息成功" );
    }

    @Override
    public RspBase<?> personalReport( String startTime, String endTime, String memberId ) {
        List<Callable<Map<String, Object>>> forkJoinTasks = new ArrayList<>();

        // 线下充值 Offline recharge
        forkJoinTasks.add( () -> ImmutableMap.of( "personalRecharge", this.baseMapper.personalRecharge( startTime, endTime,
                memberId ) ) );
        // 线上充值 online recharge
        forkJoinTasks.add( () -> ImmutableMap.of( "personalOnlineRecharge", this.baseMapper.personalOnlineRecharge( startTime,
                endTime, memberId ) ) );
        //        // 线上充值2 online recharge 2
        //        forkJoinTasks.add( () -> ImmutableMap.of( "personalAgentRecharge", this.baseMapper.personalAgentRecharge(
        //        startTime,
        //                endTime, memberId ) ) );
        // 线上充值3 online recharge 3
        forkJoinTasks.add( () -> ImmutableMap.of( "personalUsdtRecharge", this.baseMapper.personalUsdtRecharge( startTime,
                endTime, memberId ) ) );
        // 提款 withdrawal
        forkJoinTasks.add( () -> ImmutableMap.of( "personalWithdrawRecharge",
                this.baseMapper.personalWithdrawRecharge( startTime, endTime, memberId ) ) );
        forkJoinTasks.add( () -> ImmutableMap.of( "totalAccount",
                this.baseMapper.totalAccount( startTime, endTime, memberId ) ) );

        List<Future<Map<String, Object>>> futureList = null;
        try {
            futureList = Executors.newVirtualThreadPerTaskExecutor().invokeAll( forkJoinTasks );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
        Set<Map<String, Object>> resultSet = futureList.stream().map( t -> {
            try {
                return t.get();
            } catch ( InterruptedException | ExecutionException e ) {
                throw new IllegalStateException( e );
            }
        } ).filter( Objects::nonNull ).collect( Collectors.toSet() );
        resultSet.add( ImmutableMap.of( "memberId", memberId ) );

        Map<String, Object> resultMap = resultSet
                .stream()
                .map( Map::entrySet )
                .flatMap( Set::stream )
                .collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) );

        List<Map> mapList = this.baseMapper.personalGameData( startTime, endTime, memberId, memberId.substring(
                memberId.length() - 1 ) );

        resultMap.put( "bCodeList", mapList );

        return RspBase.ok( resultMap );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public RspBase<?> boxDish( String memberId ) {
        MemberInfo memberInfo   = this.baseMapper.selectById( memberId );
        BigDecimal totalAccount = memberInfo.getAccountNow();
        BigDecimal boxAccount   = memberInfo.getBoxAccount();
        if ( boxAccount.compareTo( BigDecimal.ZERO ) == 0 ) {
            return RspBase.businessError( "保险箱余额为0,无需转出" );
        }

        BigDecimal totalNow = totalAccount.add( boxAccount );
        String     name     = "保险箱存入:" + boxAccount.negate() + "现保险箱余额:0";

        int i = memberMoneyManager.logSafebox( memberId, boxAccount.negate(), name, totalAccount, totalNow );
        int j = this.baseMapper.boxDish( memberId );
        if ( i <= 0 || j <= 0 ) {
            throw new BusinessException( "保险箱余额提出失败" );
        }
        return RspBase.ok();
    }

    @Override
    public RspBase<?> updateVip( String memberId, Integer vip, String nickName ) {
        if ( vip > 50 ) {
            return RspBase.businessError( "vip等级最大为50级" );
        }
        MemberInfo m = this.baseMapper.selectById( memberId );
        if ( m == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        if ( m.getVip() > vip ) {
            return RspBase.businessError( "vip等级修改不能小于之前的等级" );
        }
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setVip( vip );
        update.setNickName( nickName );
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            String token = redisUtils.strGet( Constants.MEMBER_LOGIN_USER + memberId );
            if ( StringUtils.isNotBlank( token ) ) {
                Map loginUserMap = redisUtils.hGetAll( Constants.MEMBER_LOGIN_TOKEN + token );
                if ( !CollectionUtils.isEmpty( loginUserMap ) ) {
                    PlatformUser platformUser = JsonUtil.json2Object( loginUserMap
                            .getOrDefault( "platformUserStr", "" )
                            .toString(), PlatformUser.class );
                    platformUser.setVip( vip );
                    platformUser.setNickName( nickName );
                    loginUserMap.put( "platformUserStr", JsonUtil.object2Json( platformUser ) );
                    redisUtils.hMSet( Constants.MEMBER_LOGIN_TOKEN + token, loginUserMap );
                }
            }
            return RspBase.ok( "更新成功" );
        }
        return RspBase.businessError( "更新失败" );
    }

    @Override
    public RspBase<?> memberBoxPassIsOpen( String memberId ) {
        MemberInfo memberInfo = new QueryChainWrapper<>( this.baseMapper ).eq( "id", memberId ).select( "id", "box_pass" ).one();
        if ( memberInfo == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        return RspBase.ok( StringUtils.isNotBlank( memberInfo.getBoxPass() ) );
    }

    @Override
    public RspBase<?> memberBoxPassSet( String memberId, ReqBoxPass boxPass ) {
        MemberInfo memberInfo = new QueryChainWrapper<>( this.baseMapper ).eq( "id", memberId ).select( "id", "box_pass" ).one();
        if ( memberInfo == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        if ( StringUtils.isNotBlank( memberInfo.getWithdrawalPass() ) ) {
            return RspBase.businessError( "提现已经设置过密码" );
        }
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setBoxPass( passwordEncoder.encode( boxPass.getBoxPass() ) );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "设置保险箱密码异常，请稍后再试" );
    }

    @Override
    public RspBase<RspMoney> boxAccount( String memberId, ReqBoxPass boxPass ) {
        MemberInfo memberInfo = new QueryChainWrapper<>( this.baseMapper )
                .eq( "id", memberId )
                .select( "id", "box_account", "account_now", "box_pass" )
                .one();
        if ( memberInfo == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        if ( StringUtils.isNotBlank( memberInfo.getBoxPass() )
                && !passwordEncoder.matches( boxPass.getBoxPass(), memberInfo.getBoxPass() ) ) {
            return RspBase.businessError( "保险箱密码错误，请重新输入" );
        }
        RspMoney money = new RspMoney();
        money.setBoxAccount( memberInfo.getBoxAccount().setScale( 2, RoundingMode.HALF_UP ) );
        money.setAccountNow( memberInfo.getAccountNow().setScale( 2, RoundingMode.HALF_UP ) );
        return RspBase.ok( money );
    }

    @Override
    public RspBase<RspMoney> boxTransfer( String memberId, ReqBoxChange boxChange ) {
        BigDecimal addAccount = boxChange.getAddAccount().setScale( 0, RoundingMode.DOWN );
        if ( addAccount.compareTo( BigDecimal.ZERO ) == 0 ) {
            return RspBase.businessError( "转入或取出数量有误，请重新输入" );
        }
        if ( !redisUtils.lock( "boxTransfer" + memberId, 5 ) ) {
            return RspBase.businessError( "处理中请稍后" );
        }
        MemberInfo memberInfo = new QueryChainWrapper<>( this.baseMapper )
                .eq( "id", memberId )
                .select( "id", "box_account", "account_now" )
                .one();
        BigDecimal boxAccount = memberInfo.getBoxAccount();
        BigDecimal accountNow = memberInfo.getAccountNow();
        boolean    flag       = false;
        if ( addAccount.compareTo( BigDecimal.ZERO ) > 0 ) {//转入保险箱
            flag = true;
            if ( addAccount.compareTo( accountNow ) > 0 ) {
                return RspBase.businessError( "钱包余额少于转入量" );
            }
        } else {//取出保险箱
            if ( addAccount.negate().compareTo( boxAccount ) > 0 ) {
                return RspBase.businessError( "保险箱余额少于取出量" );
            }
        }
        SpringUtils.getBean( MemberInfoService.class ).updateSafeBox( memberInfo, addAccount, flag );
        MemberInfo newInfo = new QueryChainWrapper<>( this.baseMapper )
                .eq( "id", memberId )
                .select( "id", "box_account", "account_now" )
                .one();
        RspMoney money = new RspMoney();
        money.setBoxAccount( newInfo.getBoxAccount() );
        money.setAccountNow( newInfo.getAccountNow() );
        redisUtils.unLock( "boxTransfer" + memberId );
        return RspBase.ok( money );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateSafeBox( MemberInfo memberInfo, BigDecimal addAccount, boolean flag ) {
        int i = this.baseMapper.updateSafeBox( memberInfo.getId(), addAccount );

        BigDecimal boxAccount = memberInfo.getBoxAccount().add( addAccount );
        BigDecimal accountNow = memberInfo.getAccountNow().subtract( addAccount );
        String     remark     = "保险箱:" + ( flag ? "存入" : "取出" ) + addAccount + "现保险箱余额:" + boxAccount;

        int j = memberMoneyManager.logSafebox( memberInfo.getId(), addAccount, remark, accountNow, memberInfo.getAccountNow() );
        if ( i <= 0 || j <= 0 ) {
            throw new BusinessException( "转入或取出数据异常，请稍后再试" );
        }
    }

    @Override
    public RspBase<RspAccountMoney> getAccountNow( String memberId ) {
        RspAccountMoney accountMoney = new RspAccountMoney();
        accountMoney.setBalance( this.baseMapper.getUserBalance( memberId ).setScale( 2, RoundingMode.HALF_UP ) );
        return RspBase.ok( accountMoney );
    }

    @Override
    public RspBase<RspMember> getAccountInfo( String memberId ) {
        MemberInfo memberInfo = this.baseMapper.selectById( memberId );
        RspMember  rspMember  = new RspMember();
        BeanUtils.copyProperties( memberInfo, rspMember );

        List<ConfigVip> configVips = configVipCacheUtils
                .getConfigVipMap()
                .values()
                .stream()
                .sorted( Comparator.comparing( ConfigVip::getBcode ) )
                .toList();
        Integer vip = 1;
        for ( ConfigVip configVip : configVips ) {
            if ( memberInfo.getCodeTotal().compareTo( configVip.getBcode() ) < 0 ) {
                rspMember.setNextLevelIntegral( configVip.getBcode().subtract( rspMember.getCodeTotal() ) );
                break;
            }
            vip = configVip.getLevel();
        }
        if ( vip > memberInfo.getVip() ) {
            int updateVip = this.baseMapper.updateVipById( memberId, vip );
            rspMember.setVip( vip );
            if ( updateVip > 0 ) {
                // 更新缓存
                String token = redisUtils.strGet( Constants.MEMBER_LOGIN_USER + memberId );
                if ( StringUtils.isNotBlank( token ) && redisUtils.exists( Constants.MEMBER_LOGIN_TOKEN + token ) ) {
                    Map loginUserMap = redisUtils.hGetAll( Constants.MEMBER_LOGIN_TOKEN + token );
                    PlatformUser platformUser = JsonUtil.json2Object( loginUserMap
                            .getOrDefault( "platformUserStr", "" )
                            .toString(), PlatformUser.class );
                    platformUser.setVip( vip );
                    loginUserMap.put( "platformUserStr", JsonUtil.object2Json( platformUser ) );
                    redisUtils.hMSet( Constants.MEMBER_LOGIN_TOKEN + token, loginUserMap );
                }
            }
        }
        return RspBase.ok( rspMember );
    }

    @Override
    public List<RspLogMoney> getFundDetails( String memberId, ReqLogMoney reqLogMoney ) {
        String beginDay = reqLogMoney.getEnumReqTime().getBeginDayTime();
        String endDay   = reqLogMoney.getEnumReqTime().getEndDayTime();
        List<RspLogMoney> logMoneyList = logMoneyMapper.findLogMoneyList( memberId, memberId.substring(
                memberId.length() - 1 ), reqLogMoney, beginDay, endDay );
        for ( RspLogMoney rspLogMoney : logMoneyList ) {
            EnumMoney byType = EnumMoney.getByType( rspLogMoney.getType() );
            if ( byType != null ) {
                rspLogMoney.setDes( byType.getDes() );
            }
        }
        return logMoneyList;
    }

    @Override
    public List<RspCodeFlow> getCodeFlowList( String memberId ) {
        return memberBcodeMapper.findByMemberId( memberId );
    }

    @Override
    public RspVipInfo getVipGiftInfo( String memberId ) {
        RspVipInfo              rsp          = new RspVipInfo();
        Integer                 vip          = this.baseMapper.getUserVip( memberId );
        Map<Integer, ConfigVip> configVipMap = configVipCacheUtils.getConfigVipMap();
        rsp.setVipSetList( configVipMap.values().stream().map( v -> {
            RspVipSet rspVipSet = new RspVipSet();
            BeanUtils.copyProperties( v, rspVipSet );
            return rspVipSet;
        } ).sorted( Comparator.comparing( RspVipSet::getLevel ) ).collect( Collectors.toList() ) );
        ConfigVip     vipSet        = configVipMap.get( vip );
        MemberVipGift memberVipGift = memberVipGiftMapper.selectById( memberId );
        if ( memberVipGift == null ) {
            rsp.setLevelBonusStatus( 1 );
            rsp.setWeekBonusStatus( 1 );
            //rsp.setMonthBonusStatus( 1 );
        } else {
            rsp.setLevelBonusStatus( 2 );
            rsp.setWeekBonusStatus( 2 );
            //rsp.setMonthBonusStatus( 2 );
            if ( vip > memberVipGift.getLevelBonusVip() ) {
                rsp.setLevelBonusStatus( 1 );
            }
            LocalDateTime now = LocalDateTime.now();
            if ( memberVipGift.getWeekBonusTime() == null
                    || !LocalDateTimeUtils.isSameWeek( memberVipGift.getWeekBonusTime(), now ) ) {
                rsp.setWeekBonusStatus( 1 );
            }
           /* if ( memberVipGift.getMonthBonusTime() == null
                    || !LocalDateTimeUtils.isSameMonth( memberVipGift.getMonthBonusTime(), now ) ) {
                rsp.setMonthBonusStatus( 1 );
            }*/
        }
        if ( vipSet.getLevelBonus().compareTo( BigDecimal.ZERO ) == 0 ) {
            rsp.setLevelBonusStatus( 0 );
        }
        if ( vipSet.getWeekBonus().compareTo( BigDecimal.ZERO ) == 0 ) {
            rsp.setWeekBonusStatus( 0 );
        }
        /*if ( vipSet.getMonthBonus().compareTo( BigDecimal.ZERO ) == 0 ) {
            rsp.setMonthBonusStatus( 0 );
        }*/
        return rsp;
    }

    @Override
    public RspBase<?> receiveVipGift( String memberId, Integer type ) {
        if ( type == null || type < 1 || type > 3 ) {
            return RspBase.businessError( "参数有误" );
        }
        if ( !redisUtils.lock( "receiveVipGift" + memberId, 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        Integer       vip           = this.baseMapper.getUserVip( memberId );
        MemberVipGift memberVipGift = memberVipGiftMapper.selectById( memberId );
        ConfigVip     configVip     = configVipCacheUtils.getConfigVip( vip );
        boolean       isInsert;
        LocalDateTime now           = LocalDateTime.now();
        MemberVipGift saveOrUpdate  = new MemberVipGift();
        if ( memberVipGift == null ) {
            saveOrUpdate.setMemberId( memberId );
            isInsert = true;
            if ( type == 1 ) {
                saveOrUpdate.setLevelBonusVip( vip );
            } else if ( type == 2 ) {
                saveOrUpdate.setWeekBonusTime( now );
                saveOrUpdate.setLevelBonusVip( 0 );
            } else {
                saveOrUpdate.setMonthBonusTime( now );
                saveOrUpdate.setLevelBonusVip( 0 );
            }

        } else {
            isInsert = false;
            saveOrUpdate.setMemberId( memberId );
            if ( type == 1 ) {
                if ( Objects.equals( memberVipGift.getLevelBonusVip(), vip ) ) {
                    return RspBase.businessError( "晋级彩金重复领取" );
                }
                saveOrUpdate.setLevelBonusVip( vip );
            } else if ( type == 2 ) {
                if ( memberVipGift.getWeekBonusTime() != null
                        && LocalDateTimeUtils.isSameWeek( memberVipGift.getWeekBonusTime(), now ) ) {
                    return RspBase.businessError( "周彩金重复领取" );
                }
                saveOrUpdate.setWeekBonusTime( now );
            } else {
                if ( memberVipGift.getMonthBonusTime() != null
                        && LocalDateTimeUtils.isSameMonth( memberVipGift.getMonthBonusTime(), now ) ) {
                    return RspBase.businessError( "月彩金重复领取" );
                }
                saveOrUpdate.setMonthBonusTime( now );
            }
        }
        String     name      = "vip:" + vip;
        BigDecimal addMoney  = BigDecimal.ZERO;
        BigDecimal needBcode = BigDecimal.ZERO;
        if ( type == 1 ) {
            name     = name + "晋级彩金";
            addMoney = configVip.getLevelBonus();
        } else if ( type == 2 ) {
            name     = name + "周俸禄";
            addMoney = configVip.getWeekBonus();
            if ( configVip.getWeekCharge() != null && configVip.getWeekCharge().compareTo( BigDecimal.ZERO ) > 0 ) {
                BigDecimal sumRechargeWeek = memberInfoMapper.sumRecharge( memberId, 7 );
                if ( configVip.getWeekCharge().compareTo( sumRechargeWeek ) > 0 ) {
                    return RspBase.businessError( "近一周内充值未达标,无法领取俸禄,剩余充值:" + configVip
                            .getWeekCharge()
                            .subtract( sumRechargeWeek )
                            .setScale( 2, RoundingMode.UP ) );
                }
            }
            if ( configVip.getBcodeMultiple() != null && configVip.getBcodeMultiple().compareTo( BigDecimal.ZERO ) > 0 ) {
                needBcode = addMoney.multiply( configVip.getBcodeMultiple() ).setScale( 2, RoundingMode.HALF_UP );
            }
        } /*else {
            name     = name + "月俸禄";
            addMoney = configVip.getMonthBonus();
            if ( configVip.getMonthCharge() != null && configVip.getMonthCharge().compareTo( BigDecimal.ZERO ) > 0 ) {
                BigDecimal sumRechargeMonth = memberInfoMapper.sumRecharge( memberId, 31 );
                if ( configVip.getMonthCharge().compareTo( sumRechargeMonth ) > 0 ) {
                    return RspBase.businessError( "近一月内充值未达标,无法领取俸禄,剩余充值:" + configVip
                            .getMonthCharge()
                            .subtract( sumRechargeMonth )
                            .setScale( 2, RoundingMode.UP ) );
                }
            }
            if ( configVip.getBcodeMultiple() != null && configVip.getBcodeMultiple().compareTo( BigDecimal.ZERO ) > 0 ) {
                needBcode = addMoney.multiply( configVip.getBcodeMultiple() ).setScale( 2, RoundingMode.HALF_UP );
            }
        }*/

        SpringUtils
                .getBean( MemberInfoService.class )
                .receiveVipGift( memberId, isInsert, saveOrUpdate, name, addMoney, needBcode );

        redisUtils.unLock( "receiveVipGift" + memberId );
        return RspBase.ok( "领取成功" );
    }

    @Transactional( rollbackFor = Exception.class )
    public void receiveVipGift( String memberId, boolean isInsert, MemberVipGift saveOrUpdate, String name, BigDecimal addMoney
            , BigDecimal needBcode ) {
        int i;
        if ( isInsert ) {
            i = memberVipGiftMapper.insert( saveOrUpdate );
        } else {
            i = memberVipGiftMapper.updateById( saveOrUpdate );
        }
        if ( i <= 0 ) {
            throw new BusinessException( "领取异常,请重试" );
        }
        if ( needBcode.compareTo( BigDecimal.ZERO ) > 0 ) {
            MemberBcode code = new MemberBcode();
            code.setIncome( needBcode );
            code.setCharge( addMoney );
            code.setCreateTime( LocalDateTime.now() );
            code.setCur( BigDecimal.ZERO );
            code.setStatus( 0 );
            code.setUserId( memberId );
            code.setDes( name );
            if ( memberBcodeMapper.insert( code ) <= 0 ) {
                throw new BusinessException( "领取异常,请重试" );
            }
        }

        //会员加钱，日志
        memberMoneyManager.addMemberMoney( memberId, addMoney, EnumMoney.WONGIVE, BigDecimal.ONE,
                name + "奖励:" + addMoney.toString(), null, null );
    }

    @Override
    public RspBase<RspImToken> getImToken( String userId ) {
        String newImHosts = configEnvCacheUtil.getConf( "new_im_hosts" );
        if ( StringUtils.isBlank( this.getImTokenUrl ) || StringUtils.isBlank( newImHosts ) ) {
            return RspBase.businessError( "未初始化参数" );
        }
        Map<String, String> requestMap = Maps.newHashMap();
        requestMap.put( "memberId", userId );
        requestMap.put( "agentCenter", profile );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        String token = null;
        try {
            token = restTemplate.execute( this.getImTokenUrl, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ),
                    response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return text;
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        if ( StringUtils.isBlank( token ) ) {
            return RspBase.businessError( "获取token失败" );
        }
        RspImToken rspImToken = new RspImToken();
        rspImToken.setToken( token );
        rspImToken.setImHostlist( Arrays.asList( newImHosts.split( "," ) ) );
        return RspBase.ok( rspImToken );
    }

    @Override
    public RspBase<?> insertMemberInfo( String phone, String password ) {
        if ( phone == null ) {
            return RspBase.businessError( "手机号不能为空" );
        }

        if ( StringUtils.isBlank( password ) ) {
            return RspBase.businessError( "密码不能为空" );
        }

        if ( password.length() < 6 || password.length() > 15 ) {
            return RspBase.businessError( "密码长度必须大于等于6小于15" );
        }
        //校验是不是手机号
        if ( !ValidatorUtil.isNumber11( phone ) ) {
            return RspBase.businessError( "手机号必须是11位数字" );
        }
        if ( this.baseMapper.selectCount( new QueryWrapper<MemberInfo>().eq( "phone", phone ) ) > 0 ) {
            return RspBase.businessError( "此手机号已经存在" );
        }
        MemberInfo m = new MemberInfo();
        m.setPassword( passwordEncoder.encode( password ) );
        m.setHeadImg( String.valueOf( RandomUtils.randomIntWithMax( 1, 14 ) ) );
        m.setId( makeMemberCode() );
        m.setNickName( m.getId() );
        m.setPhone( phone );
        m.setStatus( 2 );
        m.setVip( 1 );//默认vip1
        m.setRegisterTime( LocalDateTime.now() );
        m.setAccountNow( BigDecimal.ZERO );
        m.setAccountCharge( BigDecimal.ZERO );
        m.setBoxAccount( BigDecimal.ZERO );
        m.setCodeNow( BigDecimal.ZERO );
        m.setCodeWill( BigDecimal.ZERO );
        m.setCodeTotal( BigDecimal.ZERO );
        m.setLoginNum( 0 );
        int insert = this.baseMapper.insert( m );
        return insert > 0 ? RspBase.ok( "新增成功" ) : RspBase.businessError( "新增失败" );
    }

    @Override
    public RspBase<?> bindPhone( MobileBind mobileBind, PlatformUser platformUser ) {
        if ( StringUtils.isBlank( mobileBind.getPasswd() ) ) {
            return RspBase.businessError( "请输入登录密码" );
        }
        RspBase<?> rspBase = verificationPhoneCode( mobileBind.getMobile(), mobileBind.getCode() );
        if ( rspBase != null ) {
            return rspBase;
        }
        String phone = this.baseMapper.getUserPhone( platformUser.getId() );
        if ( StringUtils.isNotBlank( phone ) ) {
            return RspBase.businessError( "您已绑定手机号,请勿重复绑定" );
        }
        Long phoneCount = this.baseMapper.selectCount( new QueryWrapper<MemberInfo>().eq( "phone", mobileBind.getMobile() ) );
        if ( phoneCount > 0 ) {
            return RspBase.businessError( "该手机号已注册账号,请勿重复绑定" );
        }

        MemberInfo update = new MemberInfo();
        update.setId( platformUser.getId() );
        update.setPassword( mobileBind.getPasswordEncrypt() );
        update.setPhone( mobileBind.getMobile() );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok( "绑定手机成功" ) : RspBase.businessError( "绑定手机失败,请重试" );
    }

    private RspBase<?> verificationPhoneCode( String phone, String code ) {
        if ( StringUtils.isBlank( phone ) ) {
            return RspBase.businessError( "请输入手机号" );
        }
        if ( phone.length() != 11 ) {
            return RspBase.businessError( "请输入正确的手机号" );
        }
        MobileLimit mobileLimit = mobileLimitMapper.selectById( phone );
        if ( mobileLimit != null ) {
            return RspBase.businessError( "该手机号被限制登录,请联系客服" );
        }
        if ( StringUtils.isBlank( code ) ) {
            return RspBase.businessError( "请输入短信验证码" );
        }
        String fcode = smsPhoneCacheUtil.getPhoneCode( phone );
        if ( StringUtils.isBlank( fcode ) ) {
            return RspBase.businessError( "验证码过期" );
        }
        if ( smsPhoneCacheUtil.setSmsNumber( phone ) >= 5 ) {
            smsPhoneCacheUtil.unLink( phone );
            return RspBase.businessError( "短信验证错误不能超过五次" );
        }
        if ( !code.equals( fcode ) ) {
            return RspBase.businessError( "验证码错误" );
        }
        return null;
    }

    @Override
    public RspBase<?> resetPasswd( ReqResetPasswd reqResetPasswd, PlatformUser platformUser ) {
        if ( StringUtils.isBlank( reqResetPasswd.getOldPasswd() ) ) {
            return RspBase.businessError( "请输入原登录密码" );
        }
        if ( StringUtils.isBlank( reqResetPasswd.getNewPasswd() ) ) {
            return RspBase.businessError( "请输入新登录密码" );
        }
        String passwd = this.baseMapper.getUserPasswd( platformUser.getId() );
        if ( StringUtils.isBlank( passwd ) ) {
            return RspBase.businessError( "非手机注册用户,请绑定手机号" );
        }
        if ( !passwordEncoder.matches( reqResetPasswd.getOldPasswd(), passwd ) ) {
            return RspBase.businessError( "原登录密码错误" );
        }
        MemberInfo update = new MemberInfo();
        update.setId( platformUser.getId() );
        update.setPassword( reqResetPasswd.getPasswordEncrypt() );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok( "登录密码更新成功" ) : RspBase.businessError( "登录密码更新失败,请重试" );
    }

    @Override
    public String getMemberLoginAddress( String memberId ) {
        return this.baseMapper.selectMemberInfoAddressById( memberId );
    }

    @Override
    public RspBase<?> bindInviterCode( ReqMemberRecommend reqMemberRecommend, String memberId ) {
        MemberInfo memberInfo = new QueryChainWrapper<>( this.baseMapper )
                .eq( "id", memberId )
                .select( "id", "inviter_code" )
                .one();
        if ( memberInfo == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        if ( StringUtils.isNotBlank( memberInfo.getInviterCode() ) ) {
            return RspBase.businessError( "已存在邀请码" );
        }
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setInviterCode( reqMemberRecommend.getCode() );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok( "邀请码更新成功" ) : RspBase.businessError( "邀请码更新失败,请重试" );
    }

    @Override
    public RspBase<?> insertBatchExcelMoney( String userIds ) {
        this.baseMapper.clearMemberMoney();
        this.baseMapper.insertBatchMemberMoney( userIds );
        return RspBase.ok( "member_money updated" );
    }

    @Override
    public RspBase<?> updatePhones( ReqSmallFeatures req ) {
        if ( org.springframework.util.StringUtils.hasText( req.getPhones() )
                && org.springframework.util.StringUtils.hasText( req.getPassword() ) ) {
            if ( req.getPhones().contains( "\n" ) ) {
                try {
                    String[]      phones = req.getPhones().split( "\n" );
                    StringBuilder phone  = new StringBuilder();
                    for ( String s : phones ) {
                        phone.append( "\"" ).append( s ).append( "\"" ).append( "," );
                    }
                    phone = new StringBuilder( phone.substring( 0, phone.length() - 1 ) );
                    req.setPhones( phone.toString() );
                } catch ( Exception e ) {
                    return RspBase.businessError( "分割手机号出错,请检查格式" );
                }
            } else {
                req.setPhones( "\"" + req.getPhones() + "\"" );
            }
            memberInfoMapper.updatePhones( req );
            return RspBase.ok();
        }
        return RspBase.businessError( "请输入批量手机号和密码" );
    }

    @Override
    public RspBase<?> queryPhones( ReqSmallFeatures req ) {
        if ( org.springframework.util.StringUtils.hasText( req.getUserIds() ) ) {
            if ( req.getUserIds().contains( "\n" ) ) {
                try {
                    String[]      userIds = req.getUserIds().split( "\n" );
                    StringBuilder userId  = new StringBuilder();
                    for ( String id : userIds ) {
                        userId.append( "\"" ).append( id ).append( "\"" ).append( "," );
                    }
                    userId = new StringBuilder( userId.substring( 0, userId.length() - 1 ) );
                    req.setUserIds( userId.toString() );
                } catch ( Exception e ) {
                    return RspBase.businessError( "分割会员ID出错,请检查格式" );
                }
            } else {
                req.setUserIds( "\"" + req.getUserIds() + "\"" );
            }
            List<ReqSmallFeatures> phonesAndUserId = memberInfoMapper.queryPhones( req );
            List<String>           phonesByIds     = new ArrayList<>();
            for ( ReqSmallFeatures ph : phonesAndUserId ) {
                phonesByIds.add( ph.getUserIds() + ":" + ph.getPhonesByIds() );
            }
            return RspBase.ok( phonesByIds );
        }
        return RspBase.businessError( "请输入批量会员ID" );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public RspBase<?> commitMoney( ReqSmallFeatures req ) {
        if ( org.springframework.util.StringUtils.hasText( req.getMemberIds() ) ) {
            String[] userIds = null;
            if ( req.getMemberIds().contains( "\n" ) ) {
                try {
                    userIds = req.getMemberIds().split( "\n" );
                    StringBuilder userId = new StringBuilder();
                    for ( String id : userIds ) {
                        userId
                                .append( "\"" )
                                .append( id )
                                .append( "\"" )
                                .append( "," )
                                .append( req.getMoney() )
                                .append( "," )
                                .append( req.getMoney() )
                                .append( "),(" );
                    }
                    userId = new StringBuilder( userId.substring( 0, userId.length() - 3 ) );
                    req.setUserIds( userId.toString() );
                } catch ( Exception e ) {
                    return RspBase.businessError( "分割会员ID出错,请检查格式" );
                }
            } else {
                req.setUserIds( "\"" + req.getMemberIds() + "\"" + "," + req.getMoney() + "," + req.getMoney() );
            }
            //清除表中数据
            memberInfoMapper.clear();
            memberInfoMapper.insertPaiSong( req.getUserIds() );
            return RspBase.ok();
        }
        return RspBase.businessError( "请输入批量会员ID" );
    }

    @Override
    public RspBase<?> insertPaiSong( String userIds ) {
        if ( org.springframework.util.StringUtils.hasText( userIds ) ) {
            memberInfoMapper.insertPaiSong( userIds );
            return RspBase.ok();
        }
        return RspBase.businessError( "请输入批量会员ID" );
    }

    @Override
    public RspBase<?> clear() {
        memberInfoMapper.clear();
        return RspBase.ok();
    }

    @Override
    public void sendMsg( String msg, String memberId ) {
        MemberInfo memberInfo = memberInfoMapper.selectMemberInfoById( memberId );
        String     phone      = memberInfo.getPhone();
        smsApi.sendMemSms( phone, msg );
    }

    private void setNextLevelIntegral( MemberInfo memberInfo, RspMember rspMember ) {
        List<ConfigVip> configVips = configVipCacheUtils
                .getConfigVipMap()
                .values()
                .stream()
                .sorted( Comparator.comparing( ConfigVip::getBcode ) )
                .toList();
        for ( ConfigVip configVip : configVips ) {
            if ( memberInfo.getCodeTotal().compareTo( configVip.getBcode() ) < 0 ) {
                rspMember.setNextLevelIntegral( configVip.getBcode().subtract( rspMember.getCodeTotal() ) );
                break;
            }
        }
    }

    @Override
    public RspBase<?> updateCodeTotalVipLevel( MemberInfo memberInfo ) {
        if ( memberInfoMapper.updateCodeTotalVipLevel( memberInfo ) != 1 ) {
            throw new BusinessException( "更新代码总数和vip级别失败" );
        }
        return RspBase.ok();
    }
}
