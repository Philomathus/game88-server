package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.platform.api.entity.MobileLimit;
import tv.game88.platform.api.service.MobileLimitService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/member/mobileLimit" )
public class MobileLimitController extends BaseController {
    @Resource
    private MobileLimitService mobileLimitService;

    /**
     * 查询列表
     */
    @PreAuthorize( "@ss.hasPermi('member:limit:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MobileLimit>> list( MobileLimit mobileLimit ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MobileLimit> list = mobileLimitService.selectMobileLimitList( mobileLimit );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出列表
     */
    @PreAuthorize( "@ss.hasPermi('member:limit:export')" )
    @Log( title = "限制手机号注册", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( MobileLimit mobileLimit, HttpServletResponse response ) {
        List<MobileLimit> list = mobileLimitService.selectMobileLimitList( mobileLimit );
        ExportExcelUtil.exportExcel( list, "限制手机号注册", "限制手机号注册表", MobileLimit.class, response );
    }

    /**
     * 新增
     */
    @PreAuthorize( "@ss.hasPermi('member:limit:add')" )
    @Log( title = "限制手机号注册", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody MobileLimit mobileLimit ) {
        return toResult( mobileLimitService.save( mobileLimit ) );
    }

    /**
     * 删除
     */
    @PreAuthorize( "@ss.hasPermi('member:limit:remove')" )
    @Log( title = "限制手机号注册", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{mobiles}" )
    public RspBase<?> remove( @PathVariable String[] mobiles ) {
        return toResult( mobileLimitService.removeByIds( Arrays.asList( mobiles ) ) );
    }
}