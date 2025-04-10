package tv.game88.pay.admin.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.entity.SysRole;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.admin.vo.LoginUser;
import tv.game88.pay.api.dto.ReqMemberWithdrawDetail;
import tv.game88.pay.api.dto.RspMemberWithdrawDetailShunWei;
import tv.game88.pay.api.dto.RspWithdrawReport;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.service.MemberWithdrawDetailService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 会员提现信息Controller
 *
 * @author mengJun
 */
@Log4j2
@RestController
@RequestMapping( "/pay/memberWithdrawDetail" )
public class MemberWithdrawDetailController extends BaseController {
    @Resource
    private MemberWithdrawDetailService memberWithdrawDetailService;

    /**
     * 查询会员提现信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberWithdrawDetail>> list( ReqMemberWithdrawDetail reqMemberWithdrawDetail ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberWithdrawDetail> list = memberWithdrawDetailService.selectMemberWithdrawDetailList( reqMemberWithdrawDetail );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 获取会员提现信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<MemberWithdrawDetail> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberWithdrawDetailService.getById( id ) );
    }

    /**
     * 获取会员提现信息统计
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:query')" )
    @GetMapping( value = "/countTotal" )
    public RspBase<?> getTotal( ReqMemberWithdrawDetail reqMemberWithdrawDetail ) {
        return RspBase.ok( memberWithdrawDetailService.getTotal( reqMemberWithdrawDetail ) );
    }

    /**
     * 获取会员提现信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:query')" )
    @GetMapping( value = "/report/{id}" )
    public RspBase<List<RspWithdrawReport>> getReport( @PathVariable( "id" ) String id ) {
        return memberWithdrawDetailService.withdrawReport( id );
    }

    /**
     * 查询资金明细列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:query')" )
    @GetMapping( value = "/reportList" )
    public RspBase<List<RspWithdrawReport>> withdrawReportList() {
        return RspBase.ok( memberWithdrawDetailService.withdrawReportList() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:list')" )
    @GetMapping( "/count" )
    public RspBase<List<MemberWithdrawDetail>> count( ReqMemberWithdrawDetail reqMemberWithdrawDetail ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberWithdrawDetail> list = memberWithdrawDetailService.selectMemberWithdrawDetailCount( reqMemberWithdrawDetail );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出会员提现信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:export')" )
    @Log( title = "会员提现信息", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<MemberWithdrawDetail>> export(ReqMemberWithdrawDetail reqMemberWithdrawDetail, HttpServletResponse response ) {
        List<MemberWithdrawDetail> list = memberWithdrawDetailService.selectMemberWithdrawDetailList( reqMemberWithdrawDetail );
//        ExportExcelUtil.exportBigExcel( list, "会员提现", "会员提现信息表", MemberWithdrawDetail.class, response );
        return RspBase.ok( list );
    }

    /**
     * 顺为代付格式导出会员提现信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:export')" )
    @Log( title = "顺为代付格式会员提现信息", businessType = BusinessType.EXPORT )
    @PostMapping( "/exportShunWei" )
    public RspBase<List<RspMemberWithdrawDetailShunWei>> exportShunWei(@RequestBody ReqMemberWithdrawDetail req, HttpServletResponse response ) {
        List<RspMemberWithdrawDetailShunWei> list = memberWithdrawDetailService.selectMemberWithdrawDetailShunWeiList( req );
//        ExportExcelUtil.exportBigExcel( list, null, "顺为格式会员提现表", RspMemberWithdrawDetailShunWei.class, response );
        return RspBase.ok( list );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:refused')" )
    @Log( title = "会员提现拒绝", businessType = BusinessType.AUDIT )
    @PutMapping( "/refused" )
    public RspBase<?> refused( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.refused( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:refused')" )
    @Log( title = "会员提现批量拒绝", businessType = BusinessType.AUDIT )
    @PutMapping( "/refuseds" )
    public RspBase<?> refuseds( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.refuseds( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:lock')" )
    @Log( title = "会员提现批量锁定", businessType = BusinessType.AUDIT )
    @PutMapping( "/locks" )
    public RspBase<?> locks( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.locks( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:back')" )
    @Log( title = "会员提现回退", businessType = BusinessType.AUDIT )
    @PutMapping( "/back" )
    public RspBase<?> back( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.back( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:back')" )
    @Log( title = "会员提现代付失败回退", businessType = BusinessType.AUDIT )
    @PutMapping( "/failBack" )
    public RspBase<?> failBack( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.failBack( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:queryStatus')" )
    @Log( title = "会员提现查询状态", businessType = BusinessType.AUDIT )
    @PutMapping( "/queryStatus" )
    public RspBase<?> queryStatus( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.queryStatus( req );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:lock')" )
    @Log( title = "会员提现锁定", businessType = BusinessType.AUDIT )
    @PutMapping( "/lock" )
    public RspBase<?> lock( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.lock( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:unlock')" )
    @Log( title = "会员提现解锁", businessType = BusinessType.AUDIT )
    @PutMapping( "/unlock" )
    public RspBase<?> unlock( @RequestBody ReqMemberWithdrawDetail req ) {
        LoginUser     loginUser = SecurityUtils.getLoginUser();
        String        userName  = loginUser.getUsername();
        List<SysRole> roles     = loginUser.getUser().getRoles();
        boolean       contains  = roles.stream().anyMatch( m -> "common".equals( m.getRoleKey() ) );
        return memberWithdrawDetailService.unlock( req, userName, contains );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:artificial')" )
    @Log( title = "会员提现人工出款", businessType = BusinessType.AUDIT )
    @PutMapping( "/artificial" )
    public RspBase<?> artificial( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.artificial( req, SecurityUtils.getUsername() );
    }

    @Log( title = "修改备注", businessType = BusinessType.AUDIT )
    @PutMapping( "/updateRemark" )
    public RspBase<?> updateRemark( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.updateRemark( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:abnormalWithdrawal')" )
    @Log( title = "会员出款异常", businessType = BusinessType.AUDIT )
    @PutMapping( "/abnormalWithdrawal" )
    public RspBase<?> abnormalWithdrawal( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.abnormalWithdrawal( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberWithdrawDetail:manualWithdrawal')" )
    @Log( title = "会员人工代付中", businessType = BusinessType.AUDIT )
    @PutMapping( "/manualWithdrawal" )
    public RspBase<?> manualWithdrawal( @RequestBody ReqMemberWithdrawDetail req ) {
        return memberWithdrawDetailService.manualWithdrawal( req, SecurityUtils.getUsername() );
    }
}
