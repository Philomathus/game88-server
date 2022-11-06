package tv.game88.core.config.cache;

import lombok.extern.log4j.Log4j2;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.config.entity.ConfigEnvironment;
import tv.game88.core.config.mapper.ConfigEnvironmentMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author MengJun
 */
@Log4j2
@Component
public class ConfigEnvCacheUtil {
    private static final String SYS_CONFIG_KEY = Constants.CONFIG_PREX + "env";

    @Resource
    private RedisUtils              redisUtil;
    @Resource
    private ConfigEnvironmentMapper configEnvironmentMapper;

    public String dynamicValue( String value ) {
        if ( value.contains( "${" ) && value.contains( "}" ) ) {
            String param  = value.substring( value.indexOf( "${" ) + 2, value.indexOf( "}" ) );
            String paramValue = this.getConf( param );
            if ( StringUtils.isNotBlank( paramValue ) ) {
                return value.replace( "${" + param + "}", paramValue );
            }
        }
        return value;
    }

    public List<String> getConf( List<Object> codes ) {
        Boolean exists = redisUtil.exists( SYS_CONFIG_KEY );
        if (exists == null || !exists) {
            this.refreshConfCache();
        }
        List<Object> objects    = redisUtil.hMGet( SYS_CONFIG_KEY, codes );
        List<String> resultList = new ArrayList<>( objects.size() );
        for ( Object object : objects ) {
            String value = object != null ? this.dynamicValue( object.toString() ) : null;
            resultList.add( value );
        }
        return resultList;
    }

    public String getConf( String code, String defaultValue ) {
        Boolean exists = redisUtil.exists( SYS_CONFIG_KEY );
        if (exists == null || !exists) {
            this.refreshConfCache();
        }
        Object value = redisUtil.hGet( SYS_CONFIG_KEY, code );
        return value != null ? this.dynamicValue( value.toString() ) : defaultValue;
    }

    public String getConf( String code ) {
        return getConf( code, "" );
    }

    public BigDecimal getConfBd( String code ) {
        try {
            return new BigDecimal( getConf( code, "0" ) );
        } catch ( NumberFormatException e ) {
            return BigDecimal.ZERO;
        }
    }

    public int getConfInt( String code ) {
        try {
            return Integer.parseInt( getConf( code, "0" ) );
        } catch ( NumberFormatException e ) {
            return 0;
        }
    }

    public int getConfInt( String code, int defaultValue ) {
        try {
            return Integer.parseInt( getConf( code, defaultValue + "" ) );
        } catch ( NumberFormatException e ) {
            return 0;
        }
    }

    public boolean getConfBool( String code ) {
        return getConfInt( code ) > 0;
    }


    public void refreshConfCache() {
        ConfigEnvironment query = new ConfigEnvironment();
        query.setEnvStatus( 1 );
        List<ConfigEnvironment> configEnvironments = configEnvironmentMapper.selectConfigEnvironmentList( query );

        Map<String, String> map = configEnvironments.stream().collect( Collectors.toMap( ConfigEnvironment::getEnvCode, ( env ) ->
                env.getEnvValue() == null ? "" : env.getEnvValue() ) );
        redisUtil.unlink( SYS_CONFIG_KEY );
        redisUtil.hMSet( SYS_CONFIG_KEY, map );
    }

    public void setConfCache( ConfigEnvironment configEnvironment ) {
        redisUtil.hSet( SYS_CONFIG_KEY, configEnvironment.getEnvCode(), configEnvironment.getEnvValue() );
    }

    public void setConfCache( String code, String value ) {
        redisUtil.hSet( SYS_CONFIG_KEY, code, value );
    }

    public void deleteCache( String... envCodes ) {
        redisUtil.hRemove( SYS_CONFIG_KEY, envCodes );
    }
}
