package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
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
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.OG + "GameProcessor" )
public class GameButtOG extends AbstractGameButt {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        if ( !redisUtils.exists( Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() ) ) {
            HttpHeaders headers = new HttpHeaders();
            headers.set( "X-Operator", reqJoinGame.getDes() );
            headers.set( "x-key", reqJoinGame.getMd5() );
            headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( headers );

            Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                    + "/token", HttpMethod.GET, restTemplate.httpEntityCallback( requestEntity ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
            if ( !CollectionUtils.isEmpty( resultMap ) ) {
                String              status  = resultMap.getOrDefault( "status", "" ).toString();
                Map<String, String> dataMap = ( Map<String, String> ) resultMap.getOrDefault( "data", new HashMap<>() );
                if ( "success".equals( status ) && CollectionUtils.isEmpty( dataMap ) && dataMap.containsKey( "token" ) ) {
                    reqJoinGame.setToken( dataMap.get( "token" ) );
                    redisUtils.strSet( Constants.GAME_TOKEN_PREX
                            + reqJoinGame.getPlatformId(), reqJoinGame.getToken(), Duration.ofMinutes( 29 ) );
                }
            }
        } else {
            String token = redisUtils.strGet( Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() );
            reqJoinGame.setToken( token );
        }

        if ( StringUtils.isBlank( reqJoinGame.getToken() ) ) {
            throw new BusinessException( "OG - 获取token失败" );
        }
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "username", reqJoinGame.getGameMemberId() );
        params.add( "country", "China" );
        params.add( "fullname", reqJoinGame.getGameMemberId() );
        params.add( "email", "12345678@qq.com" );
        params.add( "language", "cn" );
        params.add( "birthdate", "2000-01-01" );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        headers.set( "X-Token", reqJoinGame.getToken() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/register", HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.info( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = resultMap.getOrDefault( "status", "" ).toString();
            if ( "success".equals( status ) ) {
                redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
            }
        }
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "username", reqJoinGame.getGameMemberId() );
        params.add( "balance", reqJoinGame.getTransferMoney().toString() );
        params.add( "action", "in" );
        params.add( "transferId", reqJoinGame.getOrderId() );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        headers.set( "X-Token", reqJoinGame.getToken() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                    + "/game-providers/30/balance", HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ),
                    response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = resultMap.getOrDefault( "status", "" ).toString();
            if ( "success".equals( status ) ) {
                return;
            }
        }
        throw new GameTransferException( "OG上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        return null;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "username", reqJoinGame.getGameMemberId() );
        params.add( "transferId", reqJoinGame.getOrderId() );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        headers.set( "X-Token", reqJoinGame.getToken() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/game-providers/30/confirm-transfer", HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ),
                response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = resultMap.getOrDefault( "status", "" ).toString();
            return "success".equals( status );
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
