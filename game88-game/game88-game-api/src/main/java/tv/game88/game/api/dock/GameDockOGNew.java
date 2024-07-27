package tv.game88.game.api.dock;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
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
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.OG_NEW + "GameProcessor" )
public class GameDockOGNew extends AbstractGameDock {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        Map<String, Object> params = new TreeMap<>();
        params.put( "player_id", reqJoinGame.getGameMemberId() );
        params.put( "nickname", reqJoinGame.getGameMemberId() );
        params.put( "timestamp", System.currentTimeMillis() / 1000 );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set( "key", reqJoinGame.getDes() );
        headers.set( "operator-name", reqJoinGame.getAgent() );
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>( params, headers );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/api/v2/platform/transfer-wallet/register", HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = resultMap.getOrDefault( "rs_code", "" ).toString();
            if ( "S-100".equals( status ) || "S-121".equals( status ) ) {
                redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
                return;
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        Map<String, String> params = new TreeMap<>();
        params.put( "player_id", reqJoinGame.getGameMemberId() );
        params.put( "nickname", reqJoinGame.getGameMemberId() );
        params.put( "timestamp", String.valueOf( System.currentTimeMillis() / 1000 ) );
        params.put( "lang", "zh" );
        params.put( "token", IdWorker.get32UUID() + reqJoinGame.getMemberId() );
        params.put( "game_id", reqJoinGame.getKindId() );
        params.put( "betlimit", "317" );

        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign = DigestUtils.md5Hex( sb.substring( sb.length() - 1 ) + reqJoinGame.getMd5() );
        params.put( "signature", sign );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( params );
        HttpHeaders headers = new HttpHeaders();
        headers.set( "key", reqJoinGame.getDes() );
        headers.set( "operator-name", reqJoinGame.getAgent() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( headers );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(
                reqJoinGame.getApiUrl() + "/api/v2/platform/games/launch" ).queryParams( requestMap ).build( true );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) && "S-100".equals( resultMap.get( "rs_code" ).toString() ) ) {
            reqJoinGame.setGameUrl( resultMap.getOrDefault( "game_link", "" ).toString() );
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        Map<String, String> params = new TreeMap<>();
        params.put( "player_id", reqJoinGame.getGameMemberId() );
        params.put( "transaction_id", reqJoinGame.getOrderId() );
        params.put( "transfer_amount", reqJoinGame.getTransferMoney().setScale( 2, RoundingMode.DOWN ).toString() );
        params.put( "timestamp", String.valueOf( System.currentTimeMillis() / 1000 ) );

        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign = DigestUtils.md5Hex( sb.substring( sb.length() - 1 ) + reqJoinGame.getMd5() );
        params.put( "signature", sign );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set( "key", reqJoinGame.getDes() );
        headers.set( "operator-name", reqJoinGame.getAgent() );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( params, headers );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                    + "/api/v2/platform/transfer-wallet/deposit", HttpMethod.POST,
                    restTemplate.httpEntityCallback( requestEntity ), response -> {
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

        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "S-100".equals( resultMap.get( "rs_code" ).toString() ) ) {
            return;
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        Map<String, String> params = new TreeMap<>();
        params.put( "player_id", reqJoinGame.getGameMemberId() );
        params.put( "transaction_id", reqJoinGame.getOrderId() );
        params.put( "transfer_amount", reqJoinGame.getTransferMoney().setScale( 2, RoundingMode.DOWN ).toString() );
        params.put( "timestamp", String.valueOf( System.currentTimeMillis() / 1000 ) );

        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign = DigestUtils.md5Hex( sb.substring( sb.length() - 1 ) + reqJoinGame.getMd5() );
        params.put( "signature", sign );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set( "key", reqJoinGame.getDes() );
        headers.set( "operator-name", reqJoinGame.getAgent() );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( params, headers );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                    + "/api/v2/platform/transfer-wallet/withdraw", HttpMethod.POST,
                    restTemplate.httpEntityCallback( requestEntity ), response -> {
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

        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "S-100".equals( resultMap.get( "rs_code" ).toString() ) ) {
            return;
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add( "player_id", reqJoinGame.getGameMemberId() );

        HttpHeaders headers = new HttpHeaders();
        headers.set( "key", reqJoinGame.getDes() );
        headers.set( "operator-name", reqJoinGame.getAgent() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( headers );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(
                        reqJoinGame.getApiUrl() + "/api/v2/platform/transfer-wallet/get-balance" ).queryParams( requestMap )
                .build( true );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
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
        if ( !CollectionUtils.isEmpty( resultMap ) && "S-100".equals( resultMap.get( "rs_code" ).toString() ) ) {
            return new BigDecimal( resultMap.getOrDefault( "current_balance", "0" ).toString() );
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add( "player_id", reqJoinGame.getGameMemberId() );
        requestMap.add( "transaction_id", reqJoinGame.getOrderId() );

        HttpHeaders headers = new HttpHeaders();
        headers.set( "key", reqJoinGame.getDes() );
        headers.set( "operator-name", reqJoinGame.getAgent() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( headers );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(
                        reqJoinGame.getApiUrl() + "/api/v2/platform/transaction/transfer-history" ).queryParams( requestMap )
                .build( true );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
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
        if ( !CollectionUtils.isEmpty( resultMap ) && "S-100".equals( resultMap.getOrDefault( "rs_code", "" ).toString() ) ) {
            List<Map<String, Object>> records = ( List<Map<String, Object>> ) resultMap.getOrDefault( "records",
                    new ArrayList<>() );
            if ( !CollectionUtils.isEmpty( records ) && !CollectionUtils.isEmpty( records.getFirst() ) ) {
                Map<String, Object> recordsFirst = records.getFirst();
                return reqJoinGame.getOrderId().equals( recordsFirst.get( "transaction_id" ) ) && reqJoinGame.getGameMemberId()
                        .equals( recordsFirst.get( "player_id" ) );
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
