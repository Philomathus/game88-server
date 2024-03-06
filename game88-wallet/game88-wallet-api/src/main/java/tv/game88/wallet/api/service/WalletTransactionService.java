package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletTransaction;

import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_transaction(钱包交易表)】的数据库操作Service
 * @createDate 2023-08-21 17:27:31
 */
public interface WalletTransactionService extends IService<WalletTransaction> {
    RspBase<String> sellOrder( String userId, ReqSellCoins reqSellCoins );

    void saveTransAndReduceUserAmount( String userId, WalletTransaction walletTransaction, Long sellNum );

    void updateTransAndAddUserAmount( String userId, WalletTransaction update, Long amount );

    RspBase<RspSellOrderDetail> sellOrderDetail( String userId, String transactionId );

    RspBase<?> cancelSellOrder( String userId, String transactionId );

    List<RspSellOrderDetail> sellOrderList( String userId, ReqSellOrderList reqSellOrderList );

    List<RspTransCenterDetail> transSellOrderList( String userId, ReqTransCenterDetail reqTransCenterDetail );

    RspBase<RspSellOrderDetail2> toBuySellOrderDetail( String userId, String transactionId );

    RspBase<?> validTransaction( WalletTransaction walletTransaction );

    List<WalletTransaction> getWalletTransactionList( WalletTransaction walletTransaction );
}
