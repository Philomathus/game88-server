package tv.game88.admin.system.controller;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.admin.system.service.ISysOperLogService;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.entity.SysOperLog;
import tv.game88.core.admin.enums.BusinessType;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/monitor/operlog" )
public class SysOperlogController extends BaseController {
    @Resource
    private ISysOperLogService operLogService;

    @PreAuthorize( "@ss.hasPermi('monitor:operlog:list')" )
    @GetMapping( "/list" )
    public RspBase<List<SysOperLog>> list( SysOperLog operLog ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<SysOperLog> list = operLogService.selectOperLogList( operLog );
        return getRspBasePage( list, pageDomain );
    }

    @Log( title = "操作日志", businessType = BusinessType.EXPORT )
    @PreAuthorize( "@ss.hasPermi('monitor:operlog:export')" )
    @GetMapping( "/export" )
    public RspBase<List<SysOperLog>> export( SysOperLog operLog ) {
        return RspBase.ok(  operLogService.selectOperLogList( operLog ) ) ;
    }
}
