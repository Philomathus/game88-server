package tv.game88.wallet.admin.controller;


import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.entity.WalletUserFundLog;
import tv.game88.wallet.api.service.WalletUserFundLogService;

import java.util.List;


@RestController
@RequestMapping("/admin/walletUserFundLog")
public class walletUserFundLogController extends BaseController {

    @Resource
    private WalletUserFundLogService walletUserFundLogService;

    @GetMapping("/list")
    public RspBase<List<WalletUserFundLog>> list( WalletUserFundLog walletUserFundLog ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );

        List<WalletUserFundLog> walletUserFundLogList = walletUserFundLogService.getWalletUserFundLog( walletUserFundLog );
        return getRspBasePage( walletUserFundLogList, pageDomain );
    }

    @GetMapping(value = "/{id}")
    public RspBase<WalletUserFundLog> getInfo( @PathVariable("id") Long id ) {
        return RspBase.ok( walletUserFundLogService.getById( id ) );
    }
}
