package tv.game88.general.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.general.api.entity.ReportPlamGameschilds;
import tv.game88.general.api.service.IReportPlamGameschildsService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 游戏投注报表子表Controller
 *
 * @author 77tv
 * @date 2021-02-20
 */
@RestController
@RequestMapping( "/admin/reportPlamGameschilds" )
public class ReportPlamGameschildsController extends BaseController {
    @Resource
    private IReportPlamGameschildsService reportPlamGameschildsService;

    /**
     * 查询游戏投注报表子表列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:report-plam-games:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ReportPlamGameschilds>> list( ReportPlamGameschilds reportPlamGameschilds ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ReportPlamGameschilds> list = reportPlamGameschildsService.selectReportPlamGameschildsList( reportPlamGameschilds );
        return getRspBasePage( list, pageDomain );
    }
}
