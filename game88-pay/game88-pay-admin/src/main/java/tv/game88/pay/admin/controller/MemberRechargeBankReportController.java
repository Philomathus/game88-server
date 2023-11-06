package tv.game88.pay.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
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
import tv.game88.pay.api.dto.ReqMemberRechargeBank;
import tv.game88.pay.api.dto.RspRechargeBankReport;
import tv.game88.pay.api.service.MemberRechargeBankService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 公司入款银行卡简易统计Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/memberRechargeBankReport" )
public class MemberRechargeBankReportController extends BaseController {
    @Resource
    private MemberRechargeBankService memberRechargeBankService;

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBankReport:export')" )
    @Log( title = "公司入款报表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( HttpServletResponse response, ReqMemberRechargeBank req ) {
        List<RspRechargeBankReport> list = memberRechargeBankService.selectReportList( req );
        ExportExcelUtil.exportExcel( list, "公司入款", "公司入款表", RspRechargeBankReport.class, response );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBankReport:lists')" )
    @GetMapping( "/list" )
    public RspBase<List<RspRechargeBankReport>> lists( ReqMemberRechargeBank req ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<RspRechargeBankReport> list = memberRechargeBankService.selectReportList( req );
        return getRspBasePage( list, pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBankReport:query')" )
    @GetMapping( "/listCounts" )
    public Map listCounts( ReqMemberRechargeBank req ) {
        return memberRechargeBankService.selectReportListCount( req );
    }
}
