package tv.game88.wallet.api.mapper;

import org.apache.ibatis.annotations.Param;
import tv.game88.wallet.api.entity.WalletMerchant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.math.BigDecimal;

/**
 * @author meng.jun
 * @description 针对表【wallet_merchant(钱包商户表)】的数据库操作Mapper
 * @createDate 2023-08-21 17:25:19
 * @Entity tv.game88.wallet.api.entity.WalletMerchant
 */
public interface WalletMerchantMapper extends BaseMapper<WalletMerchant> {

    BigDecimal getMerchantMoney( @Param( "merchantId" ) Long merchantId );

    int reduceMoney( @Param( "merchantId" ) Long merchantId, @Param( "money" ) BigDecimal reduceMoney );

    int addMoney( @Param( "merchantId" ) Long merchantId, @Param( "money" ) BigDecimal addMoney );
}




