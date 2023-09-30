package tv.game88.wallet.api.cache;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.wallet.api.entity.WalletMerchant;
import tv.game88.wallet.api.mapper.WalletMerchantMapper;

import javax.annotation.Resource;
import java.util.Map;

@Component
@Log4j2
public class WalletMerchantCacheUtil {
    @Resource
    private RedisUtils           redisUtils;
    @Resource
    private WalletMerchantMapper walletMerchantMapper;

    public static final String WALLET_MERCHANT_KEY = "wallet:merchant:";

    public WalletMerchant getWalletMerchantCache( Long walletMerchantId ) {
        String key = WALLET_MERCHANT_KEY + walletMerchantId;
        if ( !redisUtils.exists( key ) ) {
            this.refreshWalletMerchantCache( walletMerchantId );
        }
        Map<Object, Object> map = redisUtils.hGetAll( key );
        if ( CollectionUtils.isEmpty( map ) ) {
            return null;
        }
        return JsonUtil.map2Object( map, WalletMerchant.class );
    }

    public String getWalletMerchantCache( Long walletMerchantId, String field ) {
        String key = WALLET_MERCHANT_KEY + walletMerchantId;
        if ( !redisUtils.exists( key ) ) {
            this.refreshWalletMerchantCache( walletMerchantId );
        }
        Object o = redisUtils.hGet( key, field );
        if ( o == null ) {
            return null;
        }
        return o.toString();
    }

    private void refreshWalletMerchantCache( Long walletMerchantId ) {
        WalletMerchant walletMerchant = walletMerchantMapper.selectById( walletMerchantId );
        if ( walletMerchant == null ) {
            return;
        }

        Map<String, String> map = JsonUtil.object2MapStr( walletMerchant );
        log.warn( JsonUtil.object2Json( map ) );
        redisUtils.hMSet( WALLET_MERCHANT_KEY + walletMerchantId, map );
    }

    public void clearWalletMerchantCache( Long walletMerchantId ) {
        redisUtils.unlink( WALLET_MERCHANT_KEY + walletMerchantId );
    }
}
