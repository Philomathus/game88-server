package tv.game88.core.config.cache;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.config.entity.ConfigSms;
import tv.game88.core.config.mapper.ConfigSmsMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author mengJun
 */
@Log4j2
@Component
public class ConfigSmsCacheUtil {
    private static final String SERVER_SMS_LIST = Constants.CONFIG_PREX + "sms:set";
    private static final String SERVER_SMS_HASH = Constants.CONFIG_PREX + "sms:";

    @Resource
    private RedisUtils      redisUtil;
    @Resource
    private ConfigSmsMapper configSmsMapper;

    public long countCache() {
        return redisUtil.lSize( SERVER_SMS_LIST );
    }

    public void setConfigSmsCache( ConfigSms configSms ) {
        String smsId = configSms.getId().toString();
        Long   size  = redisUtil.lSize( SERVER_SMS_LIST );
        if ( size > 0 ) {
            List<String> keys = redisUtil.lRange( SERVER_SMS_LIST, 0, size - 1 );
            if ( keys.contains( smsId ) ) {
                redisUtil.strSet( SERVER_SMS_HASH + smsId, JsonUtil.object2Json( configSms ) );
                return;
            }
        }
        redisUtil.lRightPush( SERVER_SMS_LIST, smsId );
        redisUtil.strSet( SERVER_SMS_HASH + smsId, JsonUtil.object2Json( configSms ) );
    }

    public ConfigSms getConfigSmsCache( long index ) {
        this.existsCache();
        String smsId = redisUtil.lIndex( SERVER_SMS_LIST, index );
        if ( StringUtils.isBlank( smsId ) ) {
            if ( index == 0 ) {
                return null;
            } else {
                return this.getConfigSmsCache( 0 );
            }
        }
        String s = redisUtil.strGet( SERVER_SMS_HASH + smsId );
        return StringUtils.isBlank( s ) ? null : JsonUtil.json2Object( s, ConfigSms.class );
    }

    private void existsCache() {
        if ( redisUtil.lSize( SERVER_SMS_LIST ) == 0 ) {
            List<ConfigSms> configSmsList = configSmsMapper.selectConfigSmsByEffect();
            if ( configSmsList.isEmpty() ) {
                return;
            }
            for ( ConfigSms configSms : configSmsList ) {
                redisUtil.lRightPush( SERVER_SMS_LIST, configSms.getId().toString() );
                redisUtil.strSet( SERVER_SMS_HASH + configSms.getId(), JsonUtil.object2Json( configSms ) );
            }
        }
    }

    public void clearCache( long smsId ) {
        redisUtil.lDelete( SERVER_SMS_LIST, 0, smsId + "" );
        redisUtil.unlink( SERVER_SMS_HASH + smsId );
    }
}
