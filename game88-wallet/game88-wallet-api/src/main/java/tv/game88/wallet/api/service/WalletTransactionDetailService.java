package tv.game88.wallet.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.ReqBuyCoins;
import tv.game88.wallet.api.dto.ReqBuyerConfirmTransfer;
import tv.game88.wallet.api.dto.RspBuyOrderDetail;
import tv.game88.wallet.api.entity.WalletTransactionDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author meng.jun
 * @description 针对表【wallet_transaction_detail(钱包交易明细表)】的数据库操作Service
 * @createDate 2023-08-21 17:31:44
 */
public interface WalletTransactionDetailService extends IService<WalletTransactionDetail> {

    RspBase<?> buyOrder( String userId, ReqBuyCoins reqBuyCoins );

    void saveTransDetailOrReduceTransAmount( WalletTransactionDetail walletTransactionDetail );

    RspBase<RspBuyOrderDetail> buyOrderDetail( String transDetailId );

    RspBase<?> sellerConfirmTrans( String userId, String transDetailId );

    RspBase<?> sellerCancelTrans( String userId, String transDetailId );

    void updateTransDetailOrAddTransAmount( WalletTransactionDetail update, WalletTransactionDetail walletTransactionDetail );

    RspBase<?> buyerConfirmTransfer( String userId, ReqBuyerConfirmTransfer reqBuyerConfirmTransfer );

    RspBase<?> buyerCancelTrans( String userId, String transDetailId );

    RspBase<?> sellerConfirmTransfer( String userId, String transDetailId );

    RspBase<?> sellerNotReceived( String userId, String transDetailId );

    void updateTransDetailOrAddUserAmount( WalletTransactionDetail update, WalletTransactionDetail walletTransactionDetail );
}
