package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
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
@Repository( value = ConstantsGame.SGWIN + "GamePullProcessor" )
public class GamePullDockSGWin extends AbstractGamePull {
    @Override
    public List<Map<String, Object>> requestRemoteGameData(GamePlatform gamePlatform) {
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
        String keyParams = String.format( "ac=%s&all=%s&timestamp=%s", 9 , 1 , time ) + gamePlatform.getMd5() ;

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "agentId", gamePlatform.getAgent() );
        requestMap.put( "timestamp", time );
        requestMap.put( "param", param );
        requestMap.put( "sign", DigestUtils.md5Hex( keyParams ) );

        String url = gamePlatform.getRecordUrl() + "/logHandle" + this.assemblyUrl( requestMap ); //

        Map<String, Object> resultMap = this.sendGetMap( url );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( d ) && "0".equals( d.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return ( List<Map<String, Object>> ) d.getOrDefault( "list", new ArrayList<>() );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult(Map<String, Object> remoteGameDatum, GamePlatform gamePlatform) {
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameId" ) ) );
        String id   = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( id );
//        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "roundId" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "userCode" ) ) );
//        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "nid" ) ) );
        gameDataRecord.setCellScore( fenToYuan( String.valueOf( remoteGameDatum.get( "effectBet" ) ) ) );
        gameDataRecord.setAllBet( fenToYuan( String.valueOf( remoteGameDatum.get( "totalBet" ) ) ) );
        gameDataRecord.setProfit( fenToYuan( String.valueOf( remoteGameDatum.get( "allWin" ) ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "deskId" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "openTime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "endTime" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "agentId" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }

    private String fenToYuan( String money ) {
        return new BigDecimal( money ).divide( new BigDecimal( 100 ), 2, RoundingMode.HALF_UP ).toString();
    }

}
