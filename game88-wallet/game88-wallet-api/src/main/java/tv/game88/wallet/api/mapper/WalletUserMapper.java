package tv.game88.wallet.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.vo.PlatformUser;

import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_user(钱包用户表)】的数据库操作Mapper
 * @createDate 2023-08-21 17:32:24
 * @Entity tv.game88.wallet.api.entity.WalletUser
 */
public interface WalletUserMapper extends BaseMapper<WalletUser> {

    /**
     * 查询钱包用户列表
     *
     * @param walletUser 钱包用户
     *
     * @return 钱包用户集合
     */
    List<WalletUser> selectWalletUserList( WalletUser walletUser );

    Long getUserMoney( @Param( "userId" ) String userId );

    int addMoney( @Param( "userId" ) String userId, @Param( "money" ) Long addMoney );
    int addCancelOrderMoney( @Param( "userId" ) String userId, @Param( "money" ) Long addMoney );

    int addChargeMoney( @Param( "userId" ) String userId, @Param( "money" ) Long addMoney );

    int reduceMoney( @Param( "userId" ) String userId, @Param( "money" ) Long reduceMoney );

    int reduceSaleMoney( @Param( "userId" ) String userId, @Param( "money" ) Long reduceMoney );

    int reduceFrozenAndAmount( @Param( "userId" ) String userId, @Param( "needAmount" ) Long needAmount,
                               @Param( "frozenAmount" ) Long frozenAmount );

    PlatformUser selectPlatformUserByUserId( @Param( "userId" ) String userId );

    WalletUser findMemberHistoryByMobile( @Param( "mobile" ) String mobile );

    int deleteByHistoryKey( @Param( "userId" ) String userId );

    WalletUser findMemberHistoryById( @Param( "id" ) String walletAddress );

    String getUserPasswd( String userId );

    void addBuyerTransactionSuccess( @Param( "id" ) String id, @Param( "money" ) Long money );

    void addSellerTransactionSuccess( @Param( "id" ) String id, @Param( "money" ) Long money );

    void addSellerTotalSellingAmount( @Param( "id" ) String id, @Param( "money" ) Long money );

    void addSellerOngoingSellingAmount( @Param( "id" ) String id, @Param( "money" ) Long money );

    void addSellerCancelSellingAmount( @Param( "id" ) String id, @Param( "money" ) Long money );

    void addSellerInitCancelSell( @Param( "id" ) String id, @Param( "money" ) Long money );
}




