package tv.game88.core.session.security.handle;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.manager.MemberTokenManager;
import tv.game88.core.member.vo.MemberLoginUser;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义退出处理类 返回成功
 *
 * @author MengJun
 */
@Configuration
public class MemberLogoutSuccessHandle implements LogoutSuccessHandler {
	@Resource
	private MemberTokenManager memberTokenManager;

	/**
	 * 退出处理
	 *
	 * @return
	 */
	@Override
	public void onLogoutSuccess( HttpServletRequest request, HttpServletResponse response, Authentication authentication ) {
		MemberLoginUser loginUser = memberTokenManager.getLoginUser( request );
		if ( StringUtils.isNotNull( loginUser ) ) {
			// 删除用户缓存记录
			memberTokenManager.delToken( loginUser.getUserId() );
		}
		ServletUtil.renderString( response, JsonUtil.object2Json( RspBase.ok( "退出登录" ) ) );
	}
}
