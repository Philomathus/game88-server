package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
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
@Repository( value = ConstantsGame.DATANG + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockDaTang extends AbstractGamePull {
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
        String params = String.format( "startTime=%s&endTime=%s", startTime, endTime );
        String param  = null;
        try {
            param = AESCoder.encryptByKey( params, gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( gamePlatform.getAgent() + time + gamePlatform.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agentid", gamePlatform.getAgent() );
        requestMap.set( "timestamp", time );
        requestMap.set( "type", "6" );
        requestMap.set( "paraValue", param );
        requestMap.set( "key", key );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( gamePlatform.getRecordUrl() + "/GetRecordHandle" )
                                                          .queryParams( requestMap ).build( true );
        String uriString = uriComponents.toUriString();

        Map<String, Object> resultMap = this.sendGetMap( uriString );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d    = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            String              code = d.getOrDefault( "code", "-1" ).toString();
            if ( !CollectionUtils.isEmpty( d ) && ( "0".equals( code ) || "101".equals( code ) ) ) {
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return ( List<Object> ) d.getOrDefault( "list", new ArrayList<>() );
            } else {
                log.error( uriString + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "ObjectID" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "GameInfoID" ) ) );
        String account = String.valueOf( remoteGameDatum.get( "Account" ) );
        String agent   = account.split( "_" )[ 0 ];
        gameDataRecord.setAccount( account );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "GameID" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "ValidBet" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "AllBet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "Profit" ) ) );
        gameDataRecord.setRevenue( String.valueOf( remoteGameDatum.get( "Revenue" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "TableID" ) ) );
        gameDataRecord.setChairId( String.valueOf( remoteGameDatum.get( "ChairID" ) ) );
        String gameStartTime = String.valueOf( remoteGameDatum.get( "GameStartTime" ) );
        gameDataRecord.setGameStartTime( gameStartTime.substring( 0, gameStartTime.length() - 4 ) );
        String gameEndTime = String.valueOf( remoteGameDatum.get( "GameEndTime" ) );
        gameDataRecord.setGameEndTime( gameEndTime.substring( 0, gameEndTime.length() - 4 ) );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
