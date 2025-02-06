package tv.game88.platform.admin.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.platform.api.dto.ReportPlamGamesChildVo;
import tv.game88.platform.api.entity.ReportPlamGameschilds;
import tv.game88.platform.api.service.ReportPlamGameschildsService;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏投注报表子表Controller
 *
 * @author 77tv
 * @date 2021-02-20
 */
@RestController
@RequestMapping( "/report/plamGamesChilds" )
public class ReportPlamGameschildsController extends BaseController {
    @Resource
    private ReportPlamGameschildsService reportPlamGameschildsService;

    /**
     * 查询游戏投注报表子表列表
     */
    @PreAuthorize( "@ss.hasPermi('report:plam-games:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ReportPlamGameschilds>> list( ReportPlamGameschilds reportPlamGameschilds ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ReportPlamGameschilds> list = reportPlamGameschildsService.selectReportPlamGameschildsList( reportPlamGameschilds );
        return getRspBasePage( list, pageDomain );
    }

    @GetMapping(value = "/plamGameListData")
    public RspBase<List<ReportPlamGameschilds>> listData(ReportPlamGameschilds reportPlamGameschilds) {

        String platformId = reportPlamGameschildsService.getPlatformId( reportPlamGameschilds );
        reportPlamGameschilds.setGameplamid(platformId);

        startPage( reportPlamGameschilds );
        List<ReportPlamGameschilds> list = reportPlamGameschildsService.selectByBettorsCount(reportPlamGameschilds);
        return getRspBasePage(list, reportPlamGameschilds);
    }

    /**
     * 导出投注详情 Export betting details
     */
    @GetMapping("/export")
    public void export(ReportPlamGameschilds reportPlamGamesChilds, HttpServletResponse response) {
        List<ReportPlamGameschilds> list = reportPlamGameschildsService.selectByBettorsCount(reportPlamGamesChilds);
        List<ReportPlamGamesChildVo> voList = list.stream().map( child ->
                ReportPlamGamesChildVo
                        .builder()
                        .gamecell(child.getGamecell().toString())
                        .gameprofit(child.getGameprofit().toString())
                        .agentchild(child.getAgentchild()).build()
        ).collect( Collectors.toList());
        ExportExcelUtil.exportBigExcel(voList, "综合数据报表", "综合数据报表", ReportPlamGamesChildVo.class, response);
    }
}
