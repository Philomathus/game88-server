package tv.game88.wallet.api.mapper;

import org.apache.ibatis.annotations.Param;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author meng.jun
* @description 针对表【wallet_user_pay_method】的数据库操作Mapper
* @createDate 2023-08-21 17:33:52
* @Entity tv.game88.wallet.api.entity.WalletUserPayMethod
*/
public interface WalletUserPayMethodMapper extends BaseMapper<WalletUserPayMethod> {

    List<WalletUserPayMethod> selectWalletUserPayMethod(WalletUserPayMethod walletUserPayMethod );

    List<WalletUserPayMethod> selectMemberCard( @Param( "userId" ) String userId );

    WalletUserPayMethod getWalletUserPayMethod( @Param( "bankAccount" ) String bankAccount );
}




