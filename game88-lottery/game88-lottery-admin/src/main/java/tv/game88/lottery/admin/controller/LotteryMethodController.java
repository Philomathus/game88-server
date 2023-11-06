package tv.game88.lottery.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.lottery.api.entity.LotteryMethod;
import tv.game88.lottery.api.service.LotteryMethodService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 彩票种类Controller
 *
 * @author mengJun
 * @date 2022-12-21
 */
@RestController
@RequestMapping("/lottery/method")
public class LotteryMethodController extends BaseController {

    @Resource
    private LotteryMethodService lotteryMethodService;

    /**
     * 查询彩票种类列表 lottery method list
     */
    @PreAuthorize( "@ss.hasPermi('lottery:method:list')" )
    @GetMapping( "/list" )
    public RspBase<List<LotteryMethod>> list( LotteryMethod lotteryMethod ){
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<LotteryMethod> list = lotteryMethodService.selectLotteryMethodList( lotteryMethod );
        return getRspBasePage( list, pageDomain );
    }



}
