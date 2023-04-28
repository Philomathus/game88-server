package tv.game88.platform.api.cache;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.config.enums.PlatformUserKey;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * @author jake
 * Copied from 77tv
 */
@Component
public class MemberForbidUtil {
	public static final String CACHE_USER_FORBID         = Constants.LIVE_PREX + "user:forbid:";
	public static final String CACHE_USER_SPEAK_INTERVAL = Constants.LIVE_PREX + "user:speakInterval:";

	@Resource
	private RedisUtils redisUtil;

	public void setUserForbid( String pUserId, Integer videoId, Duration forbidTime ) {
		redisUtil.strSet( CACHE_USER_FORBID + pUserId, videoId.toString(), forbidTime );
	}

	public long getUserForbidExpire( String pUserId ) {
		long expire = redisUtil.getExpire( CACHE_USER_FORBID + pUserId );
		return expire > 0 ? expire : 0;
	}

	public void setUserSpeakInterval( String pUserId, Integer videoId, Duration forbidTime ) {
		redisUtil.strSet( CACHE_USER_SPEAK_INTERVAL + pUserId, videoId.toString(), forbidTime );
	}

	public long getUserSpeakIntervalExpire( String pUserId ) {
		long expire = redisUtil.getExpire( CACHE_USER_SPEAK_INTERVAL + pUserId );
		return expire > 0 ? expire : 0;
	}

	public boolean setPlatformUserSpeak( String pUserId, boolean speak ) {
		String token = redisUtil.strGet( Constants.USER_TOKEN_KEY + pUserId );
		if ( Strings.isBlank( token ) ) {
			return false;
		}
		redisUtil.hSet( Constants.TOKEN_USER_KEY + token, PlatformUserKey.SPEAK.getKey(), speak + "" );
		return true;
	}
	public int setPlatformUserStatus( String pUserId, int status ) {
		String token = redisUtil.strGet( Constants.USER_TOKEN_KEY + pUserId );
		if ( Strings.isBlank( token ) ) {
			return status;
		}
		redisUtil.hSet( Constants.TOKEN_USER_KEY + token, PlatformUserKey.STATUS.getKey(), status + "" );
		return status;
	}

//	set member address by ip
public int setStatusZero( String loginIp, int status ) {
	String token = redisUtil.strGet( Constants.USER_TOKEN_KEY + loginIp );
	if ( Strings.isBlank( token ) ) {
		return status;
	}
	redisUtil.hSet( Constants.TOKEN_USER_KEY + token, PlatformUserKey.STATUS.getKey(), status + "" );
	return status;
}
}
