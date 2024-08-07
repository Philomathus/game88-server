package tv.game88.platform.admin.controller;

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
import tv.game88.platform.api.entity.LogCommission;
import tv.game88.platform.api.service.CommissionRecordsService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 佣金领取日志Controller
 *
 * @author 77tv
 * @date 2021-01-27
 */
@RestController
@RequestMapping( "/member/commissionRecords" )
public class CommissionRecordsController extends BaseController {
    @Resource
    private CommissionRecordsService commissionRecordsService;

    /**
     * 查询佣金领取日志列表
     */
    @PreAuthorize( "@ss.hasPermi('member:commissionRecords:list')" )
    @GetMapping( "/list" )
    public RspBase<List<LogCommission>> list( LogCommission commissionRecords ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<LogCommission> list = commissionRecordsService.selectLogCommissionList( commissionRecords );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出佣金领取日志列表
     */
    @PreAuthorize( "@ss.hasPermi('member:commissionRecords:export')" )
    @Log( title = "佣金领取日志", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( LogCommission commissionRecords, HttpServletResponse response ) {
        List<LogCommission> list = commissionRecordsService.selectLogCommissionList( commissionRecords );
        ExportExcelUtil.exportBigExcel( list, "佣金领取日志", "佣金领取日志表", LogCommission.class, response );
    }

}
