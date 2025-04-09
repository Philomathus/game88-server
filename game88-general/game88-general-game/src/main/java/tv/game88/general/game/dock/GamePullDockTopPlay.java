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
import java.util.stream.Collectors;

@Log4j2
@Repository( value = ConstantsGame.TOP_PLAY + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockTopPlay extends AbstractGamePull {

    private static final Map<String, BigDecimal> RATE_MAP = Map.of( "KVND", new BigDecimal( 1000 ) );

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 5 ) ) ) {
            return null;
        }

        LocalDateTime end = start.plusMinutes( 1 );

        Map<String, String> params = new TreeMap<>();
        params.put( "gamehall", gamePlatform.getLinecode() );
        params.put( "start_time", LocalDateTimeUtils.format( LocalDateTimeUtils.convertToMeiDong( start ) ) );
        params.put( "end_time", LocalDateTimeUtils.format( LocalDateTimeUtils.convertToMeiDong( end ) ) );
        params.put( "page", "1" );
        params.put( "page_size", "5000" );

        String body = params.keySet().stream().map( key -> key + "=" + params.get( key ) ).collect( Collectors.joining( "&" ) )
                .concat( gamePlatform.getDes() );
        params.put( "sign", DigestUtils.md5Hex( body ) );


        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( params );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set( "Authorization", gamePlatform.getMd5() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( httpHeaders );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( gamePlatform.getRecordUrl() + "/api/betlog" )
                .queryParams( requestMap ).build();

        String uriString = uriComponents.toUriString();

        Map<String, Object> resultMap = restTemplate.execute( uriString, HttpMethod.GET,
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
            if ( "0".equals( statusMap.getOrDefault( "code", "-1" ).toString() ) ) {
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( dataMap ) ) {
                    return ( List<Object> ) dataMap.getOrDefault( "page_result", new ArrayList<>() );
                }
            } else {
                log.error( uriString + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        if ( "0".equals( remoteGameDatum.getOrDefault( "status", "0" ).toString() ) ) {
            return null;
        }
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "rowid" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "casino_account" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameid" ) ) );

        String     currency = gamePlatform.getAgent().split( "-" )[ 1 ];
        BigDecimal rate     = RATE_MAP.get( currency );

        BigDecimal betvalid = new BigDecimal( String.valueOf( remoteGameDatum.get( "betvalid" ) ) ).multiply( rate );
        gameDataRecord.setCellScore( betvalid.stripTrailingZeros().toPlainString() );
        BigDecimal betamount = new BigDecimal( String.valueOf( remoteGameDatum.get( "betamount" ) ) ).multiply( rate );
        gameDataRecord.setAllBet( betamount.stripTrailingZeros().toPlainString() );
        BigDecimal betresult = new BigDecimal( String.valueOf( remoteGameDatum.get( "betresult" ) ) ).multiply( rate );
        gameDataRecord.setProfit( betresult.stripTrailingZeros().toPlainString() );
        String gameStartTime = String.valueOf( remoteGameDatum.get( "bettime" ) );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( LocalDateTimeUtils.convertMeiDongToDefault( gameStartTime ) ) );
        String gameEndTime = String.valueOf( remoteGameDatum.get( "payout_time" ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( LocalDateTimeUtils.convertMeiDongToDefault( gameEndTime ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        gameDataRecord.setCurrency( currency );
        return gameDataRecord;
    }
}
