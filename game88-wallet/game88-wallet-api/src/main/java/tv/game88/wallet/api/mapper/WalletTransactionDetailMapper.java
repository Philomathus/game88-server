package tv.game88.wallet.api.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tv.game88.wallet.api.entity.WalletTransactionDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author meng.jun
 * @description 针对表【wallet_transaction_detail(钱包交易明细表)】的数据库操作Mapper
 * @createDate 2023-08-21 17:31:44
 * @Entity tv.game88.wallet.api.entity.WalletTransactionDetail
 */
public interface WalletTransactionDetailMapper extends BaseMapper<WalletTransactionDetail> {
    @Select( "select count(1) from wallet_transaction_detail where seller_id = #{sellerId} and buyer_confirm_buy_time >= "
            + "#{startTime} and buyer_confirm_buy_time <= #{endTime} and status <> 'BUYER_CANCEL';" )
    Long countSellerTotal( @Param( "sellerId" ) String sellerId, @Param( "startTime" ) LocalDateTime startTime, @Param(
            "endTime" ) LocalDateTime endTime );

    @Select( "select sum(received_time_sec) s, count(1) c from wallet_transaction_detail where `status` in"
            + " ('SELLER_CONFIRM_TRANSFER','SYSTEM_CONFIRM_TRANSFER') and seller_id = #{sellerId} and buyer_confirm_buy_time >= "
            + "#{startTime} and buyer_confirm_buy_time <= #{endTime}" )
    Map<String, Object> sumSellerReceived( @Param( "sellerId" ) String sellerId, @Param( "startTime" ) LocalDateTime startTime,
                                           @Param( "endTime" ) LocalDateTime endTime );

    @Select( "select sum(transfer_time_sec) s, count(1) c from wallet_transaction_detail where `status` in"
            + " ('SELLER_CONFIRM_TRANSFER','SYSTEM_CONFIRM_TRANSFER') and buyer_id = #{buyerId} and buyer_confirm_buy_time >= "
            + "#{startTime} and buyer_confirm_buy_time <= #{endTime}" )
    Map<String, Object> sumBuyerTransfer( @Param( "buyerId" ) String buyerId, @Param( "startTime" ) LocalDateTime startTime,
                                          @Param( "endTime" ) LocalDateTime endTime );
}




