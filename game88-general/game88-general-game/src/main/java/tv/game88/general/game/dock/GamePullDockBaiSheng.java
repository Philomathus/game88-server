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
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.BAISHENG + "GamePullProcessor" )
public class GamePullDockBaiSheng extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        long startTime = System.currentTimeMillis();
        long endTime   = startTime + 30000;

        String params = String.format( "action=9&start_time=%s&end_time=%s&money_type=RMB", String.valueOf( startTime ),
                String.valueOf( endTime ) );
        String param  = null;
        try {
            param = AESCoder.encryptByKeyUrl( params, gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        // 如果不是1分钟前的时间,跳过
        if ( startTime > ( System.currentTimeMillis() + 60000 ) ) {
            return null;
        }

        String key = DigestUtils.md5Hex( gamePlatform.getAgent() + startTime + gamePlatform.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "param", param );
        requestMap.set( "channel_id", gamePlatform.getAgent() );
        requestMap.set( "timestamp", String.valueOf( startTime ) );
        requestMap.set( "key", key );

        String url = gamePlatform.getRecordUrl() + "/Api/interface?" + params;

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
            List<Object> records = ( List<Object> ) resultMap.getOrDefault( "records", new ArrayList<Map<String, Object>>() );
            if ( !CollectionUtils.isEmpty( records ) && "0".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( resultMap.getOrDefault( "time", endTime ) ) );
                return records;
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "game_id" ) ) );

        String logId = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( logId );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "round_id" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "user_id" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "room_id" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "avail_bet" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "all_bet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "profit" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "seat_id" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "start_time" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "end_time" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "channel_id" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
