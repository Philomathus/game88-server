package tv.game88.general.game.dock;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.RICH88 + "GamePullProcessor" )
public class GamePullDockRich88 extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        String startTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC0( start ) );
        String endTime   = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC0( end ) );

        String time = String.valueOf( System.currentTimeMillis() );

        Map<String, Object> headerParams = new LinkedHashMap<>();
        headerParams.put( "api_key", DigestUtils.sha256Hex( gamePlatform.getAgent() + gamePlatform.getMd5() + time ) );
        headerParams.put( "pf_id", gamePlatform.getAgent() );
        headerParams.put( "timestamp", time );

        String url = String.format( "%s/v2/platform/bet/records?from=%s&to=%s", gamePlatform.getApiUrl(), startTime, endTime );

        Map<String, Object> resultMap = this.sendGetMap( url, packageForm( headerParams ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                return ( List<Object> ) resultMap.getOrDefault( "data", new ArrayList<Map<String, Object>>() );
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
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "record_id" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "round_id" ) ) );
        String account = String.valueOf( remoteGameDatum.get( "account" ) );
        String agent   = account.split( "_" )[ 0 ];
        gameDataRecord.setAccount( account );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_code" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "bet_valid" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "bet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "profit" ) ) );
        gameDataRecord.setRevenue( String.valueOf( remoteGameDatum.get( "tax" ) ) );
        String gameStartTime = String.valueOf( remoteGameDatum.get( "round_start_at" ) );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( LocalDateTimeUtils.convertUTC0ToDefault( gameStartTime ) ) );
        String gameEndTime = String.valueOf( remoteGameDatum.get( "round_end_at" ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( LocalDateTimeUtils.convertUTC0ToDefault( gameEndTime ) ) );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
