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
import tv.game88.pay.api.entity.RechargeLog;
import tv.game88.pay.api.service.RechargeLogService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 充值日志服务 controller
 *
 * @author Rajesh
 * @date 2023-05-20
 */

@RestController
@RequestMapping("/pay/rechargeLog")
public class RechargeLogController extends BaseController {

    @Resource
    private RechargeLogService rechargeLogService;

    /**
     * 充值日志列表 - recharge log list controller
     *
     * @param rechargeLog  - list of recharge log
     * @return 返回充值日志列表 - ist of recharge log
     */
    @PreAuthorize( "@ss.hasPermi('pay:rechargeLog:list')" )
    @GetMapping("/list")
    public RspBase<List<RechargeLog>> list( RechargeLog rechargeLog ){
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        return getRspBasePage( rechargeLogService.selectAllRechargeLog( rechargeLog ) );
    }

    /**
     * 导出充值日志列表
     * export recharge log
     */
    @PreAuthorize( "@ss.hasPermi('pay:rechargeLog:export')" )
    @Log( title = "充值日志列表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( RechargeLog rechargeLog, HttpServletResponse response ) {
        List<RechargeLog> list = rechargeLogService.selectAllRechargeLog( rechargeLog );
        ExportExcelUtil.exportBigExcel( list, "导出充值日志列表", "导出充值日志列表", RechargeLog.class, response );
    }


}
