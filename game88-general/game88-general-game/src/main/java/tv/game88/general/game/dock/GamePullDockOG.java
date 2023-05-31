package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.OG + "GamePullProcessor" )
public class GamePullDockOG extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 10 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        Map<String, Object> requestMap = new HashMap<>();

        requestMap.put( "X-Operator", gamePlatform.getDes() );
        requestMap.put( "x-key", gamePlatform.getMd5() );
        requestMap.put( "SDate", LocalDateTimeUtils.format( start, LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS_FORMATTER ) );
        requestMap.put( "EDate", LocalDateTimeUtils.format( end, LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS_FORMATTER ) );
        requestMap.put( "Provider", "ogplus" );

        String url = gamePlatform.getApiUrl() + "/transactions";

        Map<String, Object> resultMap = this.sendPostMap( url, packageForm( requestMap ) );
        log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataMapList = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( dataMapList ) && "0".equals( dataMapList.getOrDefault( "status", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                return ( List<Object> ) dataMapList;
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "id" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "roundno" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "membername" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameid" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "bettingamount" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "validbet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "winloseamount" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "mtype" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "bettingdate" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "bettingdate" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "vendor_id" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
