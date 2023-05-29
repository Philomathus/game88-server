package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.NEWWORLD + "GamePullProcessor" )
public class GamePullDockNewWorld extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        long startTime = System.currentTimeMillis();
        long endTime   = startTime + 300000;

        String params = String.format( "method=6&startTime=%s&endTime=%s", String.valueOf( startTime ),
                String.valueOf( endTime ) );
        String param  = null;
        try {
            param = AESCoder.encryptByKeyUrl( params, gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        if ( startTime > ( System.currentTimeMillis() + 300000 ) ) {
            return null;
        }

        String key = DigestUtils.md5Hex( gamePlatform.getAgent() + startTime + gamePlatform.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "paramerter", param );
        requestMap.set( "channel", gamePlatform.getAgent() );
        requestMap.set( "timestamp", String.valueOf( startTime ) );
        requestMap.set( "key", key );

        String url = gamePlatform.getRecordUrl() + "/record?" + params;

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( url ).queryParams( requestMap ).build( true );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataStr = ( Map<String, Object> ) resultMap.getOrDefault( "dataStr", new HashMap<>() );
            List<Object> contentsMapList = ( List<Object> ) dataStr.getOrDefault( "contents",
                    new ArrayList<Map<String, Object>>() );
            if ( !CollectionUtils.isEmpty( contentsMapList ) && "0".equals( dataStr.getOrDefault( "code", "-1" ).toString() ) ) {
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return contentsMapList;
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();

        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameCode" ) ) );

        String logId = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( logId );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "GameArrNo" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "PlayerAccount" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "ServerCode" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "effScore" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "allScore" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "netIn" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "table" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "gameBeginTime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "gameFinishTime" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "channelNo" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
