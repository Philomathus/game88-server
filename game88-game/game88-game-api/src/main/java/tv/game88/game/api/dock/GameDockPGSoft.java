package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.PG_SOFT + "GameProcessor" )
@SuppressWarnings( "unchecked" )
public class GameDockPGSoft extends AbstractGameDock {
    private static final String CURRENCY = "RMB";

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        //ignore
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        String url = String.format( "%s/external/v3/Player/Create", reqJoinGame.getApiUrl() );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "operator_token", reqJoinGame.getDes() );
        params.add( "secret_key", reqJoinGame.getMd5() );
        params.add( "player_name", reqJoinGame.getGameMemberId() );
        params.add( "nickname", reqJoinGame.getGameMemberId() );
        params.add( "currency", CURRENCY );
        Map<String, Object> resultMap = execute( url, params );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "Create Account Result:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        // @formatter:off
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataMap  = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            Map<String, Object> errorMap = ( Map<String, Object> ) resultMap.getOrDefault( "error", Collections.emptyMap() );
            if ( ( dataMap != null && "1".equals( dataMap.getOrDefault( "action_result", "" ).toString() ) )
                    || ( errorMap != null && "1305".equals( errorMap.getOrDefault( "code", "" ).toString() ) ) ) {
                redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
                return;
            }
        }
        // @formatter:on
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put( "client_ip", reqJoinGame.getIp() );
        params.put( "url_type", "game-entry" );
        params.put( "path", URLEncoder.encode( "/" + reqJoinGame.getKindId() + "/index.html", StandardCharsets.UTF_8 ) );
        params.put( "operator_token", reqJoinGame.getDes() );
        Map<String, String> extraMap = new HashMap<>();
        extraMap.put( "btt", "1" );
        String token = reqJoinGame.getGameMemberId() + "-" + System.currentTimeMillis() + "-" + CURRENCY;
        try {
            extraMap.put( "ops", AESCoder.encryptByKey( token, AESCoder.secretKey ) );
        } catch ( Exception e ) {
            throw new BusinessException( e.getMessage() );
        }
        extraMap.put( "oc", "0" );
        extraMap.put( "iwk", "1" );
        extraMap.put( "l", "zh-CN" );
        params.put( "extra_args", URLEncoder.encode( assemblyUrl( extraMap ), StandardCharsets.UTF_8 ) );
        String body = assemblyUrl( params );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        httpHeaders.setCacheControl( "no-cache, no-store, must-revalidate" );
        HttpEntity<String> requestEntity = new HttpEntity<>( body, httpHeaders );

        String url = reqJoinGame.getApiUrl() + "/external-game-launcher/api/v1/GetLaunchURLHTML?trace_id=" + UUID.randomUUID();
        log.warn( url + " ::: " + body );

        String responseStr = restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ),
                response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return text;
        } );

        if ( StringUtils.isBlank( responseStr ) || !responseStr.contains( "CONNECTED SUCCESSFULLY" ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败 userId:{} - Response: {}", reqJoinGame.getGameMemberId(), responseStr );
            throw new BusinessException( "获取游戏链接失败" );
        }

        reqJoinGame.setGameUrl( responseStr );
    }

    @Override
    @Retryable( retryFor = Exception.class, noRetryFor = GameTransferException.class, backoff = @Backoff( delay = 1000 ),
            maxAttempts = 5 )
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/external/Cash/v3/TransferIn", reqJoinGame.getApiUrl() );
        transact( reqJoinGame, url, true );
    }

    @Override
    @Retryable( retryFor = Exception.class, noRetryFor = GameTransferException.class, backoff = @Backoff( delay = 1000 ),
            maxAttempts = 5 )
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/external/Cash/v3/TransferOut", reqJoinGame.getApiUrl() );
        transact( reqJoinGame, url, false );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        if ( reqJoinGame.getMoneyType() != null && reqJoinGame.getMoneyType() == 2 ) {
            this.sleep( 2 );
        }

        String url = String.format( "%s/external/Cash/v3/GetPlayerWallet", reqJoinGame.getApiUrl() );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "operator_token", reqJoinGame.getDes() );
        params.add( "secret_key", reqJoinGame.getMd5() );
        params.add( "player_name", reqJoinGame.getGameMemberId() );
        Map<String, Object> resultMap = execute( url, params );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "Query Balance result:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        Map<String, Object> dataMap = getDataMapIfValid( resultMap );
        if ( !CollectionUtils.isEmpty( dataMap ) ) {
            return new BigDecimal( dataMap.getOrDefault( "cashBalance", "0" ).toString() ).setScale( 2, RoundingMode.HALF_UP );
        } else {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + "上下分失败" );
    }

    private void transact( ReqJoinGame reqJoinGame, String url, final boolean isDeposit ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "operator_token", reqJoinGame.getDes() );
        params.add( "secret_key", reqJoinGame.getMd5() );
        params.add( "player_name", reqJoinGame.getGameMemberId() );
        params.add( "amount", reqJoinGame.getTransferMoney().setScale( 2, RoundingMode.DOWN ).toString() );
        params.add( "transfer_reference", reqJoinGame.getOrderId() );
        params.add( "currency", CURRENCY );

        Map<String, Object> resultMap = execute( url, params );

        final String action = isDeposit ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataMap  = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            Map<String, Object> errorMap = ( Map<String, Object> ) resultMap.getOrDefault( "error", Collections.emptyMap() );
            if ( dataMap != null && StringUtils.isNotBlank( dataMap.getOrDefault( "transactionId", "" ).toString() ) ) {
                return;
            }
            // 转账状态特殊处理
            if ( errorMap != null ) {
                this.sleep( 2 );
                throw new RuntimeException( errorMap.getOrDefault( "message", "" ).toString() );
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上下分失败" );
    }

    private static String assemblyUrl( Map<String, ?> bodyMap ) {
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }

    private Map<String, Object> execute( String url, MultiValueMap<String, String> params ) {
        String urlAndGUID = url + "?trace_id=" + UUID.randomUUID();

        log.warn( urlAndGUID + ":::" + JsonUtil.object2Json( params ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, httpHeaders );
        return restTemplate.execute( urlAndGUID, HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    private Map<String, Object> getDataMapIfValid( Map<String, Object> resultMap ) {
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataMap  = ( Map<String, Object> ) resultMap.get( "data" );
            Map<String, Object> errorMap = ( Map<String, Object> ) resultMap.get( "error" );
            if ( CollectionUtils.isEmpty( errorMap ) && !CollectionUtils.isEmpty( dataMap ) ) {
                return dataMap;
            }
        }
        return null;
    }
}
