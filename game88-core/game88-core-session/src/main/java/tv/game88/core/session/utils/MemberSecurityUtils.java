package tv.game88.core.session.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.exception.BusinessException;
import tv.game88.core.session.vo.MemberLoginUser;

/**
 * 安全服务工具类
 *
 * @author MengJun
 */
@Log4j2
public class MemberSecurityUtils {

    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取用户ID
     **/
    public static String getUserId() {
        try {
            return getLoginUser().getUserId();
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( "获取用户账户异常", HttpStatus.UNAUTHORIZED );
        }
    }

    /**
     * 获取用户
     **/
    public static MemberLoginUser getLoginUser() {
        try {
            return ( MemberLoginUser ) getAuthentication().getPrincipal();
        } catch ( Exception e ) {
            throw new BusinessException( "获取用户信息异常", HttpStatus.UNAUTHORIZED );
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
        return bCryptPasswordEncoder.encode( password );
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
        return bCryptPasswordEncoder.matches( rawPassword, encodedPassword );
    }
}
