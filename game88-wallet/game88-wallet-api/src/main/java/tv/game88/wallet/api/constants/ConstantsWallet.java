package tv.game88.wallet.api.constants;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.constants.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public static final String MESSAGE_CHANNEL                   = Constants.WALLET_PREX + "messageChannel:";
    public static final String MESSAGE_SSEEMITTER_REMOVE_CHANNEL = Constants.WALLET_PREX + "messageSseEmitterRemoveChannel:";

    public static Map<String, SseEmitter> MEMBER_SSEEMITTER_MAP = new ConcurrentHashMap<>();
}
