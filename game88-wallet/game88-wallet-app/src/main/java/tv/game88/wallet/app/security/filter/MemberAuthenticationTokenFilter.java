package tv.game88.wallet.app.security.filter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tv.game88.common.utils.StringUtils;
import tv.game88.wallet.app.manager.MemberTokenManager;
import tv.game88.wallet.app.utils.MemberSecurityUtils;
import tv.game88.wallet.app.vo.MemberLoginUser;

import java.io.IOException;

/**
 * token过滤器 验证token有效性
 *
 * @author MengJun
 */
@Component
public class MemberAuthenticationTokenFilter extends OncePerRequestFilter {
    @Resource
    private MemberTokenManager memberTokenManager;

    private final SecurityContextRepository repository = new RequestAttributeSecurityContextRepository();

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain ) throws ServletException, IOException {
        MemberLoginUser loginUser = memberTokenManager.getLoginUser( request );
        if ( StringUtils.isNotNull( loginUser ) && StringUtils.isNull( MemberSecurityUtils.getAuthentication() ) ) {
            memberTokenManager.refreshLoginUserCache( loginUser.getUserId() );
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken( loginUser, null,
                    loginUser.getAuthorities() );
            authenticationToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
            SecurityContextHolder.getContext().setAuthentication( authenticationToken );

            if ( "/api/stream/subscribe".equals( request.getRequestURI() ) ) {
                this.repository.saveContext( SecurityContextHolder.getContext(), request, response );
            }
        }
        chain.doFilter( request, response );

    }
}
