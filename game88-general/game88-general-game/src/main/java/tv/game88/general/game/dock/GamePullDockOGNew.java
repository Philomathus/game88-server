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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
@Repository( value = ConstantsGame.OG_NEW + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockOGNew extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        if ( StringUtils.isBlank( gamePlatform.getVersionValue() ) ) {
            return null;
        }

        Map<String, Integer> versionMap = JsonUtil.json2Map( gamePlatform.getVersionValue() );

        List<Callable<Map<String, Object>>> forkJoinTasks = new ArrayList<>();

        versionMap.forEach( ( key, value ) -> forkJoinTasks.add( () -> this.queryList( gamePlatform, Integer.parseInt( key ),
                value ) ) );

        List<Future<Map<String, Object>>> futures = null;
        try {
            futures = Executors.newVirtualThreadPerTaskExecutor().invokeAll( forkJoinTasks );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }

        List<Map<String, Object>> collect = futures.stream().map( t -> {
            try {
                return t.get();
            } catch ( Exception e ) {
                throw new IllegalStateException( e );
            }
        } ).filter( Objects::nonNull ).toList();

        List<Object> resultList = new ArrayList<>();

        for ( Map<String, Object> resultMap : collect ) {
            resultList.addAll( ( ArrayList ) resultMap.get( "records" ) );
            versionMap.put( resultMap.get( "gameTypeId" ).toString(), Integer.parseInt( resultMap.get( "fetchId" ).toString() ) );
        }

        gamePlatform.setVersionValue( JsonUtil.object2Json( versionMap ) );
        return resultList;
    }

    private Map<String, Object> queryList( GamePlatform gamePlatform, int gameTypeId, int fetchId ) {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add( "fetch_id", String.valueOf( fetchId ) );
        requestMap.add( "game_type_id", String.valueOf( gameTypeId ) );

        HttpHeaders headers = new HttpHeaders();
        headers.set( "key", gamePlatform.getDes() );
        headers.set( "operator-name", gamePlatform.getAgent() );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( headers );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(
                gamePlatform.getApiUrl() + "/api/v2/platform/transaction/history" ).queryParams( requestMap ).build( true );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
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
        }

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "S-100".equals( resultMap.getOrDefault( "rs_code", "" ).toString() ) ) {
                List<Map<String, Object>> records = ( List<Map<String, Object>> ) resultMap.getOrDefault( "records",
                        new ArrayList<>() );
                Integer lastFetchId = ( Integer ) resultMap.getOrDefault( "last_fetch_id", fetchId );
                if ( !CollectionUtils.isEmpty( records ) ) {
                    return Map.of( "records", records, "fetchId", lastFetchId, "gameTypeId", gameTypeId );
                }
            } else {
                log.error( gamePlatform.getName() + ":::" + uriComponents.toUriString() + ":::"
                        + JsonUtil.object2Json( resultMap ) );
            }
        }
        return Map.of( "records", new ArrayList<>(), "fetchId", fetchId, "gameTypeId", gameTypeId );
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        log.warn( JsonUtil.object2Json( object ) );
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        String              playerId        = String.valueOf( remoteGameDatum.get( "player_id" ) );
        if ( !playerId.startsWith( "77" ) && !playerId.startsWith( "88" ) && !playerId.startsWith( "99" ) ) {
            return null;
        }
        String[]       splitParam     = playerId.split( "_" );
        String         agent          = splitParam[ 1 ].toLowerCase();
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "transaction_id" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "round_id" ) ) );
        gameDataRecord.setAccount( agent + "_" + splitParam[ 2 ].toUpperCase() );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setCurrency( String.valueOf( remoteGameDatum.get( "currency" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_id" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "effective_amount" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "debit_amount" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "winlose_amount" ) ) );
        LocalDateTime gameStartTime = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong(
                remoteGameDatum.get( "debit_at" ) + "000" ) );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( gameStartTime ) );
        LocalDateTime gameEndTime = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong(
                remoteGameDatum.get( "credit_at" ) + "000" ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( gameEndTime ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
