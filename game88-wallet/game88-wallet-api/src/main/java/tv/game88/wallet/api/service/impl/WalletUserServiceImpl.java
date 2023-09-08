package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.core.config.cache.SmsPhoneCacheUtil;
import tv.game88.core.utils.SmsApi;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.service.WalletUserService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * @author meng.jun
 * @description 针对表【wallet_user(钱包用户表)】的数据库操作Service实现
 * @createDate 2023-08-21 17:32:24
 */
@Log4j2
@Service
public class WalletUserServiceImpl extends ServiceImpl<WalletUserMapper, WalletUser> implements WalletUserService {
    @Resource
    private SmsPhoneCacheUtil     smsPhoneCacheUtil;
    @Resource
    private SmsApi                smsApi;
    @Resource
    private RedisUtils            redisUtils;
    @Resource
    private ConfigEnvCacheUtil    configEnvCacheUtil;
    @Resource
    private AuthenticationManager authenticationManager;

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
    public RspBase<RspMember> login( MobileLogin mobileLogin, Integer dev, String loginUrl ) {
        if ( StringUtils.isBlank( mobileLogin.getMobile() ) ) {
            return RspBase.businessError( "请输入手机号码" );
        }
        if ( mobileLogin.getMobile().length() != 11 ) {
            return RspBase.businessError( "请输入正确的手机号" );
        }
        if ( StringUtils.isBlank( mobileLogin.getPasswd() ) ) {
            return RspBase.businessError( "请输入登陆密码" );
        }

        WalletUser walletUser = new QueryChainWrapper<>( this.baseMapper ).eq( "phone", mobileLogin.getMobile() ).one();
        WalletUser oldm       = null;
        if ( walletUser == null ) {
            //检查是不是归档会员回归
            oldm = this.baseMapper.findMemberHistoryByMobile( mobileLogin.getMobile() );
            if ( oldm == null ) {
                return RspBase.businessError( "手机号不存在/密码错误" );
            }
            walletUser = oldm;
        }
        if ( walletUser.getStatus() == 0 ) {
            return RspBase.businessError( "您被限制登录,请联系客服" );
        }

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken( walletUser.getId(), mobileLogin.getPasswd() );
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
        log.info( "会员{}手机号密码登录IP:{}", walletUser.getId(), ip );

        WalletUser update = new WalletUser();
        update.setId( walletUser.getId() );
        update.setDeviceId( mobileLogin.getDeviceId() );
        this.setMemberLoginParam( mobileLogin, dev, loginUrl, update );

        if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getMobile(), 5 ) ) {
            return RspBase.businessError( "请勿重复登录" );
        }

        if ( oldm != null ) {
            this.baseMapper.insert( walletUser );
            this.baseMapper.deleteByHistoryKey( oldm.getId() );
        } else {
            this.baseMapper.updateById( update );
        }

        RspMember rspMember = new RspMember();
        BeanUtils.copyProperties( walletUser, rspMember );
        return RspBase.ok( "登录成功", rspMember );
    }

    private void setMemberLoginParam( MobileLogin mobileLogin, Integer dev, String loginUrl, WalletUser walletUser ) {
        walletUser.setLoginDevice( dev );
        walletUser.setLoginIp( mobileLogin.getIp() );
        walletUser.setLoginTime( LocalDateTime.now() );
        //手机型号
        if ( StringUtils.isNotBlank( mobileLogin.getPhoneModel() ) ) {
            walletUser.setPhoneModel( mobileLogin.getPhoneModel() );
        }
        if ( StringUtils.isNotBlank( loginUrl ) ) {
            walletUser.setLinkUrl( loginUrl );
        }
        if ( StringUtils.isNotBlank( mobileLogin.getPasswd() ) ) {
            walletUser.setPassword( mobileLogin.getPasswordEncrypt() );
        }
    }

    @Override
    public RspBase<RspMember> register( MobileLogin mobileLogin, Integer dev, String loginUrl ) {
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
        WalletUser walletUser = new QueryChainWrapper<>( this.baseMapper ).eq( "phone", mobileLogin.getMobile() ).one();
        WalletUser oldm       = null;
        if ( walletUser == null ) {
            //检查是不是归档会员回归
            oldm = this.baseMapper.findMemberHistoryByMobile( mobileLogin.getMobile() );
            if ( oldm != null ) {
                return RspBase.businessError( "您已注册过该手机号,请勿重复注册" );
            } else {
                walletUser = this.newWalletUserReg( mobileLogin );
            }
            this.setMemberLoginParam( mobileLogin, dev, loginUrl, walletUser );

            if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getMobile(), 5 ) ) {
                return RspBase.businessError( "请勿重复注册" );
            }

            this.baseMapper.insert( walletUser );
            if ( oldm != null ) {
                this.baseMapper.deleteByHistoryKey( oldm.getId() );
            }
        } else {
            return RspBase.businessError( "您已注册过该手机号,请勿重复注册" );
        }

        RspMember rspMember = new RspMember();
        BeanUtils.copyProperties( walletUser, rspMember );
        return RspBase.ok( "注册成功", rspMember );
    }

    private WalletUser newWalletUserReg( MobileLogin mobileLogin ) {
        WalletUser m = new WalletUser();
        m.setId( GenerateOrderCacheUtils.me.getOrderIdNoTime( 36 ) );
        m.setStatus( 1 );
        m.setIsVerified( 0 );
        m.setCreditRating( 1 );
        m.setCreatedTime( LocalDateTime.now() );
        m.setLoginIp( mobileLogin.getIp() );
        m.setAmount( BigDecimal.ZERO );
        m.setTotalCharge( BigDecimal.ZERO );
        m.setTotalSale( BigDecimal.ZERO );
        if ( StringUtils.isNotBlank( mobileLogin.getDeviceId() ) ) {
            m.setDeviceId( mobileLogin.getDeviceId() );
        }
        if ( StringUtils.isNotBlank( mobileLogin.getMobile() ) ) {
            m.setPhone( mobileLogin.getMobile() );
        }
        m.setNickName( "萌新" + RandomStringUtils.randomAlphanumeric( 8 ) );
        return m;
    }

    private RspBase<?> verificationPhoneCode( String phone, String code ) {
        if ( StringUtils.isBlank( phone ) ) {
            return RspBase.businessError( "请输入手机号" );
        }
        if ( phone.length() != 11 ) {
            return RspBase.businessError( "请输入正确的手机号" );
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
    public RspPayResult embeddedLogin( ReqEmbeddedLogin reqEmbeddedLogin ) {
        WalletUser walletUser = null;
        WalletUser oldm       = null;
        if ( StringUtils.isBlank( reqEmbeddedLogin.getWalletAddress() ) ) {
            walletUser = new QueryChainWrapper<>( this.baseMapper ).eq( "phone", reqEmbeddedLogin.getPhone() ).one();
            if ( walletUser == null ) {
                //检查是不是归档会员回归
                oldm = this.baseMapper.findMemberHistoryByMobile( reqEmbeddedLogin.getPhone() );
                if ( oldm != null ) {
                    walletUser = oldm;
                } else {
                    walletUser = this.newWalletUserReg( new MobileLogin() );
                    walletUser.setPhone( reqEmbeddedLogin.getPhone() );
                }
            }
        } else {
            walletUser = this.baseMapper.selectById( reqEmbeddedLogin.getWalletAddress() );
            if ( walletUser == null ) {
                //检查是不是归档会员回归
                oldm = this.baseMapper.findMemberHistoryById( reqEmbeddedLogin.getWalletAddress() );
                if ( oldm != null ) {
                    walletUser = oldm;
                } else {
                    return RspPayResult.businessError( "钱包地址有误" );
                }
            }
        }
        if ( !redisUtils.lock( "memberLogin:" + reqEmbeddedLogin.getPhone(), 5 ) ) {
            return RspPayResult.businessError( "请勿重复访问" );
        }
        this.baseMapper.insert( walletUser );
        if ( oldm != null ) {
            this.baseMapper.deleteByHistoryKey( oldm.getId() );
        }

        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put( "userInfo", this.baseMapper.selectPlatformUserByUserId( walletUser.getId() ) );
        return RspPayResult.ok( "成功", resultMap );
    }
}




