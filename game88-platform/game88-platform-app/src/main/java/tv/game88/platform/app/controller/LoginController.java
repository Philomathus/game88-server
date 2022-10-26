package tv.game88.platform.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.dto.RspMember;
import tv.game88.core.session.service.MemberTokenService;
import tv.game88.platform.api.dto.MobileLogin;
import tv.game88.platform.api.dto.RspInit;
import tv.game88.platform.api.dto.RspManUpdateVersion;
import tv.game88.platform.api.service.MemberInfoService;

import javax.annotation.Resource;

@RestController
@Tag( name = "登录和初始化接口" )
@Log4j2
public class LoginController {
    @Resource
    private MemberInfoService  memberInfoService;
    @Resource
    private MemberTokenService memberTokenService;

    @Operation( summary = "初始化接口" )
    @PostMapping( "/init" )
    public RspBase<RspInit> loginInit( @RequestHeader( "dev" ) Integer dev, @RequestHeader( "version" ) String version ) {
        if ( dev == null || StringUtils.isBlank( version ) ) {
            return RspBase.businessError( "客户端版本较低" );
        }
        return RspBase.ok( memberInfoService.getLoginInit( dev, version ) );
    }

    @Operation( summary = "人工更新请求版本" )
    @PostMapping( "/check-update" )
    public RspBase<RspManUpdateVersion> getManUpdateVersion( @RequestHeader( "dev" ) Integer dev,
                                                             @RequestHeader( "version" ) String version ) {
        return RspBase.ok( memberInfoService.checkManUpdateVersion( dev, version ) );
    }

    @Operation( summary = "手机号密码登录接口" )
    @PostMapping( "/login" )
    public RspBase<RspMember> login( @RequestBody MobileLogin mobileLogin ) {
        RspBase<RspMember> rspMemberRspBase = memberInfoService.login( mobileLogin );
        memberTokenService.setRspMemberToken( rspMemberRspBase.getData(), mobileLogin.getIp() );
        return rspMemberRspBase;
    }

    @Operation( summary = "设备登录接口", description = "设备号登录,也称游客登录" )
    @PostMapping( "/login-device" )
    public RspBase<RspMember> loginDevice( @RequestHeader( value = "frond-host", required = false ) String loginUrl,
                                           @RequestHeader( "dev" ) Integer dev, @RequestHeader( "version" ) String version,
                                           @RequestBody MobileLogin mobileLogin ) {
        RspBase<RspMember> rspMemberRspBase = memberInfoService.loginDevice( mobileLogin, dev, version, loginUrl );
        memberTokenService.setRspMemberToken( rspMemberRspBase.getData(), mobileLogin.getIp() );
        return rspMemberRspBase;
    }

    @Operation( summary = "注册接口", description = "手机号验证码注册,同时也会更新密码以及直接登录" )
    @PostMapping( "/register" )
    public RspBase<RspMember> register( @RequestBody MobileLogin mobileLogin ) {
        RspBase<RspMember> rspMemberRspBase = memberInfoService.register( mobileLogin );
        memberTokenService.setRspMemberToken( rspMemberRspBase.getData(), mobileLogin.getIp() );
        return rspMemberRspBase;
    }
}
