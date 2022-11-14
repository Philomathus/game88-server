package tv.game88.platform.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.vo.RspBase;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.platform.api.dto.*;
import tv.game88.platform.api.service.ActivityService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "活动及任务相关接口" )
@Log4j2
public class ActivityController extends BaseController {
    @Resource
    private ActivityService activityService;

    @Operation( summary = "获取活动分类列表" )
    @PostMapping( "/getActivityTypes" )
    @Anonymous
    public RspBase<List<RspActivityType>> getActivityTypes( @RequestHeader( value = "token", required = false ) String token ) {
        return RspBase.ok( activityService.getActivityTypes( token ) );
    }

    @Operation( summary = "获取活动列表" )
    @PostMapping( "/getActivityInfos" )
    @Anonymous
    public RspBase<List<RspActivityInfo>> getActivityInfos( @RequestHeader( value = "token", required = false ) String token,
                                                            @Validated @RequestBody ReqActivityType req ) {
        return RspBase.ok( activityService.getActivityInfos( req.getId(), token ) );
    }

    @Operation( summary = "获取任务分类列表" )
    @PostMapping( "/getActivityQuestTypes" )
    public RspBase<List<RspQuestType>> getActivityQuestTypes() {
        return RspBase.ok( activityService.getActivityQuestTypes( MemberSecurityUtils.getUserId() ) );
    }

    @Operation( summary = "获取任务列表" )
    @PostMapping( "/getActivityQuestInfos" )
    public RspBase<List<RspQuestInfo>> getActivityQuestInfos( @Validated @RequestBody ReqActivityType req ) {
        return RspBase.ok( activityService.getActivityQuestInfos( req.getId(), MemberSecurityUtils.getUserId() ) );
    }

    @Operation( summary = "领取任务奖励" )
    @PostMapping( "/receiveQuestReward" )
    public RspBase<?> receiveQuestReward( @Validated @RequestBody ReqActivityType req ) {
        return activityService.receiveQuestReward( req.getId(), MemberSecurityUtils.getUserId() );
    }
}
