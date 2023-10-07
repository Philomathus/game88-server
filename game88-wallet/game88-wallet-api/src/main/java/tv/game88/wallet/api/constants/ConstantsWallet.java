package tv.game88.wallet.api.constants;

import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.constants.Constants;

/**
 * wallet常量
 *
 * @author mengJun
 */
public abstract class ConstantsWallet {
    public static final String REDIS_DEFAULT_VALUE = "1";

    public static final String DEFAULT_HEAD_IMAGE_URL = ConfigDomainCacheUtil.me.getDomainOssValue() + "/avatar/user-img.jpeg";

    public static final String BUYER_CONFIRM_BUY_ORDER      = Constants.WALLET_PREX + "timeout:buyerConfirmBuyOrder:";
    public static final String SELLER_CONFIRM_TRANS_ORDER   = Constants.WALLET_PREX + "timeout:sellerConfirmTransOrder:";
    public static final String BUYER_CONFIRM_TRANSFER_ORDER = Constants.WALLET_PREX + "timeout:buyerConfirmTransferOrder:";

    public static final String MESSAGE_SYSTEM_IS_READ  = Constants.WALLET_PREX + "message:systemRead:";
    public static final String MESSAGE_PERSONAL_PROMPT = Constants.WALLET_PREX + "message:personalPrompt:";

}
