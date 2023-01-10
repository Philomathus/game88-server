package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.HG + "GameProcessor" )
public class GameButtHG extends AbstractGameButt {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        String              url    = String.format( "%s/api/game/%s/handle", reqJoinGame.getApiUrl(), reqJoinGame.getDes() );
        Map<String, String> params = new LinkedHashMap<>();
        params.put( "action", "register" );
        params.put( "merchant", reqJoinGame.getDes() );
        params.put( "agent", reqJoinGame.getAgent() );
        params.put( "userName", reqJoinGame.getGameMemberId() );
        params.put( "password", reqJoinGame.getGameMemberId() + "1234" );
        params.put( "ip", reqJoinGame.getIp() );
        String param = null;
        try {
            param = AESCoder.encryptDES3( JsonUtil.object2Json( params ), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( Map.of( "params", param ), httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.PUT,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
                return;
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/api/game/%s/handle", reqJoinGame.getApiUrl(), reqJoinGame.getDes() );

        Map<String, String> params = new LinkedHashMap<>();
        params.put( "action", "login" );
        params.put( "merchant", reqJoinGame.getDes() );
        params.put( "agent", reqJoinGame.getAgent() );
        params.put( "userName", reqJoinGame.getGameMemberId() );
        params.put( "password", reqJoinGame.getGameMemberId() + "1234" );
        params.put( "gameCode", reqJoinGame.getLinecode() );
        params.put( "ip", reqJoinGame.getIp() );
        String param = null;
        try {
            param = AESCoder.encryptDES3( JsonUtil.object2Json( params ), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( Map.of( "params", param ), httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.PUT,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                reqJoinGame.setGameUrl( result.getOrDefault( "url", "" ).toString() );
            }
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/api/game/%s/handle", reqJoinGame.getApiUrl(), reqJoinGame.getDes() );

        Map<String, String> params = new LinkedHashMap<>();
        params.put( "action", "deposit" );
        params.put( "merchant", reqJoinGame.getDes() );
        params.put( "agent", reqJoinGame.getAgent() );
        params.put( "userName", reqJoinGame.getGameMemberId() );
        params.put( "transactionNo", reqJoinGame.getOrderId() );
        params.put( "money", reqJoinGame.getTransferMoney().toString() );
        String param = null;
        try {
            param = AESCoder.encryptDES3( JsonUtil.object2Json( params ), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( Map.of( "params", param ), httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.PUT,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                if ( reqJoinGame.getOrderId().equals( result.get( "transactionNo" ) ) ) {
                    return;
                }
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/api/game/%s/handle", reqJoinGame.getApiUrl(), reqJoinGame.getDes() );

        Map<String, String> params = new LinkedHashMap<>();
        params.put( "action", "withdraw" );
        params.put( "merchant", reqJoinGame.getDes() );
        params.put( "agent", reqJoinGame.getAgent() );
        params.put( "userName", reqJoinGame.getGameMemberId() );
        params.put( "transactionNo", reqJoinGame.getOrderId() );
        params.put( "money", reqJoinGame.getTransferMoney().toString() );
        String param = null;
        try {
            param = AESCoder.encryptDES3( JsonUtil.object2Json( params ), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( Map.of( "params", param ), httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.PUT,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                if ( reqJoinGame.getOrderId().equals( result.get( "transactionNo" ) ) ) {
                    return;
                }
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/api/game/%s/handle", reqJoinGame.getApiUrl(), reqJoinGame.getDes() );

        Map<String, String> params = new LinkedHashMap<>();
        params.put( "action", "balance" );
        params.put( "merchant", reqJoinGame.getDes() );
        params.put( "agent", reqJoinGame.getAgent() );
        params.put( "userName", reqJoinGame.getGameMemberId() );
        String param = null;
        try {
            param = AESCoder.encryptDES3( JsonUtil.object2Json( params ), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( Map.of( "params", param ), httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.PUT,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                return new BigDecimal( result.getOrDefault( "money", "0" ).toString() );
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/api/game/%s/handle", reqJoinGame.getApiUrl(), reqJoinGame.getDes() );

        Map<String, String> params = new LinkedHashMap<>();
        params.put( "action", "check" );
        params.put( "merchant", reqJoinGame.getDes() );
        params.put( "agent", reqJoinGame.getAgent() );
        params.put( "type", reqJoinGame.getMoneyType() + "" );
        params.put( "transactionNo", reqJoinGame.getOrderId() );
        String param = null;
        try {
            param = AESCoder.encryptDES3( JsonUtil.object2Json( params ), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( Map.of( "params", param ), httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.PUT,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                return reqJoinGame.getOrderId().equals( result.get( "transactionNo" ) );
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
