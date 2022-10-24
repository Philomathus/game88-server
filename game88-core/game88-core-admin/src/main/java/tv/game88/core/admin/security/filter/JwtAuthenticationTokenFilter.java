package tv.game88.core.admin.security.filter;

import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.service.impl.TokenService;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.admin.vo.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * token过滤器 验证token有效性
 *
 * @author MengJun
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
	@Resource
	private TokenService tokenService;

	@Override
	protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
			throws ServletException, IOException {
		LoginUser loginUser = tokenService.getLoginUser( request );
		if ( StringUtils.isNotNull( loginUser ) && StringUtils.isNull( SecurityUtils.getAuthentication() ) ) {
			tokenService.refreshToken( loginUser );
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken( loginUser, null,
					loginUser.getAuthorities() );
			authenticationToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
			SecurityContextHolder.getContext().setAuthentication( authenticationToken );
		}
		chain.doFilter( request, response );
	}
}
