package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.CQ9 + "GameProcessor" )
public class GameDockCQ9 extends AbstractGameDock {

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        String key = Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() + ":" + reqJoinGame.getGameMemberId();
        if ( redisUtils.exists( key ) ) {
            String token = redisUtils.strGet( key );
            reqJoinGame.setToken( token );
            // redisUtils.expire( key, Duration.ofDays( 1 ) );
        } else {
            String              url    = reqJoinGame.getApiUrl() + "/gameboy/player/login";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add( "account", reqJoinGame.getGameMemberId() );
            params.add( "password", reqJoinGame.getGameMemberId() + "XX123" );
            Map<String, Object> resultMap = execute( HttpMethod.POST, url, params, reqJoinGame.getMd5() );

            if ( isValid( resultMap ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                String              token   = dataMap.getOrDefault( "usertoken", "" ).toString();
                if ( StringUtils.isNotBlank( token ) ) {
                    reqJoinGame.setToken( token );
                    redisUtils.strSet( key, token, Duration.ofDays( 1 ) );
                    return;
                }
            }
            log.error( reqJoinGame.getGameCategory().getDes() + " 获取token失败 ->{}", JsonUtil.object2Json( resultMap ) );
            throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 获取token失败" );
        }
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        String              url    = reqJoinGame.getApiUrl() + "/gameboy/player";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "account", reqJoinGame.getGameMemberId() );
        params.add( "password", reqJoinGame.getGameMemberId() + "XX123" );
        Map<String, Object> resultMap = execute( HttpMethod.POST, url, params, reqJoinGame.getMd5() );
        log.warn( "CQ9创建玩家:" + JsonUtil.object2Json( resultMap ) );
        if ( isValid( resultMap ) ) {
            redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
            return;
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String              url    = reqJoinGame.getApiUrl() + "/gameboy/player/gamelink";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "usertoken", reqJoinGame.getToken() );
        params.add( "gamehall", "cq9" );
        params.add( "gamecode", reqJoinGame.getKindId() );
        params.add( "gameplat", "mobile" );
        params.add( "lang", "zh-cn" );
        Map<String, Object> resultMap = execute( HttpMethod.POST, url, params, reqJoinGame.getMd5() );
        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            reqJoinGame.setGameUrl( dataMap.getOrDefault( "url", "" ).toString() );
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + "/gameboy/player/deposit";
        transact( reqJoinGame, url, "上" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + "/gameboy/player/withdraw";
        transact( reqJoinGame, url, "下" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/gameboy/player/balance/%s", reqJoinGame.getApiUrl(), reqJoinGame.getGameMemberId() );
        log.info( "Query Balance: {}", url );
        Map<String, Object> resultMap = execute( HttpMethod.GET, url, null, reqJoinGame.getMd5() );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            return new BigDecimal( dataMap.getOrDefault( "balance", "0" ).toString() );
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/gameboy/transaction/record/%s", reqJoinGame.getApiUrl(), reqJoinGame.getOrderId() );
        log.info( "Query Transfer: {}", url );
        Map<String, Object> resultMap = execute( HttpMethod.GET, url, null, reqJoinGame.getMd5() );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            return reqJoinGame.getOrderId().equals( dataMap.getOrDefault( "mtcode", "" ).toString() ) && "success".equals( dataMap
                    .getOrDefault( "status", "" ).toString() );
        }
        throw new BusinessException( "查询结果为空,需要重试" );
    }

    private Map<String, Object> execute( HttpMethod method, String url, MultiValueMap<String, String> params, String token ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if ( method != HttpMethod.GET ) {
            httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        }
        httpHeaders.set( "Authorization", token );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, httpHeaders );

        return restTemplate.execute( url, method, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            log.warn( text );
            return JsonUtil.json2Map( text );
        } );
    }

    private static boolean isValid( Map<String, Object> resultMap ) {
        boolean result = false;
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> statusMap = ( Map<String, Object> ) resultMap.getOrDefault( "status", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() ) )
                    && !CollectionUtils.isEmpty( statusMap ) && "0".equals( statusMap.getOrDefault( "code", "" ) ) ) {
                result = true;
            }
        }
        return result;
    }

    private void transact( ReqJoinGame reqJoinGame, String url, String type ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "account", reqJoinGame.getGameMemberId() );
        params.add( "mtcode", reqJoinGame.getOrderId() );
        params.add( "amount", reqJoinGame.getTransferMoney().toString() );
        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( HttpMethod.POST, url, params, reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        log.info( reqJoinGame.getGameCategory().getDes() + type
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !isValid( resultMap ) ) {
            throw new GameTransferException(
                    reqJoinGame.getGameCategory().getDes() + type + "分异常 - " + type + "分失败或数据为空" );
        }
    }
}
