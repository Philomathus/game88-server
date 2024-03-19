package tv.game88.wallet.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import tv.game88.wallet.api.service.WalletUserPayMethodService;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

import java.util.List;


@RestController
@RequestMapping("/admin/walletUserPayMethod")
public class WalletUserPayMethodController extends BaseController {

    @Resource
    private WalletUserPayMethodService walletUserPayMethodService;
    @GetMapping("/list")
    public RspBase<List<WalletUserPayMethod>> list( WalletUserPayMethod walletUserPayMethod ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );

        List<WalletUserPayMethod> walletUserPayMethodList = walletUserPayMethodService.getWalletUserPayMethodList( walletUserPayMethod );
        return getRspBasePage( walletUserPayMethodList, pageDomain );
    }

    @GetMapping( value = "/{id}")
    public RspBase<WalletUserPayMethod> getInfo(@PathVariable( "id" ) Long id ) {
        return RspBase.ok( walletUserPayMethodService.getById( id ) );
    }

    @Operation( summary = "获取支付方式类型列表" )
    @GetMapping( "/getPayMethodTypes" )
    public RspBase<List<String>> getPayMethodTypes() {
        return RspBase.ok( WalletPayMethodEnum.getPayMethodTypes() );
    }
}
