package tv.game88.pay.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.pay.api.dto.ReqMemberRechargeOnline;
import tv.game88.pay.api.dto.RspRechargeOnline;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.service.MemberRechargeOnlineService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 线上充值信息Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/memberRechargeOnline" )
public class MemberRechargeOnlineController extends BaseController {
    @Resource
    private MemberRechargeOnlineService memberRechargeOnlineService;

    /**
     * 查询线上充值信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeOnline:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberRechargeOnline>> list( ReqMemberRechargeOnline reqMemberRechargeOnline ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberRechargeOnline> list = memberRechargeOnlineService.selectMemberRechargeOnlineList( reqMemberRechargeOnline );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出线上充值信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeOnline:export')" )
    @Log( title = "memberRechargeOnline", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReqMemberRechargeOnline reqMemberRechargeOnline, HttpServletResponse response ) {
        List<MemberRechargeOnline> list = memberRechargeOnlineService.selectMemberRechargeOnlineList( reqMemberRechargeOnline );
        ExportExcelUtil.exportExcel( list, "线上充值信息", "线上充值信息", MemberRechargeOnline.class, response );
    }

    /**
     * 获取线上充值信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeOnline:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<MemberRechargeOnline> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberRechargeOnlineService.selectMemberRechargeOnlineById( id ) );
    }

    /**
     * 线上充值信息统计
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeOnline:list')" )
    @GetMapping( "/listCount" )
    @Log( title = "线上充值信息统计", businessType = BusinessType.EXPORT )
    public Map listCount( ReqMemberRechargeOnline reqMemberRechargeOnline ) {
        return memberRechargeOnlineService.listCount( reqMemberRechargeOnline );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeOnline:patchOrder')" )
    @Log( title = "线上支付人工补单", businessType = BusinessType.AUDIT )
    @PutMapping( value = "/payPatchOrder" )
    public RspBase<?> payPatchOrder( @RequestBody MemberRechargeOnline memberRechargeOnline ) throws Exception {
        SecurityUtils.verifyMFACode( memberRechargeOnline.getGoogleAuthCode() );
        memberRechargeOnline.setRemark( "人工补单操作人：" + SecurityUtils.getUsername() );
        return memberRechargeOnlineService.payPatchOrder( memberRechargeOnline );
    }

    // ------------- report -------------

    /**
     * 导出线上充值信息报表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeOnline:export')" )
    @Log( title = "线上充值信息", businessType = BusinessType.EXPORT )
    @GetMapping( "/exportReport" )
    public void exportReport( ReqMemberRechargeOnline req, HttpServletResponse response ) {
        List<RspRechargeOnline> list = memberRechargeOnlineService.selectRspReportList( req );
        ExportExcelUtil.exportExcel( list, "线上充值报表", "线上充值报表", RspRechargeOnline.class, response );
    }


    /**
     * 查询线上充值信息报表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeOnline:list')" )
    @GetMapping( "/report" )
    public RspBase<List<RspRechargeOnline>> report( ReqMemberRechargeOnline req ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<RspRechargeOnline> list = memberRechargeOnlineService.selectRspReportList( req );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 报表统计
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeOnline:list')" )
    @GetMapping( "/reportListCount" )
    public Map reportListCount( ReqMemberRechargeOnline req ) {
        return memberRechargeOnlineService.reportListCount( req );
    }

}