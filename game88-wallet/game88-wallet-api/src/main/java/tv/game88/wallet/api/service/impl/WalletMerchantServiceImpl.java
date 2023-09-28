package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.wallet.api.entity.WalletMerchant;
import tv.game88.wallet.api.mapper.WalletMerchantMapper;
import tv.game88.wallet.api.service.WalletMerchantService;

import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_merchant(钱包商户表)】的数据库操作Service实现
 * @createDate 2023-08-21 17:25:19
 */
@Service
public class WalletMerchantServiceImpl extends ServiceImpl<WalletMerchantMapper, WalletMerchant> implements WalletMerchantService {
    /**
     * 查询钱包商户列表
     *
     * @param walletMerchant 钱包商户
     *
     * @return 钱包商户
     */
    @Override
    public List<WalletMerchant> selectWalletMerchantList( WalletMerchant walletMerchant ) {
        return this.baseMapper.selectWalletMerchantList( walletMerchant );
    }
}




