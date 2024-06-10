package tv.game88.game.app.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.game.api.dto.ReqPGSoftGameData;
import tv.game88.game.api.service.GameService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag( name = "游戏验证相关接口" )
@Hidden
@Log4j2
public class GameVerifySessionController {
    @Resource
    private GameService gameService;

    // 获取游戏token,内部接口
    @GetMapping( "/getGameToken" )
    @Hidden
    @Anonymous
    public RspBase<String> getGameToken( String agent, String gameCategory ) throws Exception {
        return gameService.getGameTokenByAgent( agent, gameCategory );
    }

    @Operation( summary = "PG Verify Session" )
    @PostMapping( value = "/VerifySession", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE } )
    @Hidden
    @Anonymous
    public String verifyPGSession( @RequestParam MultiValueMap<String, String> formData ) throws Exception {
        log.warn( "PG请求验证 - body:{}", JsonUtil.object2Json( formData ) );
        ReqPGSoftGameData reqPGSoftGameData = ReqPGSoftGameData.fromQueryString( formData );
        try {
            return gameService.verifyPGSession( reqPGSoftGameData );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            Map<String, Map<String, String>> resultMap = new HashMap<>();
            resultMap.put( "data", null );
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put( "code", "1034" );
            errorMap.put( "message", "operatorToken or secretKey is inconsistent with the server" );
            resultMap.put( "error", errorMap );
            return JsonUtil.object2Json( resultMap );
        }
    }
}
