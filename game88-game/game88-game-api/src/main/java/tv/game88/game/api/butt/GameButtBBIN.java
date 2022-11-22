package tv.game88.game.api.butt;

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
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
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
public class GameButtBBIN extends AbstractGameButt {

    private static final String WEBSSITE           = "rtwrt1";
    private static final String createSession_key8 = "0P2McG5jG";
    private static final String transfer_key8      = "un5zaZl";
    private static final String joinGame_key8      = "7G7kc84PxB";
    private static final String query_balance_key8 = "5HdRJ";

    private String convertTime() {
        return LocalDateTimeUtils.format( LocalDate.now( ZoneId.of( "America/Caracas" ) ),
                LocalDateTimeUtils.YYYYMMDD_FORMATTER );
    }

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        String a   = RandomStringUtils.randomAlphabetic( 9 );
        String c   = RandomStringUtils.randomAlphabetic( 9 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + reqJoinGame.getGameMemberId() + createSession_key8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "username", reqJoinGame.getGameMemberId() );
        requestMap.set( "uppername", reqJoinGame.getAgent() );
        requestMap.set( "lang", "zh-cn" );
        requestMap.set( "ingress", "2" );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "CreateSession" )
                .queryParams( requestMap )
                .build();
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
            throw new BusinessException( "BBIN - 获取sessionId失败" );
        }
    }


    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        // 无需创建账号,getToken时已经创建了
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String a   = RandomStringUtils.randomAlphabetic( 4 );
        String c   = RandomStringUtils.randomAlphabetic( 1 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + reqJoinGame.getGameMemberId() + joinGame_key8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "lang", "zh-cn" );
        requestMap.set( "sessionid", reqJoinGame.getToken() );
        // TODO kindId
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + reqJoinGame.getRecordUrl() )
                .queryParams( requestMap )
                .build();
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
                String              html5   = dataMap.getOrDefault( "html5", "" ).toString();
                String              mobile  = dataMap.getOrDefault( "mobile", "" ).toString();
                reqJoinGame.setGameUrl( StringUtils.isBlank( mobile ) ? html5 : mobile );
            }
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( "BBIN获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        String a   = RandomStringUtils.randomAlphabetic( 9 );
        String c   = RandomStringUtils.randomAlphabetic( 1 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + reqJoinGame.getGameMemberId() + transfer_key8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "username", reqJoinGame.getGameMemberId() );
        requestMap.set( "uppername", reqJoinGame.getAgent() );
        requestMap.set( "remitno", reqJoinGame.getOrderId() );
        requestMap.set( "action", "IN" );
        requestMap.set( "remit", reqJoinGame.getTransferMoney().toString() );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "Transfer" )
                .queryParams( requestMap )
                .build();
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
            throw new GameTransferException( "BBIN上分失败" );
        }
        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap
                .getOrDefault( "result", "false" )
                .toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "11100".equals( dataMap.get( "Code" ) ) ) {
                return;
            }
        }
        throw new BusinessException( "BBIN上分失败" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String a   = RandomStringUtils.randomAlphabetic( 9 );
        String c   = RandomStringUtils.randomAlphabetic( 1 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + reqJoinGame.getGameMemberId() + transfer_key8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "username", reqJoinGame.getGameMemberId() );
        requestMap.set( "uppername", reqJoinGame.getAgent() );
        requestMap.set( "remitno", reqJoinGame.getOrderId() );
        requestMap.set( "action", "OUT" );
        requestMap.set( "remit", reqJoinGame.getTransferMoney().toString() );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "Transfer" )
                .queryParams( requestMap )
                .build();
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
            throw new GameTransferException( "BBIN下分失败" );
        }
        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap
                .getOrDefault( "result", "false" )
                .toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "11100".equals( dataMap.get( "Code" ) ) ) {
                return;
            }
        }
        throw new BusinessException( "BBIN下分失败" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String a   = RandomStringUtils.randomAlphabetic( 2 );
        String c   = RandomStringUtils.randomAlphabetic( 2 );
        String md5 = DigestUtils.md5Hex( WEBSSITE + reqJoinGame.getGameMemberId() + query_balance_key8 + convertTime() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", WEBSSITE );
        requestMap.set( "username", reqJoinGame.getGameMemberId() );
        requestMap.set( "uppername", reqJoinGame.getAgent() );
        requestMap.set( "key", a + md5 + c );
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() + "CheckUsrBalance" )
                .queryParams( requestMap )
                .build();
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
                return new BigDecimal( dataMap.get( "Balance" ).toString() );
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {

        return false;
    }
}
