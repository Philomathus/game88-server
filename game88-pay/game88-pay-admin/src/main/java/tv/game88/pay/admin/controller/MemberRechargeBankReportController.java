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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 线上充值信息Controller
 *
 * @author mengJun
 * @date 2021-01-26
 */
@RestController
@RequestMapping( "/pay/memberRechargeBankReport" )
public class MemberRechargeBankReportController extends BaseController {
    @Resource
    private MemberRechargeBankService memberRechargeBankService;

    /**
     * 导出线下充值信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBankReport:export')" )
    @Log( title = "线下充值报表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( HttpServletResponse response, ReqMemberRechargeBank req ) {
        List<RspRechargeBankReport> list = memberRechargeBankService.selectReportList( req );
        ExportExcelUtil.exportExcel( list, "线下充值", "线下充值表", RspRechargeBankReport.class, response );
    }

    /**
     * 查询线下充值信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBankReport:lists')" )
    @GetMapping( "/list" )
    public RspBase<List<RspRechargeBankReport>> lists( ReqMemberRechargeBank req ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<RspRechargeBankReport> list = memberRechargeBankService.selectReportList( req );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 列表统计
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBankReport:query')" )
    @GetMapping( "/listCounts" )
    public Map listCounts( ReqMemberRechargeBank req ) {
        return memberRechargeBankService.selectReportListCount( req );
    }
}
