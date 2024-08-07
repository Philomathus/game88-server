package tv.game88.general.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.general.api.dto.RspPlamGamesMonth;
import tv.game88.general.api.entity.ReportPlamGames;
import tv.game88.general.api.service.IReportPlamGamesService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

/**
 * 游戏投注报表Controller
 *
 * @author 77tv
 * @date 2021-01-26
 */
@RestController
@RequestMapping( "/admin/report-plam-games" )
public class ReportPlamGamesController extends BaseController {
    @Resource
    private IReportPlamGamesService reportPlamGamesService;

    /**
     * 查询游戏投注报表列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:report-plam-games:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ReportPlamGames>> list( ReportPlamGames reportPlamGames ) throws ParseException {
        reportPlamGamesService.storage( reportPlamGames );
        PageDomain pageDomain = TableSupport.getPageDomain();
        startPage( pageDomain );
        List<ReportPlamGames> reportPlamGamesList = reportPlamGamesService.selectReportPlamGamesList( reportPlamGames );
        return getRspBasePage( reportPlamGamesList, pageDomain );
    }

    @GetMapping( value = "/count" )
    public RspBase<ReportPlamGames> countBetData( ReportPlamGames reportPlamGames ) {
        String myString = reportPlamGames.getBegindate();
        if ( !StringUtils.hasText( myString ) ) {
            reportPlamGames.setBegindate( LocalDateTimeUtils.format( LocalDate.now().minusDays( 1 ) ) );
        }
        ReportPlamGames reportPlamGames1 = reportPlamGamesService.countBetData( reportPlamGames );
        return RspBase.ok( reportPlamGames1 );
    }

    @PreAuthorize( "@ss.hasPermi('admin:reportPlamGames:export')" )
    @Log( title = "游戏投注报表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReportPlamGames reportPlamGames, HttpServletResponse response ) {
        List<ReportPlamGames> list = reportPlamGamesService.exportPlamGamesList( reportPlamGames );
        ExportExcelUtil.exportBigExcel( list, "游戏投注报表", "游戏投注报表", ReportPlamGames.class, response );
    }

    @PreAuthorize( "@ss.hasPermi('admin:report-plam-games:list')" )
    @GetMapping( "/listMonth" )
    public RspBase<List<RspPlamGamesMonth>> listMonth( ReportPlamGames reportPlamGames ) throws ParseException {
        if ( StringUtils.hasText( reportPlamGames.getAgentPlatform() ) && ( "77直播".equals( reportPlamGames.getAgentPlatform() )
                                                                                    || "7701".equals( reportPlamGames.getAgentPlatform() ) ) ) {
            reportPlamGames.setAgentPlatformLive( "cx_live" );
        } else {
            reportPlamGames.setAgentPlatformLive( reportPlamGames.getAgentPlatform() + "_live" );
        }
//        PageDomain pageDomain = TableSupport.buildPageRequest();
//        startPage( pageDomain );
        List<RspPlamGamesMonth> list = reportPlamGamesService.selectReportPlamGamesListMonth( reportPlamGames );
        return getRspBasePage( list );
    }

    @GetMapping( value = "/countBet" )
    public RspBase<RspPlamGamesMonth> countBet( ReportPlamGames reportPlamGames ) throws ParseException {
        if ( StringUtils.hasText( reportPlamGames.getAgentPlatform() ) && ( "77直播".equals( reportPlamGames.getAgentPlatform() )
                                                                                    || "7701".equals( reportPlamGames.getAgentPlatform() ) ) ) {
            reportPlamGames.setAgentPlatformLive( "cx_live" );
        } else {
            reportPlamGames.setAgentPlatformLive( reportPlamGames.getAgentPlatform() + "_live" );
        }
        RspPlamGamesMonth rspPlamGamesMonth = reportPlamGamesService.countBet( reportPlamGames );
        return RspBase.ok( rspPlamGamesMonth );
    }
}
