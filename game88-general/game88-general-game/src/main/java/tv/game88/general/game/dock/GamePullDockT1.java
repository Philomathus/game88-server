package tv.game88.general.game.dock;

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
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Log4j2
@Repository( value = ConstantsGame.T1 + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockT1 extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 4 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        Map<String, String> params = new TreeMap<>();
        params.put( "auth_token", getToken( gamePlatform ) );
        params.put( "merchant_code", gamePlatform.getAgent() );
        params.put( "from", LocalDateTimeUtils.format( start, LocalDateTimeUtils.LOCALTIME_SP_NOM_FORMATTER ) );
        params.put( "to", LocalDateTimeUtils.format( end, LocalDateTimeUtils.LOCALTIME_SP_NOM_FORMATTER ) );
        params.put( "time_type", "2" );
        params.put( "sign", generateSecureKey( params, gamePlatform.getDes() ) );

        String              url       = this.getURL( gamePlatform.getApiUrl(), "chain/query_game_history" );
        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( url, HttpMethod.GET, params );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            if ( e.getMessage().contains( "Invalid Auth Token" ) ) {
                redisUtils.unlink( Constants.GAME_TOKEN_PREX + gamePlatform.getId() );
            }
        }
        if ( !CollectionUtils.isEmpty( resultMap ) && isSuccessCode( resultMap ) ) {
            Map<String, Object> detail = ( Map<String, Object> ) resultMap.getOrDefault( "detail", Collections.emptyMap() );
            // 状态正常,无论是否有数据,从结束时间开始查询
            gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
            return ( List<Object> ) detail.getOrDefault( "game_history", Collections.EMPTY_LIST );
        }
        log.warn( JsonUtil.object2Json( resultMap ) );
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        if ( "1".equals( String.valueOf( remoteGameDatum.get( "status" ) ) ) ) {
            return null;
        }
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "uniqueid" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "period" ) ) );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "username" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_code" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        String        startTime      = remoteGameDatum.get( "bet_time" ).toString() + "000";
        LocalDateTime startTimeLocal = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( startTime ) );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( startTimeLocal ) );
        String        endTime      = remoteGameDatum.get( "payout_time" ).toString() + "000";
        LocalDateTime endTimeLocal = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( endTime ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( endTimeLocal ) );

        String betAmount = String.valueOf( remoteGameDatum.get( "bet_amount" ) );
        gameDataRecord.setCellScore( betAmount );
        gameDataRecord.setAllBet( betAmount );
        String payoutAmount = String.valueOf( remoteGameDatum.get( "payout_amount" ) );
        gameDataRecord.setProfit( new BigDecimal( payoutAmount ).subtract( new BigDecimal( betAmount ) ).toString() );
        return gameDataRecord;
    }

    public String getToken( GamePlatform gamePlatform ) {
        if ( !redisUtils.exists( Constants.GAME_TOKEN_PREX + gamePlatform.getId() ) ) {
            final Map<String, String> params = new TreeMap<>();
            params.put( "merchant_code", gamePlatform.getAgent() );
            params.put( "secure_key", gamePlatform.getMd5() );
            params.put( "sign", generateSecureKey( params, gamePlatform.getDes() ) );

            log.warn( JsonUtil.object2Json( params ) );

            String              url       = this.getURL( gamePlatform.getApiUrl(), "generate_token" );
            Map<String, Object> resultMap = execute( url, HttpMethod.POST, params );

            if ( !CollectionUtils.isEmpty( resultMap ) && isSuccessCode( resultMap ) ) {
                Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "detail", Collections.emptyMap() );
                if ( !data.isEmpty() ) {
                    String token   = data.getOrDefault( "auth_token", "" ).toString();
                    String timeout = data.getOrDefault( "timeout", "" ).toString();
                    if ( StringUtils.isBlank( token ) ) {
                        throw new BusinessException( "获取token失败" );
                    }
                    redisUtils.strSet( Constants.GAME_TOKEN_PREX + gamePlatform.getId(), token, Duration.ofSeconds(
                            Integer.parseInt( timeout ) - 600 ) );
                    return token;
                }
            }
        }
        return redisUtils.strGet( Constants.GAME_TOKEN_PREX + gamePlatform.getId() );
    }

    private Map<String, Object> execute( final String url, final HttpMethod method, final Map<String, String> params ) {
        HttpEntity<Map<String, String>> requestEntity = null;
        UriComponents                   uriComponents;
        if ( !HttpMethod.GET.equals( method ) ) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType( MediaType.APPLICATION_JSON );
            requestEntity = new HttpEntity<>( params, httpHeaders );
            uriComponents = UriComponentsBuilder.fromUriString( url ).build( true );
        } else {
            MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
            requestMap.setAll( params );
            uriComponents = UriComponentsBuilder.fromUriString( url ).queryParams( requestMap ).build( true );
        }
        log.warn( uriComponents.toUriString() );
        return restTemplate.execute( uriComponents.toUriString(), method, restTemplate.httpEntityCallback( requestEntity ),
                response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    private static String generateSecureKey( Map<String, ?> params, String des ) {
        StringBuilder sb = new StringBuilder();
        params.values().stream().filter( v -> v instanceof String && !( ( String ) v ).isEmpty() ).forEach( sb::append );
        String concat = sb.toString().concat( des );
        return DigestUtils.sha1Hex( concat );
    }

    private String getURL( final String apiURL, final String endpoint ) {
        return apiURL + "/gameapi/v2/" + endpoint;
    }

    private boolean isSuccessCode( final Map<String, Object> resultMap ) {
        final Object code = resultMap.get( "code" );
        return "0".equals( String.valueOf( code ) );
    }
}
