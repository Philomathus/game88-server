package tv.game88.core.admin.security.filter;

import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.security.service.SysUserTokenService;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.admin.vo.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * token过滤器 验证token有效性
 *
 * @author MengJun
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
	@Resource
	private SysUserTokenService sysUserTokenService;

	@Override
	protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
			throws ServletException, IOException {
		LoginUser loginUser = sysUserTokenService.getLoginUser( request );
		if ( StringUtils.isNotNull( loginUser ) && StringUtils.isNull( SecurityUtils.getAuthentication() ) ) {
			sysUserTokenService.refreshToken( loginUser );
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken( loginUser, null,
					loginUser.getAuthorities() );
			authenticationToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
			SecurityContextHolder.getContext().setAuthentication( authenticationToken );
		}
		chain.doFilter( request, response );
	}
}
