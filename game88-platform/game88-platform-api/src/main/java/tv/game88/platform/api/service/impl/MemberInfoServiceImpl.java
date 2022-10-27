package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.member.dto.RspMember;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumDev;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.platform.api.dto.MobileLogin;
import tv.game88.platform.api.dto.RspInit;
import tv.game88.platform.api.dto.RspManUpdateVersion;
import tv.game88.platform.api.service.MemberInfoService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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


    @Override
    public RspInit getLoginInit( Integer dev, String version ) {
        RspInit      res  = new RspInit();
        List<Object> keys = new ArrayList<>( Arrays.asList( "customer_url", "customer_url2", "web_url", "star_pic" ) );

        if ( dev == EnumDev.IOS.getType() ) {
            keys.addAll( Arrays.asList( "ios_version", "ios_force_update", "ios_down_url", "ios_update_text" ) );
        } else {
            keys.addAll( Arrays.asList( "android_version", "android_force_update", "android_down_url", "android_update_text" ) );
        }
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

        MemberInfo memberInfo = this.baseMapper.findMemberByMobile( mobileLogin.getMobile() );
        MemberInfo oldm       = null;
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
            return RspBase.businessError( "手机号不存在/密码错误" );
        } finally {
            AuthContextHolderUtils.clearContext();
        }

        String ip = ServletUtil.getIp();
        log.info( "会员{}手机号密码登录IP:{}", memberInfo.getId(), ip );

        MemberInfo update = new MemberInfo();
        update.setId( memberInfo.getId() );

        this.setMemberLoginParam( mobileLogin, dev, version, loginUrl, memberInfo.getLoginProvince(), update );

        if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getMobile(), 5 ) ) {
            return RspBase.businessError( "请勿重复登录" );
        }

        if ( oldm != null ) {
            this.baseMapper.insert( memberInfo );
            this.baseMapper.deleteByHistoryKey( oldm.getId() );
        } else {
            this.baseMapper.updateById( update );
        }

        RspMember rspMember = new RspMember();
        BeanUtils.copyProperties( memberInfo, rspMember );
        return RspBase.ok( rspMember );
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
        BeanUtils.copyProperties( memberInfo, rspMember );
        return RspBase.ok( rspMember );
    }

    @Override
    public RspBase<RspMember> register( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) {
        if ( StringUtils.isBlank( mobileLogin.getMobile() ) ) {
            return RspBase.businessError( "请输入手机号码" );
        }
        if ( mobileLogin.getMobile().length() != 11 ) {
            return RspBase.businessError( "请输入正确的手机号" );
        }
        if ( StringUtils.isBlank( mobileLogin.getPasswd() ) ) {
            return RspBase.businessError( "请输入登陆密码" );
        }
        if ( StringUtils.isBlank( mobileLogin.getCode() ) ) {
            return RspBase.businessError( "请输入短信验证码" );
        }
        return null;
    }

    private void setMemberLoginParam( MobileLogin mobileLogin, Integer dev, String version, String loginUrl,
                                      String loginProvince, MemberInfo memberInfo ) {
        memberInfo.setLoginDev( dev );
        memberInfo.setLoginIp( mobileLogin.getIp() );
        memberInfo.setLoginTime( LocalDateTime.now() );
        memberInfo.setVersion( version );
        //手机型号
        if ( StringUtils.isNotBlank( mobileLogin.getPhoneModel() ) ) {
            if ( mobileLogin.getPhoneModel().length() >= 255 ) {
                memberInfo.setPhoneModel( mobileLogin.getPhoneModel().substring( 0, 254 ) );
            } else {
                memberInfo.setPhoneModel( mobileLogin.getPhoneModel() );
            }
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
    }

    private MemberInfo newMemberInfoReg( MobileLogin mobileLogin ) {
        MemberInfo m = new MemberInfo();
        m.setHeadImg( String.valueOf( MathUtils.randomIntWithMax( 1, 14 ) ) );
        m.setId( makeMemberCode() );
        if ( StringUtils.isNotBlank( mobileLogin.getInviterCode() ) ) {
            try {
                Long.parseLong( mobileLogin.getInviterCode() );
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
        m.setAccountNow( BigDecimal.ZERO );
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
        if ( StringUtils.isNotBlank( mobileLogin.getPasswd() ) ) {
            m.setPassword( mobileLogin.getPasswdEncoder() );
        }
        m.setNickName( m.getId() );
        m.setLoginNum( 0 );
        return m;
    }


    private void regChannelNotice( MobileLogin mobileLogin, Integer dev, String userId ) {
        String noticeUrl = configEnvCacheUtil.getConf( "channel_reg_notice" );
        if ( StringUtils.isBlank( noticeUrl ) ) {
            log.error( "平台无法找到环境变量channel_reg_notice,userId:{}", userId );
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put( "channel_id", mobileLogin.getChannelCode() );
        params.put( "invitation_code", mobileLogin.getInviterCode() );
        params.put( "account", userId );
        params.put( "device_type", dev == 2 ? "android" : "ios" );
        params.put( "ip", mobileLogin.getIp() );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>( params, headers );
        try {
            restTemplate.postForObject( noticeUrl, requestEntity, Object.class );
        } catch ( Exception e ) {
            try {
                restTemplate.postForObject( noticeUrl, requestEntity, Object.class );
            } catch ( Exception ex ) {
                log.error( ex.getMessage(), ex );
            }
        }
    }

    /**
     * 生成会员编号
     */
    private String makeMemberCode() {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong( Constants.MEMBER_CODE, redisUtils.getConnectionFactory() );
        return String.valueOf( Constants.MEMBER_CODE_INIT + entityIdCounter.getAndIncrement() );
    }
}
