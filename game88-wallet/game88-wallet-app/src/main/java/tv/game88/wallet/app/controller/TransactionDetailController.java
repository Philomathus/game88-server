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
        return walletTransactionDetailService.buyOrderDetail( MemberSecurityUtils.getUserId(), reqTransactionDetail.getTransDetailId() );
    }
}
