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
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.member.entity.MemberBcode;
import tv.game88.platform.api.service.MemberBcodeService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 会员打码数据Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/member/memberBcode" )
public class MemberBcodeController extends BaseController {
    @Resource
    private MemberBcodeService memberBcodeService;

    /**
     * 查询会员打码数据列表
     */
    @PreAuthorize( "@ss.hasPermi('member:memberBcode:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberBcode>> list( MemberBcode memberBcode ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberBcode> list = memberBcodeService.selectMemberBcodeList( memberBcode );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 统计会员打码数据
     */
    @PreAuthorize( "@ss.hasPermi('member:memberBcode:list')" )
    @GetMapping( "/getTotalData" )
    public RspBase<MemberBcode> getTotalData( MemberBcode memberBcode ) {
        return memberBcodeService.getTotalData( memberBcode );
    }

    /**
     * 导出会员打码数据列表
     */
    @PreAuthorize( "@ss.hasPermi('member:memberBcode:export')" )
    @Log( title = "会员打码数据", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( MemberBcode memberBcode, HttpServletResponse response ) {
        List<MemberBcode> list = memberBcodeService.selectMemberBcodeList( memberBcode );
        ExportExcelUtil.exportExcel( list, "会员打码数据", "会员打码数据表", MemberBcode.class, response );
    }

    /**
     * 获取会员打码数据详细信息
     */
    @PreAuthorize( "@ss.hasPermi('member:memberBcode:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<MemberBcode> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberBcodeService.getById( id ) );
    }

    /**
     * 修改会员打码数据
     */
    @PreAuthorize( "@ss.hasPermi('member:memberBcode:edit')" )
    @Log( title = "修改会员打码数据", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody MemberBcode memberBcode ) throws Exception {
        SecurityUtils.verifyMFACode( memberBcode.getGoogleAuthCode() );
        return toResult( memberBcodeService.updateMemberBcode( memberBcode ) );
    }

}
