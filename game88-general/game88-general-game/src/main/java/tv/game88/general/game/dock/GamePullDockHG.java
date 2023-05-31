package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.HG + "GamePullProcessor" )
public class GamePullDockHG extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );
        long endTime = LocalDateTimeUtils.localDateToTimestamp( end );

        Map<String, Object> params = new HashMap<>();
        params.put( "action", "record" );
        params.put( "merchant", gamePlatform.getDes() );
        params.put( "agent", gamePlatform.getAgent() );
        params.put( "startDate", LocalDateTimeUtils.format( start, LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        params.put( "endDate", LocalDateTimeUtils.format( end, LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        params.put( "page", 1 );
        params.put( "pageSize", 1000 );

        String param = null;
        try {
            param = AESCoder.encryptDES3( JsonUtil.object2Json( params ), gamePlatform.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( Map.of( "params", param ), httpHeaders );

        String url = String.format( "%s/api/game/%s/handle", gamePlatform.getRecordUrl(), gamePlatform.getDes() );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.PUT,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
                    InputStream bodyStream = response.getBody();
                    String text;
                    try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                        text = IOUtils.toString( reader );
                    }
                    return JsonUtil.json2Map( text );
                } );

        if ( ! CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            List<Object> recordMapList = ( List<Object> ) resultMap.getOrDefault( "result", new ArrayList<>() );
            if ( ! CollectionUtils.isEmpty( recordMapList ) ) {
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return recordMapList;
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameCode" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "gameNumber" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "userName" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "arenaId" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "goldEffective" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "goldBet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "gold " ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "tableNumber" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "gameStartTime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "gameEndTime" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "agent " ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
