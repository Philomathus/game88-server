package tv.game88.admin.system.controller;

import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.entity.SysLogininfor;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.admin.system.service.ISysLogininforService;
import tv.game88.common.base.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 系统访问记录
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/monitor/logininfor" )
public class SysLogininforController extends BaseController {
    @Resource
    private ISysLogininforService logininforService;

    @PreAuthorize( "@ss.hasPermi('monitor:logininfor:list')" )
    @GetMapping( "/list" )
    public RspBase<List<SysLogininfor>> list( SysLogininfor logininfor ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<SysLogininfor> list = logininforService.selectLogininforList( logininfor );
        return getRspBasePage( list, pageDomain );
    }

    @Log( title = "登录日志", businessType = BusinessType.EXPORT )
    @PreAuthorize( "@ss.hasPermi('monitor:logininfor:export')" )
    @GetMapping( "/export" )
    public void export( SysLogininfor logininfor, HttpServletResponse response ) {
        List<SysLogininfor> list = logininforService.selectLogininforList( logininfor );
        ExportExcelUtil.exportBigExcel( list, "登录日志", "登录日志表", SysLogininfor.class, response );
    }
}
