package tv.game88.admin.system.service.impl;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.constant.KeyConstants;
import tv.game88.core.admin.context.AuthenticationContextHolder;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.factory.AsyncFactory;
import tv.game88.core.admin.manager.AsyncManager;
import tv.game88.core.admin.service.ISysUserService;
import tv.game88.core.admin.service.impl.TokenService;
import tv.game88.core.admin.vo.LoginBody;
import tv.game88.core.admin.vo.LoginUser;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 登录校验方法
 *
 * @author MengJun
 */
@Log4j2
@Component
public class SysLoginService {
    @Resource
    private TokenService          tokenService;
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
    public Map<String, Object> login( LoginBody loginBody ) throws Exception {
        if ( !redisUtil.lock( "systemLogin:" + loginBody.getUsername(), 5 ) ) {
            return ImmutableMap.of( "msg", "正在登录中，请勿重复点击登录", "code", 500 );
        }
        SysUser user = userService.selectOtpSecretByUserName( loginBody.getUsername() );
        if ( user == null ) {
            return ImmutableMap.of( "msg", "获取用户账户异常", "code", 500 );
        }
        if ( StringUtils.isBlank( user.getOtpSecret() ) ) {
            return ImmutableMap.of( "msg", "请联系管理员绑定MFA验证秘钥", "code", 500 );
        }
        String otpSecretKey = RSACoder.decryptByPrivateKey( user.getOtpSecret(), KeyConstants.GOOGLE_AUTH_PRIVATE_KEY );
        if ( !GoogleAuthUtil.verifyCode( otpSecretKey, loginBody.getCode() ) ) {
            AsyncManager.me()
                        .execute( AsyncFactory.recordLogininfor( loginBody.getUsername(), AdminConstants.LOGIN_FAIL,
                                "MFA验证码错误" ) );
            return ImmutableMap.of( "msg", "MFA验证码不正确，请检查", "code", 500 );
        }

        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken( loginBody.getUsername(), loginBody.getPassword() );
            AuthenticationContextHolder.setContext( authenticationToken );
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate( authenticationToken );
        } catch ( Exception e ) {
            if ( e instanceof BadCredentialsException ) {
                String message = "用户不存在/密码错误";
                AsyncManager.me()
                            .execute( AsyncFactory.recordLogininfor( loginBody.getUsername(), AdminConstants.LOGIN_FAIL,
                                    message ) );
                return ImmutableMap.of( "msg", message, "code", 500 );
            } else {
                AsyncManager.me()
                            .execute( AsyncFactory.recordLogininfor( loginBody.getUsername(), AdminConstants.LOGIN_FAIL,
                                    e.getMessage() ) );
                return ImmutableMap.of( "msg", e.getMessage(), "code", 500 );
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }

        String ip = ServletUtil.getIp();
        log.info( "管理员{}登录IP:{}", loginBody.getUsername(), ip );

        AsyncManager.me()
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
        String              token     = tokenService.createToken( loginUser );
        Map<String, Object> resultMap = JsonUtil.object2Map( RspBase.ok() );
        resultMap.put( AdminConstants.TOKEN, token );
        redisUtil.unLock( "systemLogin:" + loginBody.getUsername() );
        return resultMap;
    }
}
