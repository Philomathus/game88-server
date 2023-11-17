package tv.game88.platform.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.dto.RspMemberRecommend;
import tv.game88.core.member.dto.RspMyRecommend;
import tv.game88.core.member.entity.ConfigRecommend;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.platform.api.dto.ReqMemberRecommend;
import tv.game88.platform.api.dto.RspDetailCommission;
import tv.game88.platform.api.service.RecommendService;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "推广相关接口" )
@Log4j2
public class RecommendController extends BaseController {
    @Resource
    private RecommendService recommendService;

    @Operation( summary = "推广业绩" )
    @PostMapping( "/getRecommendDetailList" )
    public RspBase<List<RspMemberRecommend>> getRecommendDetailList( @RequestBody ReqMemberRecommend req ) {
        startPage( req );
        List<RspMemberRecommend> list = recommendService.getRecommendDetailList( req.getCode(), MemberSecurityUtils.getUserId() );
        return getRspBasePage( list, req );
    }

    @Operation( summary = "我的推广" )
    @PostMapping( "/getRecommendDetail" )
    public RspBase<RspMyRecommend> getRecommendDetail() {
        return RspBase.ok( recommendService.getRecommendDetail( MemberSecurityUtils.getUserId() ) );
    }

    @Operation( summary = "领取推广奖励" )
    @PostMapping( "/receiveRecommendReward" )
    public RspBase<?> receiveRecommendReward() {
        return recommendService.receiveRecommendReward( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "佣金领取记录" )
    @PostMapping( "/getRecommendRewardDetailList" )
    public RspBase<List<RspDetailCommission>> getRecommendRewardDetailList( @RequestBody PageDomain pageDomain ) {
        startPage( pageDomain );
        List<RspDetailCommission> list = recommendService.getRecommendRewardDetailList( MemberSecurityUtils.getUserId() );
        return getRspBasePage( list, pageDomain );
    }

    @Operation( summary = "推广说明" )
    @PostMapping( "/getRecommendDesc" )
    @Anonymous
    public RspBase<List<ConfigRecommend>> getRecommendDesc() {
        return RspBase.ok( recommendService.getRecommendDesc() );
    }
}
