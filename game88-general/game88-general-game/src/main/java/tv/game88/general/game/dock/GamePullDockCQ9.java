package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.CQ9 + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockCQ9 extends AbstractGamePull {

    private static final String dateT_pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final String date_pattern  = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 5 ) ) ) {
            return null;
        }

        LocalDateTime end = start.plusMinutes( 1 );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "starttime", formatUTC_4( start ) );
        requestMap.set( "endtime", formatUTC_4( end ) );
        requestMap.set( "page", "1" );
        requestMap.set( "pagesize", "1000" );

        String url = gamePlatform.getApiUrl() + "/gameboy/order/view";

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( url ).queryParams( requestMap ).build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set( "Authorization", gamePlatform.getMd5() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( httpHeaders );

        log.warn( uriComponents.toUriString() );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> statusMap = ( Map<String, Object> ) resultMap.getOrDefault( "status", new HashMap<>() );
            String              code      = statusMap.getOrDefault( "code", "-1" ).toString();
            Map<String, Object> dataMap   = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( "0".equals( code ) || "8".equals( code ) ) {
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                if ( !CollectionUtils.isEmpty( dataMap ) ) {
                    return ( List<Object> ) dataMap.getOrDefault( "Data", new ArrayList<>() );
                }
            } else {
                log.error( uriComponents.toUriString() + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    private static String formatUTC_4( LocalDateTime localDateTime ) {
        SimpleDateFormat sdf = new SimpleDateFormat( dateT_pattern );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT-4" ) );
        return sdf.format( Date.from( localDateTime.atZone( ZoneId.systemDefault() ).toInstant() ) );
    }

    private static String parseUTC_4( String time ) {
        SimpleDateFormat sdf = new SimpleDateFormat( date_pattern );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT-4" ) );
        Date date = null;
        try {
            date = sdf.parse( time.substring( 0, 19 ) );
        } catch ( ParseException e ) {
            throw new RuntimeException( e );
        }
        return new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( date );
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;

        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "round" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "account" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gamecode" ) ) );
        BigDecimal bet = new BigDecimal( String.valueOf( remoteGameDatum.get( "bet" ) ) );
        gameDataRecord.setCellScore( bet.toString() );
        gameDataRecord.setAllBet( bet.toString() );
        BigDecimal win = new BigDecimal( String.valueOf( remoteGameDatum.get( "win" ) ) );
        gameDataRecord.setProfit( win.subtract( bet ).toString() );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "tableid" ) ) );
        String gameStartTime = String.valueOf( remoteGameDatum.get( "bettime" ) );
        gameDataRecord.setGameStartTime( parseUTC_4( gameStartTime ) );
        String gameEndTime = String.valueOf( remoteGameDatum.get( "endroundtime" ) );
        gameDataRecord.setGameEndTime( parseUTC_4( gameEndTime ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
