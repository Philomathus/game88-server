package tv.game88.game.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.enums.EnumReqTime;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.game.api.dto.*;
import tv.game88.game.api.dto.RspWashCodeInfo;
import tv.game88.game.api.service.MemberGameDataService;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "洗码相关接口" )
@Log4j2
public class WashCodeController extends BaseController {
    @Resource
    private MemberGameDataService memberGameDataService;

    @Operation( summary = "会员洗码详情" )
    @PostMapping( "/cleanCodeDetail" )
    public RspBase<RspCleanCodeInfo> cleanCodeDetail() {
        return memberGameDataService.cleanCodeDetail( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "会员手动洗码" )
    @PostMapping( "/cleanCode" )
    public RspBase<RspCleanCodeInfo> cleanCode() {
        return memberGameDataService.cleanCode( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "会员洗码记录" )
    @PostMapping( "/cleanCodeLogs" )
    public RspBase<List<RspCleanCodeLog>> cleanCodeLogs( @RequestBody PageDomain pageDomain ) {
        startPage( pageDomain );
        return getRspBasePage( memberGameDataService.cleanCodeLogs( MemberSecurityUtils.getUserId() ), pageDomain );
    }

    @Operation( summary = "游戏类别列表" )
    @PostMapping( "/getGameCategoryList" )
    public RspBase<List<RspGameCategory>> getGameCategoryList() {
        return RspBase.ok( EnumGameCategory.getGameCategorys() );
    }

    @Operation( summary = "投注记录" )
    @PostMapping( "/getGameDataList" )
    public RspBase<List<RspGameData>> getGameDataList( @RequestBody ReqGameData reqGameData ) {
        startPage( reqGameData );
        if ( reqGameData.getEnumReqTime() == null ) {
            reqGameData.setEnumReqTime( EnumReqTime.today );
        }
        return getRspBasePage( memberGameDataService.getGameDataList( MemberSecurityUtils.getUserId(), reqGameData ),
                reqGameData );
    }

    // ========== 新洗码接口 ============

    @Operation( summary = "洗码比例 - 新" )
    @PostMapping( "/getWashCodeRateList" )
    public RspBase<List<RspWashCodeRate>> getWashCodeRateList() {
        return RspBase.ok( memberGameDataService.getWashCodeRateList() );
    }

    @Operation( summary = "会员洗码详情 - 新" )
    @PostMapping( "/getWashCodeDetail" )
    public RspBase<RspWashCodeInfo> getWashCodeDetail() {
        return memberGameDataService.getWashCodeDetail( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "会员洗码记录 - 新" )
    @PostMapping( "/getWashCodeLogs" )
    public RspBase<List<RspGameWashCodeLog>> getWashCodeLogs( @RequestBody PageDomain pageDomain ) {
        startPage( pageDomain );
        return getRspBasePage( memberGameDataService.getWashCodeLogs( MemberSecurityUtils.getUserId() ), pageDomain );
    }

    @Operation( summary = "会员手动洗码 - 新" )
    @PostMapping( "/toWashCode" )
    public RspBase<RspWashCodeInfo> toWashCode() {
        return memberGameDataService.toWashCode( MemberSecurityUtils.getUserId() );
    }
}
