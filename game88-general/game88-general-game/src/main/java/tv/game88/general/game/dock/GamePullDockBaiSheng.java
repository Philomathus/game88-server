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
@Repository ( value = ConstantsGame.BAISHENG + "GamePullProcessor" )
public class GamePullDockBaiSheng extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 6 ) ) ) {
            return null;
        }

        LocalDateTime end = start.plusMinutes( 1 );

        long startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long endTime   = LocalDateTimeUtils.localDateToTimestamp( end );

        String params = String.format( "action=9&start_time=%s&end_time=%s&money_type=RMB", startTime / 1000, endTime / 1000 );
        String param  = null;
        try {
            param = AESCoder.encryptByKeyUrl( params, gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        String time = String.valueOf( System.currentTimeMillis() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "param", param );
        requestMap.set( "channel_id", gamePlatform.getAgent() );
        requestMap.set( "timestamp", time );
        requestMap.set( "key", DigestUtils.md5Hex( gamePlatform.getAgent() + time + gamePlatform.getMd5() ) );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( gamePlatform.getRecordUrl() ).queryParams( requestMap ).build( true );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
                    InputStream bodyStream = response.getBody();
                    String      text;
                    try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                        text = IOUtils.toString( reader );
                    }
                    return JsonUtil.json2Map( text );
                } );

        log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( result ) ) {
                    return ( List<Object> ) result.getOrDefault( "records", new ArrayList<>() );
                }
            } else {
                log.error( uriComponents.toUriString() + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "round_id" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String account = String.valueOf( remoteGameDatum.get( "user_id" ) ).toUpperCase();
        String agent   = account.split( "_" )[ 0 ].toLowerCase();
        gameDataRecord.setAccount( account );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_id" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "avail_bet" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "all_bet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "user_profit" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "table_id" ) ) );
        gameDataRecord.setChairId( String.valueOf( remoteGameDatum.get( "seat_id" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "start_time" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "end_time" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "channel_id" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
