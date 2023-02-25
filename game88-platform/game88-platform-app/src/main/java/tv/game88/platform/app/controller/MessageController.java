package tv.game88.platform.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.platform.api.dto.RspMessageCommonProblem;
import tv.game88.platform.api.dto.RspMessageHomeNotice;
import tv.game88.platform.api.dto.RspMessageOnSite;
import tv.game88.platform.api.service.MessageService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "消息公告相关接口" )
@Log4j2
public class MessageController extends BaseController {
    @Resource
    private MessageService     messageService;
    @Resource
    private ConfigEnvCacheUtil configEnvCacheUtil;

    @Operation( summary = "获取首页公告列表" )
    @PostMapping( "/getMessageHomeNotices" )
    @Anonymous
    public RspBase<List<RspMessageHomeNotice>> getHomeNotices() {
        RspBase<List<RspMessageHomeNotice>> ok = RspBase.ok( messageService.getMessageHomeNotices() );
        ok.setOtherData( configEnvCacheUtil.getConf( "trumpet_notice" ) );
        return ok;
    }

    @Operation( summary = "常见问题列表" )
    @PostMapping( value = "/getMessageCommonProblems" )
    @Anonymous
    public RspBase<List<RspMessageCommonProblem>> getCommonProblems() {
        return RspBase.ok( messageService.getMessageCommonProblems() );
    }

    @Operation( summary = "获取站内信息列表" )
    @PostMapping( "/getMessageOnSites" )
    @Anonymous
    public RspBase<List<RspMessageOnSite>> getOnSiteMessages() {
        String userId = MemberSecurityUtils.getUserId();
        return RspBase.ok( messageService.getMessageOnSites(userId) );
    }
}
