package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.JDB + "GamePullProcessor" )
public class GamePullDockJDB extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        long ts = System.currentTimeMillis();

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        long startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long endTime = LocalDateTimeUtils.localDateToTimestamp( end );
        long intervalTime = endTime - ts;

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "action", intervalTime > 7200000L ? "64" : "29" );
        requestMap.put( "ts", String.valueOf( ts ) );
        requestMap.put( "parent", gamePlatform.getAgent() );
        requestMap.put( "starttime", LocalDateTimeUtils.format( start, LocalDateTimeUtils.DDMMYYYYHHMMSS_FORMATTER ) );
        requestMap.put( "endtime", LocalDateTimeUtils.format( end, LocalDateTimeUtils.DDMMYYYYHHMMSS_FORMATTER ) );
        requestMap.put( "gTypes", gamePlatform.getGameCategory().getType() );

        String url = gamePlatform.getRecordUrl() + "/apiRequest.do";

        Map<String, Object> resultMap = this.sendPostMap( url, packageJson( requestMap ) );
        if ( ! CollectionUtils.isEmpty( resultMap ) ) {
            List<Object> dataMapList = ( List<Object> ) resultMap.getOrDefault( "data", new ArrayList<>() );
            if ( ! CollectionUtils.isEmpty( dataMapList ) && "0000".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return dataMapList;
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "seqNo" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "roundSeqNo" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "playerId" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gType" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "bet" ) ) );
//        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "total" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "win" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "mtype" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "gameDate" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "lastModifyTime" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "mtype" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
