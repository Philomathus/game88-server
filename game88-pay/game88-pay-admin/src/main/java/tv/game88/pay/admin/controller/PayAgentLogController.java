package tv.game88.pay.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.service.PayAgentLogService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 【代付下单日志】Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payAgentLog" )
public class PayAgentLogController extends BaseController {
    @Resource
    private PayAgentLogService payAgentLogService;

    /**
     * 查询【代付下单日志】列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentLog:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayAgentLog>> list( PayAgentLog payAgentLog ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayAgentLog> list = payAgentLogService.selectPayAgentLogList( payAgentLog );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出【代付下单日志】列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentLog:export')" )
    @Log( title = "代付下单日志", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( PayAgentLog payAgentLog, HttpServletResponse response ) {
        List<PayAgentLog> list = payAgentLogService.selectPayAgentLogList( payAgentLog );
        ExportExcelUtil.exportExcel( list, "代付下单日志", "代付下单日志信息表", PayAgentLog.class, response );
    }

    /**
     * 获取【代付下单日志】详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:payAgentLog:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<?> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( payAgentLogService.getById( id ) );
    }
}
