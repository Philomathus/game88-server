package tv.game88.wallet.admin.controller;

import jakarta.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import tv.game88.common.security.annotation.Anonymous;
import tv.game88.wallet.api.service.WalletUserPayMethodService;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.page.PageDomain;
import tv.game88.common.vo.RspBase;

import java.util.List;


@RestController
@RequestMapping("/admin/walletUserPayMethod")
public class WalletUserPayMethodController extends BaseController {

    @Resource
    private WalletUserPayMethodService walletUserPayMethodService;

    @Resource
    private RedisUtils redisUtils;


    @GetMapping("/list")
    public RspBase<List<WalletUserPayMethod>> list( WalletUserPayMethod walletUserPayMethod ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );

        List<WalletUserPayMethod> walletUserPayMethodList = walletUserPayMethodService.getWalletUserPayMethodList( walletUserPayMethod );
        System.out.println("walletlist" + walletUserPayMethodList);
        return getRspBasePage( walletUserPayMethodList, pageDomain );
    }

    @GetMapping( value = "/{id}")
    public RspBase<WalletUserPayMethod> getInfo(@PathVariable( "id" ) Long id ) {
        return RspBase.ok( walletUserPayMethodService.getById( id ) );
    }
}
