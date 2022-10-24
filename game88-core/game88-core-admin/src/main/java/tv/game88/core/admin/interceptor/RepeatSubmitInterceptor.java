package tv.game88.core.admin.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.RepeatSubmit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 防止重复提交拦截器
 *
 * @author MengJun
 */
@Component
public abstract class RepeatSubmitInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler ) throws Exception {
		if ( handler instanceof HandlerMethod ) {
			HandlerMethod handlerMethod = ( HandlerMethod ) handler;
			Method       method     = handlerMethod.getMethod();
			RepeatSubmit annotation = method.getAnnotation( RepeatSubmit.class );
			if ( annotation != null ) {
				if ( this.isRepeatSubmit( request ) ) {
					RspBase<?> result = RspBase.businessError( "不允许重复提交，请稍后再试" );
					ServletUtil.renderString( response, JsonUtil.object2Json( result ) );
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 验证是否重复提交由子类实现具体的防重复提交的规则
	 *
	 * @return
	 * @throws Exception
	 */
	public abstract boolean isRepeatSubmit( HttpServletRequest request );
}
