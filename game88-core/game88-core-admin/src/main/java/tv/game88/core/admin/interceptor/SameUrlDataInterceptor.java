package tv.game88.core.admin.interceptor;

import tv.game88.common.utils.HttpHelper;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.filter.RepeatedlyRequestWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 判断请求url和数据是否和上一次相同， 如果和上次相同，则是重复提交表单。 有效时间为10秒内。
 *
 * @author MengJun
 */
@Component
public class SameUrlDataInterceptor extends RepeatSubmitInterceptor {
	public final String    REPEAT_PARAMS = "repeatParams";
	public final String    REPEAT_TIME   = "repeatTime";
	@Resource
	private RedisUtils redisUtil;

	// 令牌自定义标识
	@Value( "${token.header}" )
	private String header;

	/**
	 * 间隔时间，单位:秒 默认10秒
	 * <p>
	 * 两次相同参数的请求，如果间隔时间大于该参数，系统不会认定为重复提交的数据
	 */
	private int intervalTime = 10;

	public void setIntervalTime( int intervalTime ) {
		this.intervalTime = intervalTime;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean isRepeatSubmit( HttpServletRequest request ) {
		String nowParams = "";
		if ( request instanceof RepeatedlyRequestWrapper ) {
			RepeatedlyRequestWrapper repeatedlyRequest = ( RepeatedlyRequestWrapper ) request;
			nowParams = HttpHelper.getBodyString( repeatedlyRequest );
		}

		// body参数为空，获取Parameter的数据
		if ( StringUtils.isBlank( nowParams ) ) {
			nowParams = JsonUtil.object2Json( request.getParameterMap() );
		}
		Map<String, Object> nowDataMap = new HashMap<>();
		nowDataMap.put( REPEAT_PARAMS, nowParams );
		nowDataMap.put( REPEAT_TIME, System.currentTimeMillis() );

		// 请求地址（作为存放cache的key值）
		String url = request.getRequestURI();

		// 唯一值（没有消息头则使用请求地址）
		String submitKey = request.getHeader( header );
		if ( StringUtils.isBlank( submitKey ) ) {
			submitKey = url;
		}

		// 唯一标识（指定key + 消息头）
		String cache_repeat_key = AdminConstants.REPEAT_SUBMIT_KEY + submitKey;

		String sessionObj = redisUtil.strGet( cache_repeat_key );
		if ( sessionObj != null ) {
			Map<String, Object> sessionMap = JsonUtil.json2Map( sessionObj );
			if ( sessionMap.containsKey( url ) ) {
				Map<String, Object> preDataMap = ( Map<String, Object> ) sessionMap.get( url );
				if ( compareParams( nowDataMap, preDataMap ) && compareTime( nowDataMap, preDataMap ) ) {
					return true;
				}
			}
		}
		Map<String, Object> cacheMap = new HashMap<>();
		cacheMap.put( url, nowDataMap );
		redisUtil.strSet( cache_repeat_key, JsonUtil.object2Json( cacheMap ), Duration.ofSeconds( intervalTime ) );
		return false;
	}

	/**
	 * 判断参数是否相同
	 */
	private boolean compareParams( Map<String, Object> nowMap, Map<String, Object> preMap ) {
		String nowParams = ( String ) nowMap.get( REPEAT_PARAMS );
		String preParams = ( String ) preMap.get( REPEAT_PARAMS );
		return nowParams.equals( preParams );
	}

	/**
	 * 判断两次间隔时间
	 */
	private boolean compareTime( Map<String, Object> nowMap, Map<String, Object> preMap ) {
		long time1 = ( Long ) nowMap.get( REPEAT_TIME );
		long time2 = ( Long ) preMap.get( REPEAT_TIME );
		return ( time1 - time2 ) < ( this.intervalTime * 1000L );
	}
}
