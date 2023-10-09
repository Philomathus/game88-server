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
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.service.WalletTransactionService;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "挂单接口" )
@Log4j2
public class TransactionController extends BaseController {
    @Resource
    private WalletTransactionService walletTransactionService;

    @Operation( summary = "出售G币 - 发布挂单" )
    @PostMapping( "/api/sellOrder" )
    public RspBase<String> sellOrder( @RequestBody @Validated ReqSellCoins reqSellCoins ) {
        return walletTransactionService.sellOrder( MemberSecurityUtils.getUserId(), reqSellCoins );
    }

    @Operation( summary = "我的挂单 - 挂单详情" )
    @PostMapping( "/api/sellOrderDetail" )
    public RspBase<RspSellOrderDetail> sellOrderDetail( @RequestBody @Validated ReqTransaction reqTransaction ) {
        return walletTransactionService.sellOrderDetail( MemberSecurityUtils.getUserId(), reqTransaction.getTransactionId() );
    }

    @Operation( summary = "取消挂单" )
    @PostMapping( "/api/cancelSellOrder" )
    public RspBase<?> cancelSellOrder( @RequestBody @Validated ReqTransaction reqTransaction ) {
        return walletTransactionService.cancelSellOrder( MemberSecurityUtils.getUserId(), reqTransaction.getTransactionId() );
    }

    @Operation( summary = "我的挂单 - 挂单列表" )
    @PostMapping( "/api/sellOrderList" )
    public RspBase<List<RspSellOrderDetail>> sellOrderList( @RequestBody ReqSellOrderList reqSellOrderList ) {
        startPage( reqSellOrderList );
        List<RspSellOrderDetail> resultList = walletTransactionService.sellOrderList( MemberSecurityUtils.getUserId(),
                reqSellOrderList );
        return getRspBasePage( resultList, reqSellOrderList );
    }

    @Operation( summary = "交易中心 - 交易中心挂单列表" )
    @PostMapping( "/api/transSellOrderList" )
    public RspBase<List<RspTransCenterDetail>> transSellOrderList( @RequestBody ReqTransCenterDetail reqTransCenterDetail ) {
        startPage( reqTransCenterDetail );
        List<RspTransCenterDetail> resultList = walletTransactionService.transSellOrderList( reqTransCenterDetail );
        return getRspBasePage( resultList, reqTransCenterDetail );
    }

    @Operation( summary = "购买G币 - 挂单详情" )
    @PostMapping( "/api/toBuySellOrderDetail" )
    public RspBase<RspSellOrderDetail2> toBuySellOrderDetail( @RequestBody @Validated ReqTransaction reqTransaction ) {
        return walletTransactionService.toBuySellOrderDetail( MemberSecurityUtils.getUserId(), reqTransaction.getTransactionId() );
    }
}
