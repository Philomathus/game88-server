package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import tv.game88.common.base.BaseController;
import tv.game88.wallet.api.service.WalletTransactionService;

import javax.annotation.Resource;

@Controller
@Tag( name = "交易接口" )
@Log4j2
public class TransactionController extends BaseController {
    @Resource
    private WalletTransactionService walletTransactionService;
}
