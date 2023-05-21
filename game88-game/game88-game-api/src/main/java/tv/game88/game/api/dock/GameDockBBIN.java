package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.BBIN + "GameProcessor" )
public class GameDockBBIN extends AbstractGameDock {

    private static final String WEBSSITE            = "rtwrt1";
    private static final String CREATE_SESSION_KEY8 = "0P2McG5jG";
    private static final String TRANSFER_KEY8       = "un5zaZl";
    private static final String JOIN_GAME_KEY8      = "7G7kc84PxB";
    private static final String QUERY_BALANCE_KEY8  = "5HdRJ";
    private static final String CHECK_TRANSFER_KEY8 = "e1Fg2";

    private String convertTime() {
        return LocalDateTimeUtils.format( LocalDate.now( ZoneId.of( "America/Caracas" ) ),
                LocalDateTimeUtils.YYYYMMDD_FORMATTER );
    }

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        String a   = RandomStringUtils.randomAlphabetic( 9 );
        String c   = RandomStringUtils.randomAlphabetic( 9 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + reqJoinGame.getGameMemberId() + CREATE_SESSION_KEY8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "username", reqJoinGame.getGameMemberId() );
        requestMap.set( "uppername", reqJoinGame.getAgent() );
        requestMap.set( "lang", "zh-cn" );
        requestMap.set( "ingress", "2" );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "/CreateSession" )
                .queryParams( requestMap )
                .build( true );
        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap
                .getOrDefault( "result", "false" )
                .toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            reqJoinGame.setToken( dataMap.getOrDefault( "sessionid", "" ).toString() );
        }
        if ( StringUtils.isBlank( reqJoinGame.getToken() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes() + " 获取token失败 ->{}", JsonUtil.object2Json( resultMap ) );
            throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 获取sessionId失败" );
        }
    }


    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        // 无需创建账号,getToken时已经创建了
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String gameUrlId   = null;
        String gameUrlKind = null;
        if ( reqJoinGame.getKindId().contains( "-" ) ) {
            gameUrlId   = reqJoinGame.getKindId().split( "-" )[ 0 ];
            gameUrlKind = reqJoinGame.getKindId().split( "-" )[ 1 ];
        }

        String a   = RandomStringUtils.randomAlphabetic( 4 );
        String c   = RandomStringUtils.randomAlphabetic( 1 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + JOIN_GAME_KEY8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "lang", "zh-cn" );
        requestMap.set( "sessionid", reqJoinGame.getToken() );
        if ( StringUtils.isNotBlank( gameUrlId ) ) {
            requestMap.set( "exit_option", "3" );
            if ( StringUtils.isNotBlank( gameUrlKind ) && !gameUrlKind.equals( "0" ) ) {
                requestMap.set( "gametype", gameUrlKind );
            }
        } else {
            requestMap.set( "active_site", reqJoinGame.getKindId() );
        }
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + ( StringUtils.isNotBlank( gameUrlId ) ?
                        "/GameUrlBy" + gameUrlId : "/LobbyUrl" ) )
                .queryParams( requestMap )
                .build( true );
        log.warn( uriComponents.toUri().toString() );
        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap
                .getOrDefault( "result", "false" )
                .toString() ) ) {
            List<Map<String, Object>> dataList = ( List<Map<String, Object>> ) resultMap.get( "data" );
            if ( !CollectionUtils.isEmpty( dataList ) ) {
                Map<String, Object> dataMap = dataList.get( 0 );
                String              mobile  = dataMap.getOrDefault( "mobile", "" ).toString();
                String              html5   = dataMap.getOrDefault( "html5", "" ).toString();
                reqJoinGame.setGameUrl( StringUtils.isBlank( mobile ) ? html5 : mobile );
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
        String a = RandomStringUtils.randomAlphabetic( 9 );
        String c = RandomStringUtils.randomAlphabetic( 1 );
        String md5 = DigestUtils.md5Hex(
                WEBSSITE + reqJoinGame.getGameMemberId() + reqJoinGame.getOrderId() + TRANSFER_KEY8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "username", reqJoinGame.getGameMemberId() );
        requestMap.set( "uppername", reqJoinGame.getAgent() );
        requestMap.set( "remitno", reqJoinGame.getOrderId() );
        requestMap.set( "action", "IN" );
        requestMap.set( "remit", reqJoinGame.getTransferMoney().toString() );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "/Transfer" )
                .queryParams( requestMap )
                .build( true );
        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null ),
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
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分失败" );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap
                .getOrDefault( "result", "false" )
                .toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "11100".equals( dataMap.get( "Code" ) ) ) {
                return;
            }
        }
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + "上分失败" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String a = RandomStringUtils.randomAlphabetic( 9 );
        String c = RandomStringUtils.randomAlphabetic( 1 );
        String md5 = DigestUtils.md5Hex(
                WEBSSITE + reqJoinGame.getGameMemberId() + reqJoinGame.getOrderId() + TRANSFER_KEY8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "username", reqJoinGame.getGameMemberId() );
        requestMap.set( "uppername", reqJoinGame.getAgent() );
        requestMap.set( "remitno", reqJoinGame.getOrderId() );
        requestMap.set( "action", "OUT" );
        requestMap.set( "remit", reqJoinGame.getTransferMoney().toString() );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "/Transfer" )
                .queryParams( requestMap )
                .build( true );
        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null ),
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
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分失败" );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap
                .getOrDefault( "result", "false" )
                .toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "11100".equals( dataMap.get( "Code" ) ) ) {
                return;
            }
        }
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + "下分失败" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String a   = RandomStringUtils.randomAlphabetic( 2 );
        String c   = RandomStringUtils.randomAlphabetic( 2 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + reqJoinGame.getGameMemberId() + QUERY_BALANCE_KEY8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "username", reqJoinGame.getGameMemberId() );
        requestMap.set( "uppername", reqJoinGame.getAgent() );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "/CheckUsrBalance" )
                .queryParams( requestMap )
                .build( true );
        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap
                .getOrDefault( "result", "false" )
                .toString() ) ) {
            List<Map<String, Object>> dataList = ( List<Map<String, Object>> ) resultMap.get( "data" );
            if ( !CollectionUtils.isEmpty( dataList ) ) {
                Map<String, Object> dataMap = dataList.get( 0 );
                return new BigDecimal( dataMap.get( "Balance" ).toString() );
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String a   = RandomStringUtils.randomAlphabetic( 4 );
        String c   = RandomStringUtils.randomAlphabetic( 9 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + CHECK_TRANSFER_KEY8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "transid", reqJoinGame.getOrderId() );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "/CheckTransfer" )
                .queryParams( requestMap )
                .build( true );
        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap
                .getOrDefault( "result", "false" )
                .toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            String              status  = dataMap.getOrDefault( "Status", 99 ).toString();
            return "1".equals( status ) || "-2".equals( status );
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
