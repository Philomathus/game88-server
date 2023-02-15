package tv.game88.game.app.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.game.api.dto.*;
import tv.game88.game.api.service.GameService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "游戏相关接口" )
@Log4j2
public class GameController extends BaseController {
    @Resource
    private GameService gameService;

    @Operation( summary = "获取游戏分类列表" )
    @PostMapping( "/getGameTypes" )
    @Anonymous
    public RspBase<RspGameTypes> getGameTypes( @RequestHeader( value = "version", required = false ) String version ) {
        return RspBase.ok( gameService.getGameTypes( version ) );
    }

    @Operation( summary = "根据类型获取游戏列表" )
    @PostMapping( "/getGameInfoList" )
    @Anonymous
    public RspBase<List<RspGameInfo>> getGameInfoList( @Validated @RequestBody ReqGame req ) {
        List<RspGameInfo> gameInfos = gameService.getGameInfoList( req.getId() );
        for ( RspGameInfo gameInfo : gameInfos ) {
            if ( StringUtils.isNotBlank( gameInfo.getIcon() ) && !gameInfo.getIcon().startsWith( "http" ) ) {
                gameInfo.setIcon( ConfigDomainCacheUtil.me.getDomainOssValue() + gameInfo.getIcon() );
            }
        }
        return RspBase.ok( gameInfos );
    }

    @Operation( summary = "根据类型获取游戏列表-新" )
    @PostMapping( "/getGameInfos" )
    @Anonymous
    public RspBase<List<RspGameInfo>> getGameInfos( @Validated @RequestBody ReqGameInfo req ) {
        List<RspGameInfo> gameInfos = gameService.getGameInfos( req.getId(), req.getPid() );
        for ( RspGameInfo gameInfo : gameInfos ) {
            if ( StringUtils.isNotBlank( gameInfo.getIcon() ) && !gameInfo.getIcon().startsWith( "http" ) ) {
                gameInfo.setIcon( ConfigDomainCacheUtil.me.getDomainOssValue() + gameInfo.getIcon() );
            }
        }
        return RspBase.ok( gameInfos );
    }

    @Operation( summary = "根据类型获取游戏分组" )
    @PostMapping( "/getGameInfoGroup" )
    @Anonymous
    public RspBase<List<RspGamePlatform>> getGameInfoGroup( @Validated @RequestBody ReqGame req ) {
        return gameService.getGameInfoGroup( req.getId() );
    }

    // 获取游戏token,内部接口
    @GetMapping( "/getGameToken" )
    @Hidden
    public RspBase<String> getGameToken( String agent, String gameCategory ) {
        return gameService.getGameTokenByAgent( agent, gameCategory );
    }

    @Operation( summary = "进入游戏" )
    @PostMapping( "/joinGame" )
    public RspBase<?> joinGame( @RequestHeader( value = "dev", required = false ) Integer dev,
                                @Validated @RequestBody ReqGame req ) {
        return gameService.joinGame( req.getId(), MemberSecurityUtils.getLoginUser().getPlatformUser(), dev );
    }

    @Operation( summary = "会员游戏下分" )
    @PostMapping( "/escGame" )
    public RspBase<?> escGame( @Validated @RequestBody ReqGame req ) {
        return gameService.escGame( req.getId(), MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "自主查询游戏余额" )
    @PostMapping( "/getGameBalance" )
    public RspBase<List<RspGameMoney>> getGameBalance() {
        return gameService.getGameBalance( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "自主游戏下分" )
    @PostMapping( "/gameWithdrawal" )
    public RspBase<?> gameWithdrawal( @Validated @RequestBody ReqGame req ) {
        return gameService.gameWithdrawal( req.getId(), MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "PG Verify Session" )
    @PostMapping( "/VerifySession" )
    public RspBase<?> verifySession( @RequestHeader( value = "trace_id" ) String traceId,
                                     @Validated @RequestBody ReqPGSoftGameData data ) {
        return gameService.verify( traceId, data );
    }
}
