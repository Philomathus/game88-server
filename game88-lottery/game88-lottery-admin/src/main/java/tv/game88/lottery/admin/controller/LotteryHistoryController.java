package tv.game88.lottery.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.lottery.api.entity.LotteryHistory;
import tv.game88.lottery.api.service.LotteryHistoryService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 开奖历史Controller
 */
@RestController
@RequestMapping( "/lottery/lotteryHistory" )
public class LotteryHistoryController extends BaseController {

    @Resource
    private LotteryHistoryService lotteryHistoryService;

    /**
     * 查询开奖历史列表
     */
    @PreAuthorize( "@ss.hasPermi('lottery:history:list')" )
    @GetMapping( "/list" )
    public RspBase<List<LotteryHistory>> list( LotteryHistory lotteryHistory ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<LotteryHistory> list = lotteryHistoryService.selectLotteryHistoryList( lotteryHistory );
        return getRspBasePage( list, pageDomain );
    }
}
