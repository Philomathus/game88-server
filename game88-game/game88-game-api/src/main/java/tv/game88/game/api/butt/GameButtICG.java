package tv.game88.game.api.butt;

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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
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
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.ICG + "GameProcessor" )
public class GameButtICG extends AbstractGameButt {

    private static final String LOGIN              = "/login";
    private static final String COMMON_URL         = "/api/v1/players";
    private static final String DEPOSIT            = "deposit";
    private static final String WITHDRAW           = "withdraw";
    private static final String GAME               = "/api/v1/games";
    private static final String TRANSACTION_RECORD = "/api/v1/profile/transactions";

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        if ( !redisUtils.exists( Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() ) ) {
            Map<String, String> map = new HashMap<>();
            map.put( "username", reqJoinGame.getAgent() );
            map.put( "password", reqJoinGame.getDes() );

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType( MediaType.APPLICATION_JSON );
            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( map, httpHeaders );

            Map<String, Object> resultMap = restTemplate.postForObject( reqJoinGame.getApiUrl() + LOGIN, httpEntity, Map.class );
            String              token     = resultMap.get( "token" ) == null ? null : resultMap.get( "token" ).toString();
            if ( StringUtils.isBlank( token ) ) {
                throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 获取token失败" );
            }
            reqJoinGame.setToken( token );
            redisUtils.strSet( Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId(), token, Duration.ofDays( 80 ) );
        } else {
            String token = redisUtils.strGet( Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() );
            reqJoinGame.setToken( token );
        }
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put( "username", reqJoinGame.getGameMemberId() );
        map.put( "nickname", reqJoinGame.getGameMemberId() );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( map, httpHeaders );
        Map<String, Object>             resultMap  = null;
        try {
            resultMap = restTemplate.postForObject( reqJoinGame.getApiUrl() + COMMON_URL, httpEntity, Map.class );
        } catch ( Exception e ) {
            if ( !e.getMessage().contains( "username already exists" ) ) {
                log.error( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败 - 失败原因:" + e.getMessage(), e );
                throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
            }
        }
        if ( resultMap.get( "data" ) == null ) {
            log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
            throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
        }
        redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        HttpEntity<?> httpEntity = new HttpEntity<>( httpHeaders );
        String        url;
        if ( Arrays.asList( "all", "fish", "slot", "coc" ).contains( reqJoinGame.getKindId() ) ) {
            url = reqJoinGame.getApiUrl() + GAME + "?type=" + reqJoinGame.getKindId() + "&lang=zh";
        } else {
            url = reqJoinGame.getApiUrl() + "/api/v1/games/gamelink?productId=" + reqJoinGame.getKindId() + "&player="
                    + reqJoinGame.getGameMemberId() + "&lang=zh";
        }

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.GET,
                restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }

            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( resultMap.get( "data" ) instanceof List ) {
                List<Map<String, Object>> list = ( List<Map<String, Object>> ) resultMap.get( "data" );
                if ( !CollectionUtils.isEmpty( list ) ) {
                    Map<String, Object> dataMap = list.get( 0 );
                    reqJoinGame.setGameUrl( dataMap.getOrDefault( "href", "" ).toString() );
                }
            } else {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
                reqJoinGame.setGameUrl( dataMap.getOrDefault( "url", "" ).toString() );
            }
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}; url:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId(), url );
            throw new BusinessException( "获取游戏链接失败" );
        }
        reqJoinGame.setGameUrl( reqJoinGame.getGameUrl() + "&token=ICG".concat( reqJoinGame.getGameMemberId() ) );
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        Map<String, Object> map = new HashMap<>();
        map.put( "transactionId", reqJoinGame.getOrderId() );
        map.put( "amount", reqJoinGame.getTransferMoney().multiply( new BigDecimal( 100 ) ) );
        map.put( "player", reqJoinGame.getGameMemberId() );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( map, httpHeaders );
        Map<String, Object>             resultMap  = null;
        try {
            resultMap = restTemplate.postForObject( reqJoinGame.getApiUrl() + COMMON_URL + "/" + DEPOSIT, httpEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( CollectionUtils.isEmpty( resultMap ) || !resultMap.containsKey( "data" ) ) {
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
        }
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        Map<String, Object> map = new HashMap<>();
        map.put( "transactionId", reqJoinGame.getOrderId() );
        map.put( "amount", reqJoinGame.getTransferMoney().multiply( new BigDecimal( 100 ) ) );
        map.put( "player", reqJoinGame.getGameMemberId() );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( map, httpHeaders );
        Map<String, Object>             resultMap  = null;
        try {
            resultMap = restTemplate.postForObject(
                    reqJoinGame.getApiUrl() + COMMON_URL + "/" + WITHDRAW, httpEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( CollectionUtils.isEmpty( resultMap ) || !resultMap.containsKey( "data" ) ) {
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
        }
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        HttpEntity<?> httpEntity = new HttpEntity<>( httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl() + COMMON_URL + "?player="
                + reqJoinGame.getGameMemberId(), HttpMethod.GET, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            List<Map<String, Object>> list = ( List<Map<String, Object>> ) resultMap.get( "data" );
            if ( !CollectionUtils.isEmpty( list ) ) {
                Map<String, Object> dataMap = list.get( 0 );
                BigDecimal          coin    = new BigDecimal( dataMap.getOrDefault( "balance", "0" ).toString() );
                return coin.divide( new BigDecimal( 100 ), 2, RoundingMode.HALF_UP );
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "player", reqJoinGame.getGameMemberId() );
        requestMap.set( "id", reqJoinGame.getOrderId() );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        HttpEntity<?> httpEntity = new HttpEntity<>( httpHeaders );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( reqJoinGame.getApiUrl() + TRANSACTION_RECORD )
                                                          .queryParams( requestMap ).build();

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            List<Map<String, Object>> dataList = ( List<Map<String, Object>> ) resultMap.getOrDefault( "data",
                    new ArrayList<>() );
            if ( !CollectionUtils.isEmpty( dataList ) ) {
                Map<String, Object> recordMap = dataList.get( 0 );
                String              id        = recordMap.getOrDefault( "id", "" ).toString();
                BigDecimal          amount    = new BigDecimal( recordMap.getOrDefault( "amount", 0 ).toString() );
                if ( id.equals( reqJoinGame.getOrderId() ) && amount.compareTo( reqJoinGame.getTransferMoney() ) == 0 ) {
                    return true;
                }
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
