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
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.CQ9 + "GamePullProcessor" )
public class GamePullDockCQ9 extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 5 ) ) ) {
            return null;
        }

        LocalDateTime end = start.plusMinutes( 1 );
        long startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long endTime = LocalDateTimeUtils.localDateToTimestamp( end );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "starttime", LocalDateTimeUtils.format( start, LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SS_XXXFORMATTER ) );
        requestMap.set( "endtime", LocalDateTimeUtils.format( end, LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SS_XXXFORMATTER ) );
        requestMap.set( "page", String.valueOf( 1 ) );

        String url = gamePlatform.getApiUrl() + "/gameboy/order/view?";

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( url )
                .queryParams( requestMap )
                .build( true );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set( "Authorization", gamePlatform.getMd5() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( null, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.GET, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            log.warn( text );
            return JsonUtil.json2Map( text );
        } );

        if ( ! CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> statusMap = ( Map<String, Object> ) resultMap.getOrDefault( "status", new
                    HashMap<>() );
            if ( "0".equals( statusMap.getOrDefault( "code", "-1" ).toString() ) ) {
                List<Object> dataMap = ( List<Object> ) resultMap.getOrDefault( "data", new
                        ArrayList<Map<>>() );
                if ( ! CollectionUtils.isEmpty( dataMap ) ) {
                    gamePlatform.setVersionValue( String.valueOf( endTime ) );
                    return dataMap;
                }
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;

        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gamehall" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "gamecode" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "account" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "round" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "bet" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "bet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "win" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "tableid" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "createtime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "endroundtime" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "gameplat" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
