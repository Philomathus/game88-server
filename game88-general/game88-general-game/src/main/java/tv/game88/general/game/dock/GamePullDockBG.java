package tv.game88.general.game.dock;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
@Repository( value = ConstantsGame.BG + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockBG extends AbstractGamePull {
    // 443 棋牌 105 捕鱼 411 西游捕鱼 484 大仙捕鱼
    private static final String[] GAME_TYPE_LIST = { "443" };

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        String startTime = LocalDateTimeUtils.format( start );
        String endTime   = LocalDateTimeUtils.format( end );

        List<Callable<List<Map<String, Object>>>> forkJoinTasks = new ArrayList<>();
        for ( String gameType : GAME_TYPE_LIST ) {
            forkJoinTasks.add( () -> queryList( gamePlatform, startTime, endTime, gameType ) );
        }
        List<Future<List<Map<String, Object>>>> futures = null;
        try {
            futures = Executors.newVirtualThreadPerTaskExecutor().invokeAll( forkJoinTasks );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
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

    private List<Map<String, Object>> queryList( GamePlatform gamePlatform, String startTime, String endTime, String gameType ) {
        String method = "open.order.bg.query";
        String id     = IdWorker.get32UUID();
        String sn     = gamePlatform.getAgent();

        Map<String, Object> params = new HashMap<>();
        params.put( "random", id );
        params.put( "sn", sn );
        params.put( "sign", DigestUtils.md5Hex( id + sn + gamePlatform.getMd5() ) );

        params.put( "timeZone", 1 );
        params.put( "pageIndex", 1 );
        params.put( "pageSize", 1000 );

        params.put( "startTime", startTime );
        params.put( "endTime", endTime );

        params.put( "agentLoginId", gamePlatform.getLinecode() );
        params.put( "gameType", gameType );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "id", id );
        requestMap.put( "method", method );
        requestMap.put( "jsonrpc", "2.0" );
        requestMap.put( "params", params );

        // log.warn( JsonUtil.object2Json( requestMap ) );

        String url = gamePlatform.getApiUrl() + "/" + method;

        Map<String, Object> resultMap = this.sendPostMap( url, packageJson( requestMap ) );

        // log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( resultMap.get( "result" ) != null && resultMap.get( "error" ) == null ) {
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( result ) ) {
                    return ( List<Map<String, Object>> ) result.getOrDefault( "items", new ArrayList<>() );
                }
            } else {
                log.error( url + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "betId" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "issueId" ) ) );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "loginId" ) ) );
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameId" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "validAmount" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "betAmount" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "payout" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "roomId" ) ) );
        String orderTime     = String.valueOf( remoteGameDatum.get( "orderTime" ) );
        String gameStartTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertMeiDongToDefault( orderTime ) );
        gameDataRecord.setGameStartTime( gameStartTime );
        gameDataRecord.setGameEndTime( gameStartTime );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
