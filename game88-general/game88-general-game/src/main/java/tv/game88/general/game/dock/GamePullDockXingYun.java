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
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.XINGYUN + "GamePullProcessor" )
public class GamePullDockXingYun extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end       = start.plusMinutes( 1 );
        long          startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long          endTime   = LocalDateTimeUtils.localDateToTimestamp( end );

        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "platformno", gamePlatform.getAgent() );
        params.put( "requesttime", System.currentTimeMillis() / 1000 );
        params.put( "sign", gamePlatform.getDes() );
        params.put( "starttime", startTime / 1000 );
        params.put( "endtime", endTime / 1000 );

        String param = null;
        try {
            param = AESCoder.encryptDES3( JsonUtil.object2Json( params ), gamePlatform.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "platformno", gamePlatform.getAgent() );
        requestMap.put( "parameter", param );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( Map.of( "params", param ), httpHeaders );

        String url = gamePlatform.getApiUrl() + "/Game/roundRecord";

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            List<Object> recordMapList = ( List<Object> ) resultMap.getOrDefault( "result", new ArrayList<>() );
            if ( !CollectionUtils.isEmpty( recordMapList ) ) {
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
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameid" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "recordid" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "username" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "roomname" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "effectivebet" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "totalbet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "platform_profit " ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "tableno" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "starttime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "endtime" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "platformid " ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
