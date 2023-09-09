package tv.game88.core.session.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.session.vo.MemberLoginUser;

/**
 * 安全服务工具类
 *
 * @author MengJun
 */
@Log4j2
public class MemberSecurityUtils {

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
            log.warn( JsonUtil.object2Json( getAuthentication().getPrincipal() ) );
            log.error( e.getMessage(), e );
            throw new BusinessException( "获取用户信息异常", HttpStatus.UNAUTHORIZED );
        }
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
