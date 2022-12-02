package tv.game88.platform.admin.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.platform.api.entity.ReportMoneyinfo;
import tv.game88.platform.api.service.ReportMoneyinfoService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

/**
 * 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额Controller
 *
 * @author 77tv
 * @date 2021-01-25
 */
@RestController
@RequestMapping( "/report/moneyInfo" )
@Log4j2
public class ReportMoneyinfoController extends BaseController {
    @Resource
    private ReportMoneyinfoService reportMoneyinfoService;

    /**
     * 查询平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额列表
     */
    @PreAuthorize( "@ss.hasPermi('web:report-moneyinfo:list')" )
    @GetMapping( "/list" )
    public Object list( ReportMoneyinfo reportMoneyinfo ) throws ParseException {
        return reportMoneyinfoService.selectReportMoneyinfoList( reportMoneyinfo );
    }

    @GetMapping( value = "/count" )
    public RspBase<?> countMoneyData( ReportMoneyinfo reportMoneyinfo ) throws ParseException {
        ReportMoneyinfo reportMoneyinfo1 = reportMoneyinfoService.countMoneyData( reportMoneyinfo );
        return RspBase.ok( reportMoneyinfo1 );
    }

    @PreAuthorize( "@ss.hasPermi('web:report-moneyinfo:export')" )
    @Log( title = "平台资金报表导出", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReportMoneyinfo reportMoneyinfo, HttpServletResponse response ) throws ParseException {
        List<ReportMoneyinfo> list = reportMoneyinfoService.exportMoneyinfoList( reportMoneyinfo );
        ExportExcelUtil.exportExcel( list, "平台资金报表", "平台资金报表", ReportMoneyinfo.class, response );
    }

}