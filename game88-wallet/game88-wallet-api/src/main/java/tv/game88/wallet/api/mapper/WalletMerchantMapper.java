package tv.game88.wallet.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.wallet.api.entity.WalletMerchant;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_merchant(钱包商户表)】的数据库操作Mapper
 * @createDate 2023-08-21 17:25:19
 * @Entity tv.game88.wallet.api.entity.WalletMerchant
 */
public interface WalletMerchantMapper extends BaseMapper<WalletMerchant> {
    /**
     * 查询钱包商户列表
     *
     * @param walletMerchant 钱包商户
     *
     * @return 钱包商户集合
     */
    public List<WalletMerchant> selectWalletMerchantList( WalletMerchant walletMerchant );

    BigDecimal getMerchantMoney( @Param( "merchantId" ) Long merchantId );

    int reduceMoney( @Param( "merchantId" ) Long merchantId, @Param( "money" ) Long reduceMoney );

    int addMoney( @Param( "merchantId" ) Long merchantId, @Param( "money" ) Long addMoney );
}
