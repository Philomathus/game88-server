package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.ReqBuyCoins;
import tv.game88.wallet.api.dto.ReqBuyerConfirmTransfer;
import tv.game88.wallet.api.dto.ReqTransactionDetail;
import tv.game88.wallet.api.dto.RspBuyOrderDetail;
import tv.game88.wallet.api.service.WalletTransactionDetailService;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import javax.annotation.Resource;

@RestController
@Tag( name = "交易接口" )
@Log4j2
public class TransactionDetailController extends BaseController {
    @Resource
    private WalletTransactionDetailService walletTransactionDetailService;

    @Operation( summary = "购买G币" )
    @PostMapping( "/api/buyOrder" )
    public RspBase<?> buyOrder( @RequestBody @Validated ReqBuyCoins reqBuyCoins ) {
        return walletTransactionDetailService.buyOrder( MemberSecurityUtils.getUserId(), reqBuyCoins );
    }

    @Operation( summary = "我的买单 - 交易详情" )
    @PostMapping( "/api/buyOrderDetail" )
    public RspBase<RspBuyOrderDetail> buyOrderDetail( @RequestBody @Validated ReqTransactionDetail reqTransactionDetail ) {
        return walletTransactionDetailService.buyOrderDetail( reqTransactionDetail.getTransDetailId() );
    }

    @Operation( summary = "卖家确认交易" )
    @PostMapping( "/api/sellerConfirmTrans" )
    public RspBase<?> sellerConfirmTrans( @RequestBody @Validated ReqTransactionDetail reqTransactionDetail ) {
        return walletTransactionDetailService.sellerConfirmTrans( MemberSecurityUtils.getUserId(),
                reqTransactionDetail.getTransDetailId() );
    }

    @Operation( summary = "卖家取消交易" )
    @PostMapping( "/api/sellerCancelTrans" )
    public RspBase<?> sellerCancelTrans( @RequestBody @Validated ReqTransactionDetail reqTransactionDetail ) {
        return walletTransactionDetailService.sellerCancelTrans( MemberSecurityUtils.getUserId(),
                reqTransactionDetail.getTransDetailId() );
    }

    @Operation( summary = "买家确认转账" )
    @PostMapping( "/api/buyerConfirmTransfer" )
    public RspBase<?> buyerConfirmTransfer( @RequestBody @Validated ReqBuyerConfirmTransfer reqBuyerConfirmTransfer ) {
        return walletTransactionDetailService.buyerConfirmTransfer( MemberSecurityUtils.getUserId(), reqBuyerConfirmTransfer );
    }

    @Operation( summary = "买家取消交易" )
    @PostMapping( "/api/buyerCancelTrans" )
    public RspBase<?> buyerCancelTrans( @RequestBody @Validated ReqTransactionDetail reqTransactionDetail ) {
        return walletTransactionDetailService.buyerCancelTrans( MemberSecurityUtils.getUserId(),
                reqTransactionDetail.getTransDetailId() );
    }

    @Operation( summary = "卖家确认转币" )
    @PostMapping( "/api/sellerConfirmTransfer" )
    public RspBase<?> sellerConfirmTransfer( @RequestBody @Validated ReqTransactionDetail reqTransactionDetail ) {
        return walletTransactionDetailService.sellerConfirmTransfer( MemberSecurityUtils.getUserId(),
                reqTransactionDetail.getTransDetailId() );
    }

    @Operation( summary = "卖家未收到转账" )
    @PostMapping( "/api/sellerNotReceived" )
    public RspBase<?> sellerNotReceived( @RequestBody @Validated ReqTransactionDetail reqTransactionDetail ) {
        return walletTransactionDetailService.sellerNotReceived( MemberSecurityUtils.getUserId(),
                reqTransactionDetail.getTransDetailId() );
    }
}
