package tv.game88.core.admin.security.handle;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.factory.AsyncFactory;
import tv.game88.core.admin.manager.AsyncManager;
import tv.game88.core.admin.service.impl.TokenService;
import tv.game88.core.admin.vo.LoginUser;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义退出处理类 返回成功
 *
 * @author MengJun
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
	@Resource
	private TokenService tokenService;

	/**
	 * 退出处理
	 *
	 * @return
	 */
	@Override
	public void onLogoutSuccess( HttpServletRequest request, HttpServletResponse response, Authentication authentication ) {
		LoginUser loginUser = tokenService.getLoginUser( request );
		if ( StringUtils.isNotNull( loginUser ) ) {
			// 删除用户缓存记录
			tokenService.delToken( loginUser.getUser().getUserId() );
			// 记录用户退出日志
			AsyncManager.me().execute( AsyncFactory.recordLogininfor( loginUser.getUsername(), AdminConstants.LOGOUT, "退出成功" ) );
		}
		ServletUtil.renderString( response, JsonUtil.object2Json( RspBase.ok( "退出成功" ) ) );
	}
}
