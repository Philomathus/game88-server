package tv.game88.lottery.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.lottery.entity.LotteryBet;
import tv.game88.lottery.api.service.LotteryBetService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 彩票会员下注详情Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/lottery/lotteryBet" )
public class LotteryBetController extends BaseController {
    @Resource
    private LotteryBetService lotteryBetService;

    /**
     * 查询彩票会员下注详情列表
     */
    @PreAuthorize( "@ss.hasPermi('lottery:bet:list')" )
    @GetMapping( "/list" )
    public RspBase<List<LotteryBet>> list( LotteryBet lotteryBet ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<LotteryBet> list = lotteryBetService.selectLotteryBetList( lotteryBet );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出彩票会员下注详情列表
     */
    @PreAuthorize( "@ss.hasPermi('lottery:bet:export')" )
    @Log( title = "彩票会员下注详情", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( LotteryBet lotteryBet, HttpServletResponse response ) {
        List<LotteryBet> list = lotteryBetService.selectLotteryBetList( lotteryBet );
        ExportExcelUtil.exportExcel( list, "彩票会员下注详情", "彩票会员下注详情表", LotteryBet.class, response );
    }

    /**
     * 获取彩票会员下注详情详细信息
     */
    @PreAuthorize( "@ss.hasPermi('lottery:bet:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<LotteryBet> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( lotteryBetService.getById( id ) );
    }
}