package tv.game88.core.session.security.handle;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.vo.RspBase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 认证失败处理类 返回未授权
 *
 * @author MengJun
 */
@Component
public class MemberAuthenticationEntryPointHandle implements AuthenticationEntryPoint, Serializable {
    private static final long serialVersionUID = -897071841347577606L;

    @Override
    public void commence( HttpServletRequest request, HttpServletResponse response, AuthenticationException e ) {
        ServletUtil.renderString( response, JsonUtil.object2Json( RspBase.sessionError( "认证失败，请重新登录" ) ) );
    }
}
