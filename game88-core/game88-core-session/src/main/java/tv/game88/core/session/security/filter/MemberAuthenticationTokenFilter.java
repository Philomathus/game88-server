package tv.game88.core.session.security.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.member.service.MemberTokenService;
import tv.game88.core.member.utils.MemberSecurityUtils;
import tv.game88.core.member.vo.MemberLoginUser;

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
public class MemberAuthenticationTokenFilter extends OncePerRequestFilter {
	@Resource
	private MemberTokenService memberTokenService;

	@Override
	protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
			throws ServletException, IOException {
		MemberLoginUser loginUser = memberTokenService.getLoginUser( request );
		if ( StringUtils.isNotNull( loginUser ) && StringUtils.isNull( MemberSecurityUtils.getAuthentication() ) ) {
			memberTokenService.refreshLoginUserCache( loginUser );
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken( loginUser, null,
					loginUser.getAuthorities() );
			authenticationToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
			SecurityContextHolder.getContext().setAuthentication( authenticationToken );
		}
		chain.doFilter( request, response );
	}
}
