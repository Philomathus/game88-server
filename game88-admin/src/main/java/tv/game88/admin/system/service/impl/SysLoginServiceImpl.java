package tv.game88.admin.system.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tv.game88.admin.system.service.ISysLoginService;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.constant.KeyConstants;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.factory.AsyncFactory;
import tv.game88.core.admin.manager.AsyncManager;
import tv.game88.core.admin.security.service.SysUserTokenService;
import tv.game88.core.admin.service.ISysUserService;
import tv.game88.core.admin.vo.LoginBody;
import tv.game88.core.admin.vo.LoginUser;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;

/**
 * 登录校验方法
 *
 * @author MengJun
 */
@Log4j2
@Service
public class SysLoginServiceImpl implements ISysLoginService {
    @Resource
    private SysUserTokenService   sysUserTokenService;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private ISysUserService       userService;
    @Resource
    private RedisUtils            redisUtil;

    /**
     * 登录验证
     *
     * @param loginBody 登录信息
     *
     * @return 结果
     */
    public RspBase<String> login( LoginBody loginBody ) throws Exception {
        if ( !redisUtil.lock( "systemLogin:" + loginBody.getUsername(), 5 ) ) {
            return RspBase.businessError( "正在登录中，请勿重复点击登录" );
        }
        SysUser user = userService.selectOtpSecretByUserName( loginBody.getUsername() );
        if ( user == null ) {
            return RspBase.businessError( "获取用户账户异常" );
        }
        if ( StringUtils.isBlank( user.getOtpSecret() ) ) {
            return RspBase.businessError( "请联系管理员绑定MFA验证秘钥" );
        }
        String otpSecretKey = RSACoder.decryptByPrivateKey( user.getOtpSecret(), KeyConstants.GOOGLE_AUTH_PRIVATE_KEY );
        if ( !GoogleAuthUtil.verifyCode( otpSecretKey, loginBody.getCode() ) ) {
            AsyncManager
                    .me()
                    .execute( AsyncFactory.recordLogininfor( loginBody.getUsername(), AdminConstants.LOGIN_FAIL, "MFA验证码错误" ) );
            return RspBase.businessError( "MFA验证码不正确，请检查" );
        }

        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken( loginBody.getUsername(), loginBody.getPassword() );
            AuthContextHolderUtils.setContext( authenticationToken );
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate( authenticationToken );
        } catch ( Exception e ) {
            AuthContextHolderUtils.clearContext();
            if ( e instanceof BadCredentialsException ) {
                String message = "用户不存在/密码错误";
                AsyncManager
                        .me()
                        .execute( AsyncFactory.recordLogininfor( loginBody.getUsername(), AdminConstants.LOGIN_FAIL,
                                message + ":::" + loginBody.getPassword() ) );
                return RspBase.businessError( message );
            } else {
                AsyncManager
                        .me()
                        .execute( AsyncFactory.recordLogininfor( loginBody.getUsername(), AdminConstants.LOGIN_FAIL,
                                e.getMessage() ) );
                return RspBase.businessError( e.getMessage() );
            }
        } finally {
            AuthContextHolderUtils.clearContext();
        }

        String ip = ServletUtil.getIp();
        log.info( "管理员{}登录IP:{}", loginBody.getUsername(), ip );

        AsyncManager
                .me()
                .execute( AsyncFactory.recordLogininfor( loginBody.getUsername(), AdminConstants.LOGIN_SUCCESS, "登录成功" ) );
        LoginUser loginUser = ( LoginUser ) authentication.getPrincipal();

        SysUser sysUser = new SysUser();
        sysUser.setUserId( loginUser.getUser().getUserId() );
        sysUser.setLoginDate( LocalDateTime.now() );
        sysUser.setLoginIp( ip );
        userService.updateUserLoginTime( sysUser );

        loginUser.setLoginTime( sysUser.getLoginDate() );
        loginUser.setIpaddr( sysUser.getLoginIp() );

        // 生成token
        String token = sysUserTokenService.createToken( loginUser );
        redisUtil.unLock( "systemLogin:" + loginBody.getUsername() );
        return RspBase.ok( "登录成功", token );
    }
}
