package tv.game88.pay.admin.controller;

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
import tv.game88.pay.api.dto.ReqMemberRechargeUsdt;
import tv.game88.pay.api.entity.MemberRechargeUsdt;
import tv.game88.pay.api.service.MemberRechargeUsdtService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * USDT充值信息Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/memberRechargeUsdt" )
public class MemberRechargeUsdtController extends BaseController {
    @Resource
    private MemberRechargeUsdtService memberRechargeUsdtService;

    /**
     * 查询USDT充值信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeUsdt:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberRechargeUsdt>> list( ReqMemberRechargeUsdt req ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberRechargeUsdt> list = memberRechargeUsdtService.selectMemberRechargeUsdtList( req );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询USDT充值信息列表统计
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeUsdt:list')" )
    @GetMapping( "/listCount" )
    public RspBase<Map> listCount( ReqMemberRechargeUsdt req ) {
        return memberRechargeUsdtService.listCount( req );
    }

    /**
     * 导出USDT充值信息列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeUsdt:export')" )
    @Log( title = "USDT充值信息", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<MemberRechargeUsdt>> export(ReqMemberRechargeUsdt req, HttpServletResponse response ) {
        List<MemberRechargeUsdt> list = memberRechargeUsdtService.selectMemberRechargeUsdtList( req );
        return RspBase.ok( list );
    }

    /**
     * 获取USDT充值信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeUsdt:query')" )
    @GetMapping( value = "/{orderNo}" )
    public RspBase<?> getInfo( @PathVariable( "orderNo" ) String orderNo ) {
        return RspBase.ok( memberRechargeUsdtService.getById( orderNo ) );
    }

    /**
     * 锁定USDT充值信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeUsdt:edit')" )
    @Log( title = "锁定USDT充值信息", businessType = BusinessType.UPDATE )
    @GetMapping( value = "/lock/{orderNo}" )
    public RspBase<?> lock( @PathVariable( "orderNo" ) String orderNo ) {
        return memberRechargeUsdtService.lock( orderNo, SecurityUtils.getUsername() );
    }

    /**
     * 解锁USDT充值信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeUsdt:edit')" )
    @Log( title = "解锁USDT充值信息", businessType = BusinessType.UPDATE )
    @GetMapping( value = "/unLock/{orderNo}" )
    public RspBase<?> unLock( @PathVariable( "orderNo" ) String orderNo ) {
        LoginUser     loginUser = SecurityUtils.getLoginUser();
        String        userName  = loginUser.getUsername();
        List<SysRole> roles     = loginUser.getUser().getRoles();
        boolean       contains  = roles.stream().anyMatch( m -> "common".equals( m.getRoleKey() ) );
        return memberRechargeUsdtService.unLock( orderNo, userName, contains );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeUsdt:edit')" )
    @Log( title = "拒绝USDT充值信息", businessType = BusinessType.UPDATE )
    @PutMapping( "/refused/{orderNo}" )
    public RspBase<?> refuse( @PathVariable( "orderNo" ) String orderNo, @RequestBody String remark ) {
        return memberRechargeUsdtService.refused( orderNo, SecurityUtils.getUsername(), remark );
    }

    @PreAuthorize( "@ss.hasPermi('pay:memberRechargeUsdt:edit')" )
    @Log( title = "通过USDT充值信息", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody MemberRechargeUsdt memberRechargeUsdt ) throws Exception {
        SecurityUtils.verifyMFACode( memberRechargeUsdt.getGoogleAuthCode() );
        return memberRechargeUsdtService.updateMemberRechargeUsdt( memberRechargeUsdt, SecurityUtils.getUsername() );
    }

}
