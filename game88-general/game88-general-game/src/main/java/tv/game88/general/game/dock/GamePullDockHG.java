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
@Repository( value = ConstantsGame.HG + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockHG extends AbstractGamePull {

    private static final String MODE = "2";

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        Map<String, Object> params = new HashMap<>();
        params.put( "action", "record" );
        params.put( "merchant", gamePlatform.getAgent() );
        params.put( "agent", gamePlatform.getLinecode() );
        params.put( "startDate", LocalDateTimeUtils.format( start, LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        params.put( "endDate", LocalDateTimeUtils.format( end, LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        params.put( "page", 1 );
        params.put( "pageSize", 1000 );
        params.put( "mode", MODE );

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

        String url = String.format( "%s/api/game/%s/handle", gamePlatform.getApiUrl(), gamePlatform.getAgent() );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.PUT,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                return ( List<Object> ) result.getOrDefault( "bets", new ArrayList<>() );
            }
        } else {
            log.warn( url + " ::: " + JsonUtil.object2Json( resultMap ) );
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameNumber" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "gameNumber" ) ) );
        String   userName      = String.valueOf( remoteGameDatum.get( "userName" ) );
        String[] userNameSplit = userName.split( "_" );
        String   agent         = userNameSplit[ userNameSplit.length - 2 ];
        String   account       = agent + "_" + userNameSplit[ userNameSplit.length - 1 ];
        gameDataRecord.setAccount( account );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameCode" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "goldEffective" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "goldBet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "gold" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "tableNumber" ) ) );
        gameDataRecord.setChairId( String.valueOf( remoteGameDatum.get( "chairNumber" ) ) );
        long   gameStartTime = Long.parseLong( String.valueOf( remoteGameDatum.get( "gameStartTime" ) ) );
        String startTime     = LocalDateTimeUtils.format( LocalDateTimeUtils.getDateTimeFromTimestamp( gameStartTime ) );
        gameDataRecord.setGameStartTime( startTime );
        long   gameEndTime = Long.parseLong( String.valueOf( remoteGameDatum.get( "gameEndTime" ) ) );
        String endTime     = LocalDateTimeUtils.format( LocalDateTimeUtils.getDateTimeFromTimestamp( gameEndTime ) );
        gameDataRecord.setGameEndTime( endTime );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
