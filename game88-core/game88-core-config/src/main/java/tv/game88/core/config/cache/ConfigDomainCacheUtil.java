package tv.game88.core.config.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.entity.ConfigOss;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class ConfigDomainCacheUtil {

    public static ConfigDomainCacheUtil me;

    private static final String                DOMAIN_OSS_CACHE_KEY = "domain.oss";
    // 最大容量 maximumSize
    // 缓存过期时长 expireAfterWrite
    // 设置并发级别为cpu核心数 concurrencyLevel
    private static final Cache<String, String> DOMAIN_CACHE         = CacheBuilder
            .newBuilder()
            .maximumSize( 1 )
            .expireAfterWrite( 2, TimeUnit.SECONDS )
            .concurrencyLevel( Runtime.getRuntime().availableProcessors() * 2 )
            .build();

    @Resource
    private ConfigOssCacheUtil configOssCacheUtil;
    @Resource
    private ConfigEnvCacheUtil configEnvCacheUtil;

    @PostConstruct
    void init() {
        me = this;
    }

    public String getDomainOssValue() {
        String cacheInfo = DOMAIN_CACHE.getIfPresent( DOMAIN_OSS_CACHE_KEY );
        if ( StringUtils.isBlank( cacheInfo ) ) {
            this.refreshDomainOssCache();
            return DOMAIN_CACHE.getIfPresent( DOMAIN_OSS_CACHE_KEY );
        }
        return cacheInfo;
    }

    private void refreshDomainOssCache() {
        String value = configEnvCacheUtil.getConf( DOMAIN_OSS_CACHE_KEY );
        if ( StringUtils.isBlank( value ) ) {
            ConfigOss configOss = configOssCacheUtil.getEffect();
            if ( configOss == null ) {
                return;
            }
            configEnvCacheUtil.setConfCache( DOMAIN_OSS_CACHE_KEY, configOss.getDoMain() );
            DOMAIN_CACHE.put( DOMAIN_OSS_CACHE_KEY, configOss.getDoMain() );
        } else {
            DOMAIN_CACHE.put( DOMAIN_OSS_CACHE_KEY, value );
        }
    }

    public void clearDomainOss() {
        configEnvCacheUtil.deleteCache( DOMAIN_OSS_CACHE_KEY );
    }
}
