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
import tv.game88.wallet.api.dto.ReqSellCoins;
import tv.game88.wallet.api.dto.ReqSellOrderDetail;
import tv.game88.wallet.api.dto.RspSellOrderDetail;
import tv.game88.wallet.api.service.WalletTransactionService;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import javax.annotation.Resource;

@RestController
@Tag( name = "挂单接口" )
@Log4j2
public class TransactionController extends BaseController {
    @Resource
    private WalletTransactionService walletTransactionService;

    @Operation( summary = "出售G币 - 发布挂单" )
    @PostMapping( "/api/sellOrder" )
    public RspBase<?> sellOrder( @RequestBody @Validated ReqSellCoins reqSellCoins ) {
        return walletTransactionService.sellOrder( MemberSecurityUtils.getUserId(), reqSellCoins );
    }

    @Operation( summary = "我的挂单 - 挂单详情" )
    @PostMapping( "/api/sellOrderDetail" )
    public RspBase<RspSellOrderDetail> sellOrderDetail( @RequestBody @Validated ReqSellOrderDetail reqSellOrderDetail ) {
        return walletTransactionService.sellOrderDetail( MemberSecurityUtils.getUserId(), reqSellOrderDetail.getTransactionId() );
    }

    @Operation( summary = "取消挂单" )
    @PostMapping( "/api/cancelSellOrder" )
    public RspBase<?> cancelSellOrder( @RequestBody @Validated ReqSellOrderDetail reqSellOrderDetail ) {
        return walletTransactionService.cancelSellOrder( MemberSecurityUtils.getUserId(), reqSellOrderDetail.getTransactionId() );
    }
}
