package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
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
@Repository( value = ConstantsGame.BOLE + "GamePullProcessor" )
public class GamePullDockBoLe extends AbstractGamePull {

    @Override
    public List<Map<String, Object>> requestRemoteGameData( GamePlatform gamePlatform ) {
        long ts = System.currentTimeMillis();
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        long startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long endTime = LocalDateTimeUtils.localDateToTimestamp( end );
        String nonce = GenerateOrderCacheUtils.me.getOrderId( "", 5 );
        String sign = AESCoder.encrypt( gamePlatform.getDes() + nonce + String.valueOf( ts ) );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "AccessKeyId", gamePlatform.getDes() );
        requestMap.put( "Timestamp", String.valueOf( ts ) );
        requestMap.put( "nonce", nonce );
        requestMap.put( "sign", sign );
        requestMap.put( "start_time", startTime );
        requestMap.put( "end_time", endTime );

        String url = gamePlatform.getRecordUrl() + "/v1/game/get_all_record_list";

        Map<String, Object> resultMap = this.sendPostMap( url, packageJson( requestMap ) );
        if ( ! CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> respMsgMap = ( Map<String, Object> ) resultMap.getOrDefault( "resp_msg", new HashMap<>() );
            if ( ! CollectionUtils.isEmpty( resultMap ) && "200".equals( respMsgMap.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                Map<String, Object> respDataMap = ( Map<String, Object> ) resultMap.getOrDefault( "resp_data", new HashMap<>() );
                if ( ! CollectionUtils.isEmpty( respDataMap ) ) {
                    List<Map<String, Object>> dataMapList = ( List<Map<String, Object>> ) resultMap.getOrDefault( "data", new ArrayList<HashMap<>>() );
                    gamePlatform.setVersionValue( String.valueOf( endTime ) );
                    return dataMapList;
                }
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Map<String, Object> remoteGameDatum, GamePlatform gamePlatform ) {
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "game_id" ) ) );

        String logId = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( logId );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "room_id" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "player_account" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_code" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "bet_num_valid" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "income_gold" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "gain_gold" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "scene" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "start_time" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "end_time" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "sn" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
