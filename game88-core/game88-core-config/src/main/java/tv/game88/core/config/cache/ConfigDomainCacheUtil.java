package tv.game88.core.config.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.config.entity.ConfigOss;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class ConfigDomainCacheUtil {

    public static ConfigDomainCacheUtil me;

    private static final String                DOMAIN_OSS_CACHE_KEY = "domain.oss";
    private static final String                CONFIG_DOMAIN_OSS    = Constants.CONFIG_PREX + DOMAIN_OSS_CACHE_KEY;
    // 最大容量 maximumSize
    // 缓存过期时长 expireAfterWrite
    // 设置并发级别为cpu核心数 concurrencyLevel
    private static final Cache<String, String> DOMAIN_OSS_CACHE     = CacheBuilder.newBuilder().maximumSize( 1 )
                                                                                  .expireAfterWrite( 2, TimeUnit.SECONDS )
                                                                                  .concurrencyLevel( getAvailableProcessors() )
                                                                                  .build();

    @Resource
    private ConfigOssCacheUtil configOssCacheUtil;
    @Resource
    private RedisUtils      redisUtil;

    @PostConstruct
    void init() {
        me = this;
    }

    private static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors() * 2;
    }

    public String getValue( String code ) {
        String cacheInfo = DOMAIN_OSS_CACHE.getIfPresent( code );
        if (StringUtils.isBlank( cacheInfo ) && DOMAIN_OSS_CACHE_KEY.equals( code )) {
            this.refreshDomainOssCache();
            return DOMAIN_OSS_CACHE.getIfPresent( code );
        }
        return cacheInfo;
    }

    private void refreshDomainOssCache() {
        if (!redisUtil.exists( CONFIG_DOMAIN_OSS )) {
            ConfigOss configOss = configOssCacheUtil.getEffect();
            if (configOss == null) {
                return;
            }
            redisUtil.strSet( CONFIG_DOMAIN_OSS, configOss.getDoMain() );
            DOMAIN_OSS_CACHE.put( DOMAIN_OSS_CACHE_KEY, configOss.getDoMain() );
        } else {
            String json = redisUtil.strGet( CONFIG_DOMAIN_OSS );
            DOMAIN_OSS_CACHE.put( DOMAIN_OSS_CACHE_KEY, Objects.requireNonNull( json ) );
        }
    }

    public void clear() {
        redisUtil.unlink( CONFIG_DOMAIN_OSS );
    }

    /**
     * domain.oss = ConfigOss.domain
     */
    public String dynamicValue( String value ) {
        String trim = value.trim();
        if (trim.contains( "${" ) && trim.contains( "}" )) {
            String param  = trim.substring( trim.indexOf( "${" ) + 2, trim.indexOf( "}" ) );
            String domain = this.getValue( param );
            if (StringUtils.isNotBlank( domain )) {
                return trim.replace( "${" + param + "}", domain );
            }
        }
        return trim;
    }
}
