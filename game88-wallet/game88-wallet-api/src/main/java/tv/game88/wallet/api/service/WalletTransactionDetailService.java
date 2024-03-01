package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletTransactionDetail;

import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_transaction_detail(钱包交易明细表)】的数据库操作Service
 * @createDate 2023-08-21 17:31:44
 */
public interface WalletTransactionDetailService extends IService<WalletTransactionDetail> {

    List<RspTransDetail> transDetailList( String userId, ReqTransDetailList req );

    RspBase<?> buyOrder( String userId, ReqBuyCoins reqBuyCoins );

    RspBase<RspBuyOrderDetail> buyOrderDetail( String userId, String transDetailId );

    RspBase<?> sellerConfirmTrans( String userId, String transDetailId );

    RspBase<?> sellerCancelTrans( String userId, String transDetailId );

    RspBase<?> buyerConfirmTransfer( String userId, ReqBuyerConfirmTransfer reqBuyerConfirmTransfer );

    RspBase<?> buyerCancelTrans( String userId, String transDetailId );

    RspBase<?> sellerConfirmTransfer( String userId, String transDetailId );

    RspBase<?> sellerNotReceived( String userId, String transDetailId );

    void processBuyerConfirmBuyTimeout( String transDetailId );

    void processSellerConfirmTransTimeout( String transDetailId );

    void processBuyerConfirmTransferTimeout( String transDetailId );
}
