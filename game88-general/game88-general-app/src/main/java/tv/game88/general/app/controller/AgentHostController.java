package tv.game88.general.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.vo.RspBase;
import tv.game88.general.api.dto.ReqAgent;
import tv.game88.general.api.dto.RspAgent;
import tv.game88.general.api.dto.RspHostClient;
import tv.game88.general.api.service.AgentHostClientService;
import tv.game88.general.api.service.AgentHostService;

import jakarta.annotation.Resource;


@Log4j2
@RestController
@RequestMapping( "/host" )
@Tag( name = "主播总代" )
public class AgentHostController {

    @Resource
    private AgentHostService agentHostService;

    @Resource
    private AgentHostClientService agentHostClientService;

    @RequestMapping( "agent" )
    @Operation( summary = "主播获取代理信息" )
    public RspBase<RspAgent> getAgentInfo( @RequestBody ReqAgent req ) {
        if ( StringUtils.isEmpty( req.getAgent() ) ) {
            log.error( "代理号null" );
            return RspBase.businessError( "请输入代理号" );
        }

        if ( req.getAgent().length() < 4 ) {
            log.error( "代理号输入格式不正确agent:{}", req.getAgent() );
            return RspBase.businessError( "邀请码不存在" );
        }

        try {
            RspAgent rspAgent = agentHostService.findAgentHost( req.getAgent() );
            if ( rspAgent == null ) {
                log.error( "代理信息错误req:{}", req.toString() );
                return RspBase.businessError( "邀请码信息错误" );
            }
            RspBase<RspAgent> rsp = new RspBase<>();
            rsp.setData( rspAgent );
            return rsp;
        } catch ( Exception e ) {
            log.error( "获取代理信息失败,请联系客服agent:{}", req.getAgent(), e );
            return RspBase.businessError( "获取信息失败,请联系客服" );
        }
    }

    @RequestMapping( "init" )
    @Operation( summary = "主播客户端初始化" )
    public RspBase<RspHostClient> init( @RequestHeader( "dev" ) Integer dev ) {
        if ( dev == null ) {
            log.error( "dev 为空" );
            return RspBase.businessError( "请求错误" );
        }

        try {
            RspHostClient client = agentHostClientService.findLatestHostClient( dev );
            if ( client == null ) {
                log.error( "暂无最新版本" );
                client = new RspHostClient();
            }
            RspBase<RspHostClient> rsp = new RspBase<>();
            rsp.setData( client );
            return rsp;
        } catch ( Exception e ) {
            log.error( "获取主播版本号失败,请联系客服dev:{}", dev, e );
            return RspBase.businessError( "获取最新版本失败,请联系客服" );
        }
    }
}
