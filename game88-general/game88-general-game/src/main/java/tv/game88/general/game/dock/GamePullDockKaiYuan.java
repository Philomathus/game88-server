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
import tv.game88.general.api.dto.RspZdList;
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
@Repository( value = ConstantsGame.KAI_YUAN + ConstantsGame.GAME_PULL_PROCESSOR )
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
            param = AESCoder.encryptByKeyUrl( params, gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( gamePlatform.getAgent() + time + gamePlatform.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agent", gamePlatform.getAgent() );
        requestMap.set( "timestamp", time );
        requestMap.set( "param", param );
        requestMap.set( "key", key );

        log.warn( JsonUtil.object2Json( requestMap ) );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( gamePlatform.getRecordUrl() ).queryParams( requestMap )
                .build( true );

        log.warn( uriComponents.toUriString() );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        // log.warn( gamePlatform.getName() + "::" + uriComponents.toUriString() + "::" + JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d    = ( Map<String, Object> ) resultMap.getOrDefault( "d", new HashMap<>() );
            String              code = d.getOrDefault( "code", "-1" ).toString();
            if ( !CollectionUtils.isEmpty( d ) && ( "0".equals( code ) || "16".equals( code ) ) ) {
                gamePlatform.setVersionValue( String.valueOf( endTime ) );

                Map<String, Object> list = ( Map<String, Object> ) d.getOrDefault( "list", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( list ) ) {
                    Integer   count      = ( Integer ) d.getOrDefault( "count", 0 );
                    RspZdList rspZdList  = JsonUtil.map2Object( list, RspZdList.class );
                    String[]  fieldNames = getFiledName( rspZdList );

                    List<Object> resultList = new ArrayList<>();
                    for ( int i = 0; i < count; i++ ) {
                        Map<String, Object> map = new HashMap<>();
                        //获取属性的名字
                        for ( String name : fieldNames ) {
                            List<Object> listValue = ( List<Object> ) getFieldValueByName( name, rspZdList );
                            Object       value     = listValue.get( i );
                            map.put( name, value );
                        }
                        resultList.add( map );
                    }
                    return resultList;
                }
            } else {
                log.error( gamePlatform.getName() + ":::" + uriComponents.toUriString() + ":::"
                        + JsonUtil.object2Json( resultMap ) );
            }
        } else {
            log.warn( gamePlatform.getName() + "::" + uriComponents.toUriString() + "::" + JsonUtil.object2Json( resultMap ) );
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "GameID" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "Accounts" ) ) );
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "KindID" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "CellScore" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "AllBet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "Profit" ) ) );
        gameDataRecord.setRevenue( String.valueOf( remoteGameDatum.get( "Revenue" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "TableID" ) ) );
        gameDataRecord.setChairId( String.valueOf( remoteGameDatum.get( "ChairID" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "GameStartTime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "GameEndTime" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
