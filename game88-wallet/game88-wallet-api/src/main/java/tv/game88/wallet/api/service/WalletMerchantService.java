package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.wallet.api.entity.WalletMerchant;

import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_merchant(钱包商户表)】的数据库操作Service
 * @createDate 2023-08-21 17:25:19
 */
public interface WalletMerchantService extends IService<WalletMerchant> {
    /**
     * 查询钱包商户列表
     *
     * @param walletMerchant 钱包商户
     *
     * @return 钱包商户集合
     */
    public List<WalletMerchant> selectWalletMerchantList( WalletMerchant walletMerchant );
}
