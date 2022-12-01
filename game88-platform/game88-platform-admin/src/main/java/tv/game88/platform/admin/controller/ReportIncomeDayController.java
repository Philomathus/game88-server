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
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.platform.api.entity.ReportIncomeDay;
import tv.game88.platform.api.service.ReportIncomeDayService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 平台充值报表Controller
 *
 * @author 77tv
 * @date 2021-01-26
 */
@RestController
@RequestMapping( "/admin/reportIncomeDay" )
public class ReportIncomeDayController extends BaseController {
    @Resource
    private ReportIncomeDayService reportIncomeDayService;

    /**
     * 查询平台充值报表列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:reportIncomeDay:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ReportIncomeDay>> list( ReportIncomeDay reportIncomeDay ) throws ParseException {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        Date   d        = new Date();
        String myString = reportIncomeDay.getPaydate();
        if ( !StringUtils.isEmpty( myString ) ) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
            Date             dd               = simpleDateFormat.parse( myString );
            boolean          flag             = dd.before( d );
            if ( !flag ) {
                reportIncomeDay.setPaydate( null );
            }
        } else {
            reportIncomeDay.setPaydate( LocalDateTimeUtils.format( LocalDate.now().minusDays( 1 ) ) );
        }
        List<ReportIncomeDay> list = reportIncomeDayService.selectReportIncomeDayList( reportIncomeDay );
        return getRspBasePage( list, pageDomain );
    }

    @GetMapping( value = "/count" )
    public RspBase<?> countMoneyData( ReportIncomeDay reportIncomeDay ) {
        String myString = reportIncomeDay.getPaydate();
        if ( StringUtils.isEmpty( myString ) ) {
            reportIncomeDay.setPaydate( LocalDateTimeUtils.format( LocalDate.now().minusDays( 1 ) ) );
        }
        ReportIncomeDay reportIncomeDay1 = reportIncomeDayService.countSuccessData( reportIncomeDay );
        return RspBase.ok( reportIncomeDay1 );
    }

    @PreAuthorize( "@ss.hasPermi('admin:reportIncomeDay:export')" )
    @Log( title = "平台充值报表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReportIncomeDay reportIncomeDay, HttpServletResponse response ) {
        List<ReportIncomeDay> list = reportIncomeDayService.selectReportIncomeDayList( reportIncomeDay );
        ExportExcelUtil.exportExcel( list, "平台充值报表", "平台充值报表", ReportIncomeDay.class, response );
    }
}