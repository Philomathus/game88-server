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
import tv.game88.pay.api.dto.ReqMemberRechargeBank;
import tv.game88.pay.api.entity.MemberRechargeBank;
import tv.game88.pay.api.service.MemberRechargeBankService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 公司入款信息Controller
 *
 * @author 77tv
 */
@RestController
@RequestMapping( "/pay/memberRechargeBank" )
public class MemberRechargeBankController extends BaseController {
    @Resource
    private MemberRechargeBankService memberRechargeBankService;

    /**
     * 查询公司入款信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBank:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberRechargeBank>> list( ReqMemberRechargeBank req ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberRechargeBank> list = memberRechargeBankService.selectMemberRechargeBankList( req );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询公司入款信息列表统计
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBank:list')" )
    @GetMapping( "/listCount" )
    public Map listCount( ReqMemberRechargeBank req ) {
        return memberRechargeBankService.selectReportListCount( req );
    }

    /**
     * 导出公司入款信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBank:export')" )
    @Log( title = "公司入款信息", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReqMemberRechargeBank req, HttpServletResponse response ) {
        List<MemberRechargeBank> list = memberRechargeBankService.selectMemberRechargeBankList( req );
        if ( list.size() <= 200000 ) {
            ExportExcelUtil.exportExcel( list, "公司入款", "公司入款信息表", MemberRechargeBank.class, response );
        }
    }

    /**
     * 获取公司入款信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBank:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<?> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberRechargeBankService.getById( id ) );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBank:firstAudit')" )
    @Log( title = "公司入款信息初审", businessType = BusinessType.AUDIT )
    @PutMapping( "/firstAudit" )
    public RspBase<?> firstAudit( @RequestBody ReqMemberRechargeBank req ) {
        return memberRechargeBankService.firstAudit( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBank:finalAudit')" )
    @Log( title = "公司入款信息终审", businessType = BusinessType.AUDIT )
    @PutMapping( "/finalAudit" )
    public RspBase<?> finalAudit( @RequestBody ReqMemberRechargeBank req ) {
        return memberRechargeBankService.finalAudit( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBank:refusedAudit')" )
    @Log( title = "公司入款信息拒绝审核", businessType = BusinessType.AUDIT )
    @PutMapping( "/refusedAudit" )
    public RspBase<?> refusedAudit( @RequestBody ReqMemberRechargeBank req ) {
        return memberRechargeBankService.refusedAudit( req, SecurityUtils.getUsername() );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeBank:recoverAudit')" )
    @Log( title = "公司入款信息恢复审核", businessType = BusinessType.AUDIT )
    @PutMapping( "/recoverAudit" )
    public RspBase<?> recoverAudit( @RequestBody ReqMemberRechargeBank req ) {
        return memberRechargeBankService.recoverAudit( req, SecurityUtils.getUsername() );
    }
}
