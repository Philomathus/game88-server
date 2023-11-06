package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.entity.MemberInfoHistory;
import tv.game88.platform.api.service.MemberInfoHistoryService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member/memberInfoHistory")
public class MemberInfoHistoryController extends BaseController {

    @Resource
    private MemberInfoHistoryService memberInfoHistoryService;

    /**
     * 获取用户信息详细信息History
     */
    @PreAuthorize( "@ss.hasPermi('member:infoHistory:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberInfoHistory>> list( MemberInfoHistory memberInfoHistory ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberInfoHistory> list = memberInfoHistoryService.memberInfoHistoryList( memberInfoHistory);
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 获取用户信息详细信息 get data by id
     */
    @PreAuthorize( "@ss.hasPermi('member:infoHistory:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<MemberInfoHistory> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberInfoHistoryService.getById( id ) );
    }

    /**
     * 导出用户信息列表 export member info history data
     */
    @PreAuthorize( "@ss.hasPermi('member:infoHistory:export')" )
    @Log( title = "导出", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<?> export( MemberInfoHistory memberInfo, HttpServletResponse response ) {
        List<MemberInfoHistory> list = memberInfoHistoryService.memberInfoHistoryList( memberInfo );
        if ( list.size() <= 200000L ) {
            ExportExcelUtil.exportExcel( list, "用户信息", "用户信息表", MemberInfo.class, response );
            return RspBase.ok( "下载成功" );
        } else {
            return RspBase.businessError( "导出条数超过20万条" );
        }
    }

    /**
     * 获取完整手机号 get member full number
     */
    @PreAuthorize( "@ss.hasPermi('member:infoHistory:fullMobile')" )
    @GetMapping( value = "/fullMobile/{id}" )
    public RspBase<MemberInfoHistory> fullMobile( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberInfoHistoryService.getById( id ) );
    }

    /**
     * 获取用户线上充值历史金额
     */
    @PreAuthorize( "@ss.hasPermi('member:infoHistory:query')" )
    @GetMapping( value = "/getHistoryRecharge/{id}" )
    public RspBase<BigDecimal> getHistoryRecharge( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberInfoHistoryService.getHistoryRecharge( id ) );
    }

    /**
     * 会员银行卡列表
     */
    @GetMapping( value = "/card-list" )
    public RspBase<List<MemberCard>> findMemberCardList( String memberId ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberCard> list = memberInfoHistoryService.selectMemberCardList( memberId );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 会员个人报表
     *
     * @param memberId
     * @param request
     */
    @GetMapping( "/personal-report/{memberId}" )
    public RspBase<?> personalReport( @PathVariable String memberId, String[] date, HttpServletRequest request ) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String                startTime    = parameterMap.get( "date[0]" )[ 0 ];
        String                endTime      = parameterMap.get( "date[1]" )[ 0 ];
        return memberInfoHistoryService.personalReport( startTime, endTime, memberId );
    }


    /**
     * 统计会员余额
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:query')" )
    @GetMapping( "/listCount" )
    public Map listCount( MemberInfoHistory memberInfoHistory ) {
        return memberInfoHistoryService.listCount( memberInfoHistory );
    }



}













