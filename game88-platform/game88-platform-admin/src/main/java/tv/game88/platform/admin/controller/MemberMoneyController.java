package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.member.entity.MemberMoney;
import tv.game88.platform.api.service.MemberMoneyService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 派送彩金暂存表Controller
 *
 * @author Rajesh
 * @date 2022-10-23
 */
@RestController
@RequestMapping( "/member/money" )
public class MemberMoneyController extends BaseController {

    @Resource
    private MemberMoneyService memberMoneyService;

    /**
     * 查询派送彩金暂存表列表 list all data
     */
    @PreAuthorize( "@ss.hasPermi('member:money:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberMoney>> list( MemberMoney memberMoney ) {
        PageDomain pages = TableSupport.buildPageRequest();
        startPage( pages );
        List<MemberMoney> memberMoneyList = memberMoneyService.selectAllMemberMoneyList( memberMoney );
        return getRspBasePage( memberMoneyList, pages );
    }

    /**
     * 通过id查询获取 query data according to id
     */
    @PreAuthorize( "@ss.hasPermi('member:money:query')" )
    @GetMapping( "/{memberId}" )
    public RspBase<MemberMoney> getById( @PathVariable String memberId ) {
        return RspBase.ok( memberMoneyService.getById( memberId ) );
    }

    /**
     * 删除派送彩金暂存表 delete data
     */
    @PreAuthorize( "@ss.hasPermi('member:money:remove')" )
    @Log( title = "派送彩金暂存表", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        return RspBase.ok( memberMoneyService.removeByIds( Arrays.asList( ids ) ) );
    }

    /**
     * 导出用户信息列表 export data
     */
    @PreAuthorize( "@ss.hasPermi('member:money:export')" )
    @Log( title = "导出", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( MemberMoney memberMoney, HttpServletResponse response ) {
        List<MemberMoney> list = memberMoneyService.selectAllMemberMoneyList( memberMoney );
        ExportExcelUtil.exportBigExcel( list, "用户信息", "用户信息表", MemberMoney.class, response );
    }

    /**
     * 修改派送彩金暂存表 update data
     */
    @PreAuthorize( "@ss.hasPermi('member:money:edit')" )
    @Log( title = "派送彩金暂存表", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody MemberMoney memberMoney ) throws Exception {
        SecurityUtils.verifyMFACode( memberMoney.getGoogleAuthCode() );
        return RspBase.ok( memberMoneyService.updateById( memberMoney ) );
    }

    /**
     * 新增派送彩金暂存表 add new data
     */
    @Log( title = "新增派送彩金", businessType = BusinessType.INSERT )
    @PostMapping
    @PreAuthorize( "@ss.hasPermi('member:money:add')" )
    public RspBase<?> add( @RequestBody MemberMoney memberMoney ) throws Exception {
        SecurityUtils.verifyMFACode( memberMoney.getGoogleAuthCode() );
        return RspBase.ok( memberMoneyService.save( memberMoney ) );
    }

    /**
     * 行为类型统计 count money
     */
    @GetMapping( "/count" )
    public BigDecimal countMoney() {
        return memberMoneyService.countMoney();
    }

    /**
     * 查询派送彩金暂存表列表
     */
    @PreAuthorize( "@ss.hasPermi(' member:money:remove')" )
    @GetMapping( "/handleClean" )
    @Log( title = "派送彩金暂存表", businessType = BusinessType.DELETE )
    public RspBase<?> handleCleanData() {
        return RspBase.ok( memberMoneyService.handleClean() );
    }

    @PreAuthorize( "@ss.hasPermi('member:money:edit')" )
    @Log( title = "开始派送彩金", businessType = BusinessType.INSERT )
    @PostMapping( "/starSend" )
    public RspBase<?> starSend( @RequestBody MemberMoney memberMoney ) throws Exception {
        SecurityUtils.verifyMFACode( memberMoney.getGoogleAuthCode() );
        return memberMoneyService.starSend( memberMoney, SecurityUtils.getUsername() );
    }
}
