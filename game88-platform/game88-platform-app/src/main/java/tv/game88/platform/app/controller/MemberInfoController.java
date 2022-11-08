package tv.game88.platform.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.dto.ReqLogMoney;
import tv.game88.core.member.dto.RspCodeFlow;
import tv.game88.core.member.dto.RspLogMoney;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.platform.api.dto.*;
import tv.game88.platform.api.service.MemberInfoService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "会员相关接口" )
@Log4j2
@RequestMapping( "/platform" )
public class MemberInfoController extends BaseController {
    @Resource
    private MemberInfoService memberInfoService;

    @Operation( summary = "查看是否开启保险箱", description = "返回值:true=输入密码,false设置密码" )
    @PostMapping( "/boxPassIsOpen" )
    public RspBase<?> memberBoxPassIsOpen() {
        return memberInfoService.memberBoxPassIsOpen( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "设置保险箱密码" )
    @PostMapping( "/boxPassSet" )
    public RspBase<?> memberBoxPassSet( @Validated @RequestBody ReqBoxPass boxPass ) {
        return memberInfoService.memberBoxPassSet( MemberSecurityUtils.getUserId(), boxPass );
    }

    @Operation( summary = "查看保险箱账户" )
    @PostMapping( "/boxAccount" )
    public RspBase<RspMoney> boxAccount( @Validated @RequestBody ReqBoxPass boxPass ) {
        return memberInfoService.boxAccount( MemberSecurityUtils.getUserId(), boxPass );
    }

    @Operation( summary = "保险箱转入和取出" )
    @PostMapping( "/boxTransfer" )
    public RspBase<RspMoney> boxTransfer( @Validated @RequestBody ReqBoxChange boxChange ) {
        return memberInfoService.boxTransfer( MemberSecurityUtils.getUserId(), boxChange );
    }

    @Operation( summary = "获取账户余额" )
    @PostMapping( "/getAccountNow" )
    public RspBase<RspAccountMoney> getAccountNow() {
        return memberInfoService.getAccountNow( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "获取会员信息" )
    @PostMapping( "/getAccountInfo" )
    public RspBase<RspMemberDetail> getAccountInfo() {
        return memberInfoService.getAccountInfo( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "获取会员资金明细" )
    @PostMapping( "/getFundDetails" )
    public RspBase<List<RspLogMoney>> getFundDetails( @Validated @RequestBody ReqLogMoney reqLogMoney ) {
        startPage( reqLogMoney );
        List<RspLogMoney> fundDetails = memberInfoService.getFundDetails( MemberSecurityUtils.getUserId(), reqLogMoney );
        return getRspBasePage( fundDetails, reqLogMoney );
    }

    @Operation( summary = "获取交易状态列表" )
    @PostMapping( "/getTradeTypes" )
    public RspBase<List<RspConfigTradeType>> getTradeTypes() {
        return RspBase.ok( memberInfoService.getTradeTypes() );
    }

    @Operation( summary = "获取会员资金明细" )
    @PostMapping( "/getCodeFlowList" )
    public RspBase<List<RspCodeFlow>> getCodeFlowList( @RequestBody PageDomain pageDomain ) {
        startPage( pageDomain );
        List<RspCodeFlow> codeFlows = memberInfoService.getCodeFlowList( MemberSecurityUtils.getUserId() );
        return getRspBasePage( codeFlows, pageDomain );
    }
}
