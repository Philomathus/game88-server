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
import tv.game88.wallet.api.entity.WalletRecord;
import tv.game88.wallet.api.service.WalletRecordService;

import java.util.List;

@RestController
@RequestMapping("/admin/walletRecord")
public class WalletRecordController extends BaseController {
    @Resource
    private WalletRecordService walletRecordService;

    @GetMapping("/list")
    public RspBase<List<WalletRecord>> list( WalletRecord walletRecord ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );

        List<WalletRecord> walletRecordList = walletRecordService.getWalletRecordList( walletRecord );
        return getRspBasePage( walletRecordList, pageDomain );
    }

    @GetMapping(value = "/{id}")
    public RspBase<WalletRecord> getInfo( @PathVariable("id") Long id ) {
        return RspBase.ok( walletRecordService.getById( id ) );
    }
}
