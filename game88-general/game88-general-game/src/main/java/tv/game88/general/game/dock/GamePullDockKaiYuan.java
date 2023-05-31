package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.DesCoder;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Log4j2
@Repository( value = ConstantsGame.KAI_YUAN + "GamePullProcessor" )
public class GamePullDockKaiYuan extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        long startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long endTime   = LocalDateTimeUtils.localDateToTimestamp( end );

        String time   = String.valueOf( System.currentTimeMillis() );
        String params = String.format( "s=%s&startTime=%s&endTime=%s", 6, startTime, endTime );
        String param  = null;
        try {
            param = DesCoder.encrypt( params, gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String keyParams = String.format( "ac=%s&all=%s&timestamp=%s", 9, 1, time ) + gamePlatform.getMd5();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "agent", gamePlatform.getAgent() );
        requestMap.put( "timestamp", time );
        requestMap.put( "param", param );
        requestMap.put( "key", DigestUtils.md5Hex( keyParams ) );

        String url = gamePlatform.getRecordUrl() + "/getRecordHandle" + this.assemblyUrl( requestMap ); //

        Map<String, Object> resultMap = this.sendGetMap( url );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "d", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( d ) && "0".equals( d.getOrDefault( "code", "-1" ).toString() ) ) {
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return ( List<Object> ) d.getOrDefault( "list", new ArrayList<>() );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "GameID" ) ) );
        String id = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( id );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "ServerID" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "userCode" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "KindID" ) ) );
        gameDataRecord.setCellScore( fenToYuan( String.valueOf( remoteGameDatum.get( "CellScore" ) ) ) );
        gameDataRecord.setAllBet( fenToYuan( String.valueOf( remoteGameDatum.get( "AllBet" ) ) ) );
        gameDataRecord.setProfit( fenToYuan( String.valueOf( remoteGameDatum.get( "Profit" ) ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "TableID" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "GameStartTime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "GameEndTime" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "Accounts" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }

    private String fenToYuan( String money ) {
        return new BigDecimal( money ).divide( new BigDecimal( 100 ), 2, RoundingMode.HALF_UP ).toString();
    }
}
