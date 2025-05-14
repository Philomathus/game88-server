package tv.game88.core.config.cache;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;

import jakarta.annotation.Resource;
import java.time.Duration;

/**
 * @author mengJun
 */
@Log4j2
@Component
public class SmsPhoneCacheUtil {
    public static final String SMS_PHONE        = Constants.CONFIG_PREX + "smsPhone:";
    public static final String SMS_PHONE_INDEX  = Constants.CONFIG_PREX + "smsPhoneIndex:";
    public static final String SMS_PHONE_EXPIRE = Constants.CONFIG_PREX + "smsPhoneExpire:";
    public static final String SMS_PHONE_NUMBER = Constants.CONFIG_PREX + "smsPhoneNumber:";

    @Resource
    private RedisUtils redisUtil;

    public String getPhoneCode( String phone ) {// 获取缓存中的手机号
        if ( phone != null && phone.startsWith( "0" ) ) {
            phone = phone.replaceFirst( "0", "" );
        }
        return redisUtil.strGet( SMS_PHONE + phone );
    }

    public String getPhoneIndex( String phone ) {// 获取缓存中的短信运行商下表
        if ( phone != null && phone.startsWith( "0" ) ) {
            phone = phone.replaceFirst( "0", "" );
        }
        return redisUtil.strGet( SMS_PHONE_INDEX + phone );
    }

    public void setSmsPhoneCache( String phone, String code, String index ) {
        if ( phone != null && phone.startsWith( "0" ) ) {
            phone = phone.replaceFirst( "0", "" );
        }
        Duration timeout = Duration.ofMinutes( 30 );
        redisUtil.strSet( SMS_PHONE + phone, code, timeout );
        redisUtil.strSet( SMS_PHONE_INDEX + phone, index, timeout );
        redisUtil.strSet( SMS_PHONE_EXPIRE + phone, index, Duration.ofMinutes( 1 ) );
        redisUtil.strSet( SMS_PHONE_NUMBER + phone, "0", timeout );
    }

    public String getSmsPhoneExpire( String phone ) {
        if ( phone != null && phone.startsWith( "0" ) ) {
            phone = phone.replaceFirst( "0", "" );
        }
        return redisUtil.strGet( SMS_PHONE_EXPIRE + phone );//一分钟
    }

    public Long setSmsNumber( String phone ) {
        if ( phone != null && phone.startsWith( "0" ) ) {
            phone = phone.replaceFirst( "0", "" );
        }
        return redisUtil.strIncrement( SMS_PHONE_NUMBER + phone, 1 );
    }

    public void unLink( String phone ) {
        if ( phone != null && phone.startsWith( "0" ) ) {
            phone = phone.replaceFirst( "0", "" );
        }
        redisUtil.unlink( SMS_PHONE + phone );
        redisUtil.unlink( SMS_PHONE_INDEX + phone );
        redisUtil.unlink( SMS_PHONE_EXPIRE + phone );
        redisUtil.unlink( SMS_PHONE_NUMBER + phone );
    }
}
