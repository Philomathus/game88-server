package tv.game88.core.config.cache;

import org.springframework.stereotype.Component;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.entity.ConfigOss;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class ConfigDomainCacheUtil {

    public static ConfigDomainCacheUtil me;

    private static final String DOMAIN_OSS_CACHE_KEY = "domain.oss";

    @Resource
    private ConfigOssCacheUtil configOssCacheUtil;
    @Resource
    private ConfigEnvCacheUtil configEnvCacheUtil;

    @PostConstruct
    void init() {
        me = this;
    }

    public String getDomainOssValue() {
        String cacheInfo = configEnvCacheUtil.getConf( DOMAIN_OSS_CACHE_KEY );
        if ( StringUtils.isBlank( cacheInfo ) ) {
            this.refreshDomainOssCache();
            return configEnvCacheUtil.getConf( DOMAIN_OSS_CACHE_KEY );
        }
        return cacheInfo;
    }

    private void refreshDomainOssCache() {
        ConfigOss configOss = configOssCacheUtil.getEffect();
        if ( configOss == null ) {
            return;
        }
        configEnvCacheUtil.setConfCache( DOMAIN_OSS_CACHE_KEY, configOss.getDoMain() );
    }

    public void clearDomainOss() {
        configEnvCacheUtil.deleteCache( DOMAIN_OSS_CACHE_KEY );
    }
}
