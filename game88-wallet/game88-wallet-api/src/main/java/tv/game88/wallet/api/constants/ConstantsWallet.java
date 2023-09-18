package tv.game88.wallet.api.constants;

import tv.game88.core.config.cache.ConfigDomainCacheUtil;

/**
 * wallet常量
 *
 * @author mengJun
 */
public abstract class ConstantsWallet {
    public static final String DEFAULT_HEAD_IMAGE_URL = ConfigDomainCacheUtil.me.getDomainOssValue() + "/avatar/user-img.jpeg";
}
