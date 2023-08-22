package tv.game88.wallet.api.mapper;

import org.apache.ibatis.annotations.Param;
import tv.game88.wallet.api.entity.WalletUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.wallet.api.vo.PlatformUser;

import java.math.BigDecimal;

/**
 * @author meng.jun
 * @description 针对表【wallet_user(钱包用户表)】的数据库操作Mapper
 * @createDate 2023-08-21 17:32:24
 * @Entity tv.game88.wallet.api.entity.WalletUser
 */
public interface WalletUserMapper extends BaseMapper<WalletUser> {

    BigDecimal getUserMoney( @Param( "userId" ) String userId );

    int addMoney( @Param( "userId" ) String userId, @Param( "money" ) BigDecimal addMoney );

    int reduceMoney( @Param( "userId" ) String userId, @Param( "money" ) BigDecimal reduceMoney );

    int addChargeMoney( @Param( "userId" ) String userId, @Param( "money" ) BigDecimal addMoney );

    int reduceSaleMoney( @Param( "userId" ) String userId, @Param( "money" ) BigDecimal reduceMoney );

    PlatformUser selectPlatformUserByUserId( @Param( "userId" ) String userId );
}




