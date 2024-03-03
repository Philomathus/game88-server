package tv.game88.wallet.admin.controller;


import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.entity.WalletHistory;
import tv.game88.wallet.api.service.WalletHistoryService;

import java.util.List;

@RestController
@RequestMapping("/admin/walletHistory")
public class WalletHistoryController extends BaseController {

    @Resource
    WalletHistoryService walletHistoryService;

    @GetMapping( "/list" )
    public RspBase<List<WalletHistory>> list( WalletHistory walletHistory ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<WalletHistory> list = walletHistoryService.selectWalletHistoryList( walletHistory );
        return getRspBasePage( list, pageDomain );
    }
}
