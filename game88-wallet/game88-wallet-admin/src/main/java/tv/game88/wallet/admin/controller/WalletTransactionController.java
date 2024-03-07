package tv.game88.wallet.admin.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.entity.WalletTransaction;
import tv.game88.wallet.api.service.WalletTransactionService;

import java.util.List;

@RestController
@RequestMapping("/admin/walletTransaction")
public class WalletTransactionController extends BaseController {

    @Resource
    WalletTransactionService walletTransactionService;

    @GetMapping( "/list" )
    public RspBase<List<WalletTransaction>> list( WalletTransaction walletTransaction ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<WalletTransaction> list = walletTransactionService.getWalletTransactionList( walletTransaction );
        return getRspBasePage( list, pageDomain );
    }
}
