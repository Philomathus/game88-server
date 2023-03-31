package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.platform.api.dto.RspPlamGamesMonth;
import tv.game88.platform.api.entity.ReportPlamGames;
import tv.game88.platform.api.service.ReportPlamGamesService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 游戏投注报表Controller
 *
 * @author 77tv
 * @date 2021-01-26
 */
@RestController
@RequestMapping( "/report/reportPlamGames" )
public class ReportPlamGamesController extends BaseController {
    @Resource
    private ReportPlamGamesService reportPlamGamesService;

    //获取昨天数据
    private static String getYestoday() {
        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.DATE, -1 );
        Date time = cal.getTime();
        return new SimpleDateFormat( "yyyy-MM-dd" ).format( time );
    }

    /**
     * 查询游戏投注报表列表
     */
    @PreAuthorize( "@ss.hasPermi('report:plam-games:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ReportPlamGames>> list( ReportPlamGames reportPlamGames ) throws ParseException {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ReportPlamGames> list = reportPlamGamesService.selectReportPlamGamesList( reportPlamGames );
        return getRspBasePage(list, pageDomain);

    }

    @GetMapping( value = "/count" )
    public RspBase<?> countBetData( ReportPlamGames reportPlamGames ) {
        String myString = reportPlamGames.getBegindate();
        if ( StringUtils.isEmpty( myString ) ) {
            reportPlamGames.setBegindate( getYestoday() );
        }
        ReportPlamGames reportPlamGames1 = reportPlamGamesService.countBetData( reportPlamGames );
        return RspBase.ok( reportPlamGames1 );
    }

    @PreAuthorize( "@ss.hasPermi('report:plamGames:export')" )
    @Log( title = "游戏投注报表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReportPlamGames reportPlamGames, HttpServletResponse response ) {
        List<ReportPlamGames> list = reportPlamGamesService.exportPlamGamesList( reportPlamGames );
        ExportExcelUtil.exportExcel( list, "游戏投注报表", "游戏投注报表", ReportPlamGames.class, response );
    }

    @PreAuthorize( "@ss.hasPermi('report:plam-games:list')" )
    @GetMapping( "/listMonth" )
    public RspBase<List<RspPlamGamesMonth>> listMonth( ReportPlamGames reportPlamGames ) throws ParseException {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<RspPlamGamesMonth> list = reportPlamGamesService.selectReportPlamGamesListMonth( reportPlamGames );
        return getRspBasePage( list, pageDomain );
    }

    @GetMapping( value = "/countBet" )
    public RspBase<?> countBet( ReportPlamGames reportPlamGames ) throws ParseException {
        RspPlamGamesMonth rspPlamGamesMonth = reportPlamGamesService.countBet( reportPlamGames );
        return RspBase.ok( rspPlamGamesMonth );
    }
}
