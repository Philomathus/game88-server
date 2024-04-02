package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.core.config.cache.SmsPhoneCacheUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.utils.SmsApi;
import tv.game88.wallet.api.cache.WalletMerchantCacheUtil;
import tv.game88.wallet.api.constants.ReqConstant;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletMerchant;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.manager.WalletFundManager;
import tv.game88.wallet.api.mapper.WalletUserFundLogMapper;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.service.WalletRecordService;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.api.type.WalletUserFundEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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
    private SmsPhoneCacheUtil       smsPhoneCacheUtil;
    @Resource
    private SmsApi                  smsApi;
    @Resource
    private RedisUtils              redisUtils;
    @Resource
    private ConfigEnvCacheUtil      configEnvCacheUtil;
    @Resource
    private WalletMerchantCacheUtil walletMerchantCacheUtil;
    @Resource
    private AuthenticationManager   authenticationManager;
    @Resource
    private PasswordEncoder         passwordEncoder;

    @Resource
    private WalletUserFundLogMapper walletUserFundLogMapper;
    @Resource
    private WalletFundManager       walletFundManager;
    @Resource
    @Lazy
    private WalletRecordService     walletRecordService;

    @Resource
    private WalletUserMapper walletUserMapper;

    /**
     * 查询钱包用户列表
     *
     * @param walletUser 钱包用户
     *
     * @return 钱包用户
     */
    @Override
    public List<WalletUser> selectWalletUserList( WalletUser walletUser ) {
        return this.baseMapper.selectWalletUserList( walletUser );
    }

    @Override
    public RspBase<?> sendSmsVerifyCode( Phone phone ) {
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
        if ( walletUser.getStatus() != 1 ) {
            return RspBase.businessError( "用户状态异常,请联系客服" );
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
            walletUser.setPassword( passwordEncoder.encode( mobileLogin.getPasswd() ) );
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

        RspBase rspBase = this.verificationPhoneCode( mobileLogin.getMobile(), mobileLogin.getCode() );
        if ( rspBase != null ) {
            return rspBase;
        }
        WalletUser walletUser = new QueryChainWrapper<>( this.baseMapper ).eq( "phone", mobileLogin.getMobile() ).one();
        WalletUser oldm       = null;
        if ( walletUser == null ) {
            //检查是不是归档会员回归
            oldm = this.baseMapper.findMemberHistoryByMobile( mobileLogin.getMobile() );

            walletUser = Objects.requireNonNullElseGet( oldm, () -> this.newWalletUserReg( mobileLogin ) );

            this.setMemberLoginParam( mobileLogin, dev, loginUrl, walletUser );

            if ( !redisUtils.lock( "memberLogin:" + mobileLogin.getMobile(), 5 ) ) {
                return RspBase.businessError( "请勿重复注册" );
            }

            this.baseMapper.insert( walletUser );
            if ( oldm != null ) {
                this.baseMapper.deleteByHistoryKey( oldm.getId() );
            }
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
        m.setLevel( 1 );
        m.setCreatedTime( LocalDateTime.now() );
        m.setLoginIp( mobileLogin.getIp() );
        m.setAmount( 0L );
        m.setTotalCharge( 0L );
        m.setTotalSale( 0L );
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
    public RspBase<?> embeddedLogin( ReqEmbeddedLogin reqEmbeddedLogin ) {
        WalletMerchant walletMerchant = walletMerchantCacheUtil.getWalletMerchantCache( reqEmbeddedLogin.getMerchantId() );
        RspBase rspBase = walletRecordService.validated( reqEmbeddedLogin, walletMerchant, reqEmbeddedLogin.getWalletAddress() );
        if ( rspBase != null ) {
            return rspBase;
        }
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
                    walletUser.setPlatformId( reqEmbeddedLogin.getUserId() );
                    if ( StringUtils.isNotBlank( reqEmbeddedLogin.getRealName() ) ) {
                        walletUser.setRealName( reqEmbeddedLogin.getRealName() );
                    }
                    log.info( "this is details id and userid {} , {}", reqEmbeddedLogin.getPhone(),
                            reqEmbeddedLogin.getUserId() );

                    this.baseMapper.insert( walletUser );
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
                    return RspBase.businessError( "钱包地址不存在" );
                }
            }
        }
        if ( !redisUtils.lock( "memberLogin:" + reqEmbeddedLogin.getPhone(), 5 ) ) {
            return RspBase.businessError( "请勿重复访问" );
        }
        if ( oldm != null ) {
            this.baseMapper.deleteByHistoryKey( oldm.getId() );
            this.baseMapper.insert( walletUser );
        }

        if ( walletUser.getFrozenAmount() == null ) {
            walletUser.setFrozenAmount( 0L );
        }

        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put( "url", configEnvCacheUtil.getConf( "pay_host_url" ) );
        resultMap.put( "userInfo", this.baseMapper.selectPlatformUserByUserId( walletUser.getId() ) );
        resultMap.put( "walletAddress", walletUser.getId() );
        resultMap.put( "realName", walletUser.getRealName() );
        resultMap.put( "balance", walletUser.getAmount() + walletUser.getFrozenAmount() );
        return RspBase.ok( "成功", resultMap );
    }

    @Override
    public RspBase<?> resetPasswd( ReqResetPasswd reqResetPasswd, String userId ) {
        String passwd = this.baseMapper.getUserPasswd( userId );
        if ( StringUtils.isBlank( passwd ) ) {
            // TODO
        }
        if ( !passwordEncoder.matches( reqResetPasswd.getOldPasswd(), passwd ) ) {
            return RspBase.businessError( "原登录密码错误" );
        }
        WalletUser update = new WalletUser();
        update.setId( userId );
        update.setPassword( passwordEncoder.encode( reqResetPasswd.getNewPasswd() ) );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok( "登录密码更新成功" ) : RspBase.businessError( "登录密码更新失败,请重试" );
    }

    @Override
    public RspBase<RspAmount> getAmount( String userId ) {
        RspAmount rspAmount = new RspAmount();
        rspAmount.setAmount( this.baseMapper.getUserMoney( userId ) );
        return RspBase.ok( rspAmount );
    }

    @Override
    public RspBase<RspMember> getUserInfo( String userId ) {
        WalletUser walletUser = this.baseMapper.selectById( userId );
        RspMember  rspMember  = new RspMember();
        rspMember.setCreditRating( 5 );
        rspMember.setAmount( new BigDecimal( walletUser.getAmount() + walletUser.getFrozenAmount() ) );
        rspMember.setSellAbleAmount( new BigDecimal( walletUser.getAmount() ) );
        BeanUtils.copyProperties( walletUser, rspMember );

        rspMember.setHasPassword( StringUtils.isNotBlank( walletUser.getPassword() ) );
        rspMember.setHasFundPassword( StringUtils.isNotBlank( walletUser.getFundPassword() ) );
        return RspBase.ok( rspMember );
    }

    @Override
    public RspBase<?> fundPassSet( String userId, ReqFundPass reqFundPass ) {
        WalletUser walletUser = new QueryChainWrapper<>( this.baseMapper )
                .eq( "id", userId )
                .select( "id", "fund_password" )
                .one();
        if ( walletUser == null ) {
            return RspBase.businessError( "用户不存在" );
        }
        if ( StringUtils.isNotBlank( walletUser.getFundPassword() ) ) {
            return RspBase.businessError( "已经设置过资金密码" );
        }
        WalletUser update = new WalletUser();
        update.setId( userId );
        update.setFundPassword( passwordEncoder.encode( reqFundPass.getFundPass() ) );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "设置资金密码异常，请稍后再试" );
    }

    @Override
    public List<RspLogFund> getFundDetails( String userId, ReqLogFund reqLogFund ) {
        String           beginDay     = reqLogFund.getEnumReqTime().getBeginDayTime();
        String           endDay       = reqLogFund.getEnumReqTime().getEndDayTime();
        List<RspLogFund> logMoneyList = walletUserFundLogMapper.findLogFundList( userId, reqLogFund, beginDay, endDay );
        for ( RspLogFund rspLogMoney : logMoneyList ) {
            WalletUserFundEnum byType = WalletUserFundEnum.getByType( rspLogMoney.getType() );
            if ( byType != null ) {
                rspLogMoney.setDes( byType.getDes() );
            }
            rspLogMoney.setAmount( rspLogMoney
                    .getIncome()
                    .subtract( rspLogMoney.getPay() )
                    .setScale( 2, RoundingMode.HALF_DOWN ) );
        }
        return logMoneyList;
    }

    @Override
    public RspBase<?> personalTransfer( String userId, ReqPersonalTransfer reqPersonalTransfer ) {
        WalletUser walletUser = this.baseMapper.selectById( userId );
        RspBase<?> rspBase    = this.validWalletUser( walletUser );
        if ( rspBase != null ) {
            return rspBase;
        }

        rspBase = this.validatedPasswordTimes( reqPersonalTransfer.getFundPass(), walletUser );
        if ( rspBase != null ) {
            return rspBase;
        }

        if ( walletUser.getAmount().compareTo( reqPersonalTransfer.getAmount() ) < 0 ) {
            return RspBase.businessError( "您的余额不足" );
        }

        WalletUser toTransferUser = this.baseMapper.selectById( reqPersonalTransfer.getWalletUserAddress() );
        if ( toTransferUser == null ) {
            return RspBase.businessError( "对方钱包用户不存在" );
        }
        if ( toTransferUser.getStatus() != 1 ) {
            return RspBase.businessError( "对方用户状态异常,请联系客服" );
        }
        if ( toTransferUser.getIsVerified() < 2 ) {
            return RspBase.businessError( "对方未实名或实名未认证" );
        }

        SpringUtils.getBean( WalletUserService.class ).processFundTransfer( userId, reqPersonalTransfer );

        return RspBase.ok( "转账成功" );
    }

    @Override
    public RspBase validatedPasswordTimes( String rawPassword, WalletUser walletUser ) {
        if ( StringUtils.isBlank( walletUser.getFundPassword() ) ) {
            return RspBase.businessError( "请设置资金密码" );
        }
        String key = Constants.WALLET_PREX + "lock:fundPassword:" + walletUser.getId();
        if ( redisUtils.exists( key ) && Long.parseLong( redisUtils.strGet( key ) ) >= 5 ) {
            return RspBase.businessError(
                    "资金密码错误过多，账号被锁定" + LocalDateTimeUtils.secondsToTime( redisUtils.getExpire( key ) )
                            + ",请联系客服重置" );
        }
        if ( !passwordEncoder.matches( rawPassword, walletUser.getFundPassword() ) ) {
            Long num = redisUtils.strIncrement( key );
            redisUtils.expire( key, Duration.ofDays( 1 ) );
            if ( num >= 5 ) {
                return RspBase.businessError( "资金密码错误5次，账号被锁定一天,请联系客服重置" );
            }
            return RspBase.businessError( "资金密码错误，请重新输入" );
        } else {
            if ( redisUtils.exists( key ) ) {
                redisUtils.unlink( key );
            }
        }
        return null;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void processFundTransfer( String userId, ReqPersonalTransfer reqPersonalTransfer ) {
        String currentOrderNo = GenerateOrderCacheUtils.me.getOrderId( "PTO", 5 );
        String otherOrderNo   = GenerateOrderCacheUtils.me.getOrderId( "PTI", 5 );

        Long amount = reqPersonalTransfer.getAmount();

        // 扣除当前会员金额
        WalletUserFundEnum userFundEnum = WalletUserFundEnum.PERSONAL_TRANSFER_OUT;
        String             userMark     = "用户" + userFundEnum.getDes() + amount;
        walletFundManager.reduceWalletUserMoney( userId, null, amount, userFundEnum, userMark, currentOrderNo, otherOrderNo );

        // 增加对方会员金额
        WalletUserFundEnum otherFundEnum = WalletUserFundEnum.PERSONAL_TRANSFER_IN;
        String             otherMark     = "用户" + otherFundEnum.getDes() + amount;
        walletFundManager.addWalletUserMoney( reqPersonalTransfer.getWalletUserAddress(), null, amount, otherFundEnum,
                otherMark, otherOrderNo, currentOrderNo );
    }

    @Override
    public RspBase<?> validWalletUser( WalletUser walletUser ) {
        if ( walletUser == null ) {
            return RspBase.businessError( "钱包用户不存在" );
        }
        if ( walletUser.getStatus() != 1 ) {
            return RspBase.businessError( "用户状态异常,请联系客服" );
        }
        if ( walletUser.getIsVerified() < 2 ) {
            return RspBase.businessError( "用户未实名或实名未认证" );
        }
        return null;
    }

    @Override
    public RspBase<?> verifyIdCard( String userId, ReqVerifyIdCard reqVerifyIdCard ) {
        WalletUser walletUser = this.baseMapper.selectById( userId );
        if ( walletUser == null ) {
            return RspBase.businessError( "钱包用户不存在" );
        }
        if ( StringUtils.isBlank( reqVerifyIdCard.getRealName() ) ) {
            return RspBase.businessError( "需要实名" );
        }
        if ( walletUser.getStatus() != 1 ) {
            return RspBase.businessError( "用户状态异常,请联系客服" );
        }
        if ( ValidatorUtil.isIDCard( reqVerifyIdCard.getIdCardNumber() ) ) {
            return RspBase.businessError( "请输入正确的身份证号码" );
        }
        WalletUser update = new WalletUser();
        update.setId( userId );
        update.setIsVerified( 1 );
        update.setIdNumber( reqVerifyIdCard.getIdCardNumber() );
        update.setRealName( reqVerifyIdCard.getRealName() );
        update.setIdFrontPic( reqVerifyIdCard.getIdFrontPic() );
        update.setIdBackPic( reqVerifyIdCard.getIdBackPic() );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "申请身份认证异常，请稍后再试" );
    }

    @Override
    public RspBase<?> setPassword( String userId, ReqConstant.ReqSetPasswd reqSetPasswd ) {
        WalletUser walletUser = new QueryChainWrapper<>( this.baseMapper ).eq( "id", userId ).select( "id", "password" ).one();

        if ( walletUser == null ) {
            return RspBase.businessError( "钱包用户不存在" );
        }
        if ( StringUtils.isNotBlank( walletUser.getPassword() ) ) {
            return RspBase.businessError( "钱包用户密码已经存在。如果你忘记了，请重新设置!" );
        }
        if ( passwordEncoder.matches( reqSetPasswd.password(), walletUser.getPassword() ) ) {
            return RspBase.businessError( "密码不能与已有密码相同!" );
        }
        if ( !reqSetPasswd.password().equals( reqSetPasswd.confirmPassword() ) ) {
            return RspBase.businessError( "密码和确认密码必须匹配!" );
        }

        WalletUser updateUser = new WalletUser();
        updateUser.setId( userId );
        updateUser.setPassword( passwordEncoder.encode( reqSetPasswd.password() ) );
        return this.baseMapper.updateById( updateUser )
                > 0 ? RspBase.ok() : RspBase.businessError( "申请身份认证异常，请稍后再试" );
    }

    @Override
    public RspBase<?> resetFunPassword( String userId, ReqConstant.ReqResetFundPasswd reqResetFundPasswd ) {
        WalletUser walletUser = new QueryChainWrapper<>( this.baseMapper )
                .eq( "id", userId )
                .select( "id", "fund_password" )
                .one();

        if ( walletUser == null ) {
            return RspBase.businessError( "钱包用户不存在!" );
        }
        if ( StringUtils.isBlank( walletUser.getFundPassword() ) ) {
            return RspBase.businessError( "资金密码不存在,请设置您的资金密码!" );
        }
        if ( !passwordEncoder.matches( reqResetFundPasswd.fundOldPass(), walletUser.getFundPassword() ) ) {
            return RspBase.businessError( "你以前的基金密码不匹配!" );
        }
        if ( passwordEncoder.matches( reqResetFundPasswd.fundNewPass(), walletUser.getFundPassword() ) ) {
            return RspBase.businessError( "密码不能与已有密码相同!" );
        }

        WalletUser update = new WalletUser();
        update.setId( userId );
        update.setFundPassword( passwordEncoder.encode( reqResetFundPasswd.fundNewPass() ) );

        return this.baseMapper.updateById( update ) > 0 ? RspBase.ok() : RspBase.businessError( "申请身份认证异常，请稍后再试" );
    }

    @Override
    public void addBuyerTransactionSuccess( String id, Long money ) {
        walletUserMapper.addBuyerTransactionSuccess( id, money );
    }

    @Override
    public void addSellerTransactionSuccess( String id, Long money ) {
        walletUserMapper.addSellerTransactionSuccess( id, money );
    }

    @Override
    public void addSellerTotalSellingAmount( String id, Long amount ) {
        walletUserMapper.addSellerTotalSellingAmount( id, amount );
    }

    @Override
    public void addSellerOngoingSellingAmount( String id, Long amount ) {
        walletUserMapper.addSellerOngoingSellingAmount( id, amount );
    }

    @Override
    public void addSellerInitCancelSell( String id, Long amount ) {
        walletUserMapper.addSellerInitCancelSell( id, amount );
    }

    @Override
    public void addSellerCancelSellingAmount( String id, Long amount ) {
        walletUserMapper.addSellerCancelSellingAmount( id, amount );
    }
}




