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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository ( value = ConstantsGame.GAMING_365 + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDock365 extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是6分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 6 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        long startTime = Long.parseLong( gamePlatform.getVersionValue() );
        long endTime   = LocalDateTimeUtils.localDateToTimestamp( end );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "cert", gamePlatform.getMd5() );
        requestMap.put( "extension1", gamePlatform.getAgent() );
        requestMap.put( "status", 1 );
        requestMap.put( "st", startTime );
        requestMap.put( "et", endTime );

        String url = gamePlatform.getRecordUrl() + "/api/" + gamePlatform.getDes() + "/getTransactionsByPayTime";

        Map<String, Object> resultMap = this.sendPostMap( url, packageForm( requestMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = resultMap.getOrDefault( "status", "" ).toString();
            if ( "1".equals( status ) || "1003".equals( status ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return ( List<Object> ) resultMap.getOrDefault( "transactions", new ArrayList<>() );
            } else {
                log.error( url + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        if ( !"Pay".equals( String.valueOf( remoteGameDatum.get( "status" ) ) ) ) {
            return null;
        }
        GameDataRecord gameDataRecord = new GameDataRecord();

        String payTransTimeStr = String.valueOf( remoteGameDatum.get( "payTransTime" ) );
        String betTransTimeStr = String.valueOf( remoteGameDatum.get( "betTransTime" ) );

        LocalDateTime payTransTime = LocalDateTimeUtils.parseLocalDateTime( payTransTimeStr,
                LocalDateTimeUtils.MMDDYYYYHHMMSSSSS_FORMATTER );
        LocalDateTime betTransTime = LocalDateTimeUtils.parseLocalDateTime( betTransTimeStr,
                LocalDateTimeUtils.MMDDYYYYHHMMSSSSS_FORMATTER );

        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( betTransTime ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( payTransTime ) );

        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "txId" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        String[] accounts = String.valueOf( remoteGameDatum.get( "userId" ) ).toUpperCase().split( "_" );
        String   agent    = accounts[ 0 ].toLowerCase();
        gameDataRecord.setAccount( agent + "_" + accounts[ 1 ] );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "gameNumber" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameId" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "validbet" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "realBetAmount" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "profit" ) ) );
        // gameDataRecord.setServerId( String.valueOf( remoteGameDatum.get( "playId" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "playId" ) ) );
        gameDataRecord.setChairId( String.valueOf( remoteGameDatum.get( "chair" ) ) );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
