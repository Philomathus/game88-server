package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.MobileLogin;
import tv.game88.wallet.api.dto.Phone;
import tv.game88.wallet.api.dto.RspMember;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.app.manager.MemberTokenManager;

import javax.annotation.Resource;

@RestController
@Tag( name = "登录和初始化接口" )
@Log4j2
public class LoginController extends BaseController {
    @Resource
    private WalletUserService  walletUserService;
    @Resource
    private MemberTokenManager memberTokenManager;
    @Resource
    private PasswordEncoder    passwordEncoder;

    @Operation( summary = "发送短信验证码" )
    @PostMapping( "/api/sendSmsVerifyCode" )
    @Anonymous
    public RspBase<?> loginPasswd( @RequestBody @Validated Phone phone ) {
        return walletUserService.sendSmsVerifyCode( phone );
    }

    @Operation( summary = "手机号密码登录接口" )
    @PostMapping( "/api/login" )
    @Anonymous
    public RspBase<RspMember> loginPasswd( @RequestHeader( value = "frond-host", required = false ) String loginUrl,
                                           @RequestHeader( value = "dev", required = false ) Integer dev,
                                           @RequestBody @Validated MobileLogin mobileLogin ) {
        if ( StringUtils.isNotBlank( mobileLogin.getPasswd() ) ) {
            mobileLogin.setPasswordEncrypt( passwordEncoder.encode( mobileLogin.getPasswd() ) );
        }
        RspBase<RspMember> rspMemberRspBase = walletUserService.login( mobileLogin, dev, loginUrl );
        memberTokenManager.setRspMemberToken( rspMemberRspBase.getData(), mobileLogin.getIp() );
        return rspMemberRspBase;
    }

    @Operation( summary = "注册接口", description = "手机号验证码注册,同时也会更新密码以及直接登录" )
    @PostMapping( "/api/register" )
    @Anonymous
    public RspBase<RspMember> register( @RequestHeader( value = "frond-host", required = false ) String loginUrl,
                                        @RequestHeader( value = "dev", required = false ) Integer dev,
                                        @RequestBody @Validated MobileLogin mobileLogin ) {
        if ( StringUtils.isNotBlank( mobileLogin.getPasswd() ) ) {
            mobileLogin.setPasswordEncrypt( passwordEncoder.encode( mobileLogin.getPasswd() ) );
        }
        RspBase<RspMember> rspMemberRspBase = walletUserService.register( mobileLogin, dev, loginUrl );
        memberTokenManager.setRspMemberToken( rspMemberRspBase.getData(), mobileLogin.getIp() );
        return rspMemberRspBase;
    }
}
