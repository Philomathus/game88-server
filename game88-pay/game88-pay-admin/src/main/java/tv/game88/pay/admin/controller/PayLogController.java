package tv.game88.pay.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.pay.api.entity.PayLog;
import tv.game88.pay.api.service.PayLogService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 支付日志Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payLog" )
public class PayLogController extends BaseController {
    @Resource
    private PayLogService payLogService;

    /**
     * 查询支付日志列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payLog:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayLog>> list( PayLog payLog ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayLog> list = payLogService.selectPayLogList( payLog );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出支付日志列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payLog:export')" )
    @Log( title = "支付日志", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<PayLog>> export( PayLog payLog ) {
        return RspBase.ok( payLogService.selectPayLogList( payLog ) );
    }

    /**
     * 获取支付日志详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:payLog:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<PayLog> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( payLogService.getById( id ) );
    }
}
