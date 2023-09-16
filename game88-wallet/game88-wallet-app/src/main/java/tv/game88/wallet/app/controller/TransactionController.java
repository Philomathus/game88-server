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
import tv.game88.wallet.api.service.WalletTransactionService;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import javax.annotation.Resource;

@RestController
@Tag( name = "交易接口" )
@Log4j2
public class TransactionController extends BaseController {
    @Resource
    private WalletTransactionService walletTransactionService;

    @Operation( summary = "卖币" )
    @PostMapping( "/api/sellCoins" )
    public RspBase<?> sellCoins( @RequestBody @Validated ReqSellCoins reqSellCoins ) {
        return walletTransactionService.sellCoins( MemberSecurityUtils.getUserId(), reqSellCoins );
    }

}
