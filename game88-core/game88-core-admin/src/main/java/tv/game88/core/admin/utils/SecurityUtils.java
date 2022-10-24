package tv.game88.core.admin.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.GoogleAuthUtil;
import tv.game88.common.utils.RSACoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.constant.KeyConstants;
import tv.game88.core.admin.exception.CustomException;
import tv.game88.core.admin.vo.LoginUser;

/**
 * 安全服务工具类
 *
 * @author MengJun
 */
@Log4j2
public class SecurityUtils {
    /**
     * 获取用户账户
     **/
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch ( Exception e ) {
            throw new CustomException( "获取用户账户异常", HttpStatus.UNAUTHORIZED );
        }
    }

    /**
     * 获取用户
     **/
    public static LoginUser getLoginUser() {
        try {
            return ( LoginUser ) getAuthentication().getPrincipal();
        } catch ( Exception e ) {
            throw new CustomException( "获取用户信息异常", HttpStatus.UNAUTHORIZED );
        }
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     *
     * @return 加密字符串
     */
    public static String encryptPassword( String password ) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode( password );
    }

    /**
     * 校验otp密钥
     *
     * @param otpCode otp密钥
     *
     * @return 是否校验成功
     */
    public static void verifyOTPCode( int otpCode ) throws Exception {
        String otpSecret = getLoginUser().getUser().getOtpSecret();
        if (StringUtils.isBlank( otpSecret )) {
            throw new BusinessException( "请联系管理员绑定MFA验证秘钥" );
        }
        try {
            String otpSecretKey = RSACoder.decryptByPrivateKey( otpSecret, KeyConstants.GOOGLE_AUTH_PRIVATE_KEY );
            if (!GoogleAuthUtil.verifyCode( otpSecretKey, otpCode )) {
                throw new BusinessException( "OTP验证码不正确，请检查" );
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw e;
        }
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     *
     * @return 结果
     */
    public static boolean matchesPassword( String rawPassword, String encodedPassword ) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches( rawPassword, encodedPassword );
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     *
     * @return 结果
     */
    public static boolean isAdmin( Long userId ) {
        return userId != null && 1L == userId;
    }
}
