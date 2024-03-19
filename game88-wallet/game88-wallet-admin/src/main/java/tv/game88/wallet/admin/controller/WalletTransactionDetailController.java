package tv.game88.wallet.admin.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.wallet.api.entity.WalletTransactionDetail;
import tv.game88.wallet.api.service.WalletTransactionDetailService;

import java.util.List;

@RestController
@RequestMapping("/admin/walletTransactionDetail")
public class WalletTransactionDetailController extends BaseController {

    @Resource
    WalletTransactionDetailService walletTransactionDetailService;

    @GetMapping( "/list" )
    public RspBase<List<WalletTransactionDetail>> list( WalletTransactionDetail walletTransactionDetail ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<WalletTransactionDetail> list = walletTransactionDetailService.getWalletTransactionList( walletTransactionDetail );
        RspBase<List<WalletTransactionDetail>> walletTransactionRsp =  getRspBasePage( list, pageDomain );

        String domainOssValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        walletTransactionRsp.setData( list.stream().peek( wtd -> wtd.setTransCertPic( domainOssValue + wtd.getTransCertPic() ) )
                .toList() );

        return walletTransactionRsp;
    }
}
