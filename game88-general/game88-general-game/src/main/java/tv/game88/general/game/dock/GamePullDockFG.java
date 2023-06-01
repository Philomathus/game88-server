package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Log4j2
@Repository( value = ConstantsGame.FG + "GamePullProcessor" )
public class GamePullDockFG extends AbstractGamePull {

    private static final List<String> GT_TYPE_LIST = Arrays.asList( "hunter", "chess", "slot", "arcade" );

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 4 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        String startTime = String.valueOf( LocalDateTimeUtils.localDateToTimestamp( start ) / 1000L );
        String endTime   = String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) / 1000L );

        List<Callable<List<Map<String, Object>>>> forkJoinTasks = new ArrayList<>();
        for ( String gt : GT_TYPE_LIST ) {
            forkJoinTasks.add( () -> this.queryList( gamePlatform, gt, startTime, endTime ) );
        }
        List<Future<List<Map<String, Object>>>> futures = forkJoinPool.invokeAll( forkJoinTasks );
        List<List<Map<String, Object>>> collect = futures.stream().map( t -> {
            try {
                return t.get();
            } catch ( InterruptedException | ExecutionException e ) {
                throw new IllegalStateException( e );
            }
        } ).filter( Objects::nonNull ).toList();
        List<Object> resultList = new ArrayList<>();
        for ( List<Map<String, Object>> mapList : collect ) {
            resultList.addAll( mapList );
        }
        // 状态正常,无论是否有数据,从结束时间开始查询
        gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
        return resultList;
    }

    private List<Map<String, Object>> queryList( GamePlatform gamePlatform, String gt, String start, String end ) {
        String url = gamePlatform.getApiUrl() + "/v3_1/agent/log_by_page/gt/" + gt + "/start_time/" + start + "/end_time/" + end;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        httpHeaders.setAccept( List.of( MediaType.APPLICATION_JSON ) );
        httpHeaders.set( "merchantname", gamePlatform.getDes() );
        httpHeaders.set( "merchantcode", gamePlatform.getMd5() );

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( httpHeaders );

        log.warn( url );
        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              code    = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( "0".equals( code ) && !CollectionUtils.isEmpty( dataMap ) ) {
                return ( List<Map<String, Object>> ) dataMap.getOrDefault( "data", new ArrayList<>() );
            } else {
                log.warn( url + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "id" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );

        String account    = String.valueOf( remoteGameDatum.get( "player_name" ) ).toLowerCase();
        String agent  = account.split( "_" )[ 0 ];
        String createTime = remoteGameDatum.get( "time" ).toString();

        gameDataRecord.setGameRound( gameDataRecord.getGameId() ); //TODO: Not sure
        gameDataRecord.setAccount( account );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_id" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "all_bets" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "total_bets" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "result" ) ) );

        String timestamp = remoteGameDatum.get( "time" ) + "000";
        LocalDateTime time = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( timestamp ) );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( time ) );
        gameDataRecord.setGameEndTime( gameDataRecord.getGameStartTime() );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setGameAgent( String.valueOf( remoteGameDatum.get( "total_agent_uid" ) ) );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
