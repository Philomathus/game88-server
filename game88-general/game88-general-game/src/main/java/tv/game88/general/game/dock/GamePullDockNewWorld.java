package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import tv.game88.general.api.dto.RspZdXsjList;
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
@Repository ( value = ConstantsGame.NEWWORLD + "GamePullProcessor" )
public class GamePullDockNewWorld extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 4 ) ) ) {
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
        requestMap.set( "channel", gamePlatform.getAgent() );
        requestMap.set( "mTime", time );
        requestMap.set( "paramerter", param );
        requestMap.set( "key", key );

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( gamePlatform.getRecordUrl() )
                .queryParams( requestMap )
                .build( true );

        // log.warn( uriComponents.toUriString() );
        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null ),
                response -> {
                    InputStream bodyStream = response.getBody();
                    String      text;
                    try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                        text = IOUtils.toString( reader );
                    }
                    return JsonUtil.json2Map( text );
                } );

        // log.warn( uriComponents.toUriString() + "::" + JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "dataStr", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( d ) && "0".equals( d.getOrDefault( "code", "-1" ).toString() ) ) {
                gamePlatform.setVersionValue( String.valueOf( endTime ) );

                Map<String, Object> list       = ( Map<String, Object> ) d.getOrDefault( "contents", new HashMap<>() );
                Integer   count      = ( Integer ) d.getOrDefault( "sum", 0 );
                RspZdXsjList rspZdList  = JsonUtil.map2Object( list, RspZdXsjList.class );
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
            } else {
                log.error( gamePlatform.getName() + ":::" + uriComponents.toUriString() + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameCode" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String   accounts   = String.valueOf( remoteGameDatum.get( "PlayerAccount" ) );
        String[] splitParam = accounts.split( "_" );
        if ( !gamePlatform.getAgent().equals( splitParam[ 0 ] ) ) {
            return null;
        }
        gameDataRecord.setAccount( StringUtils.substringAfter( accounts, "_" ) );
        gameDataRecord.setAgent( splitParam[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "GameArrNo" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "effScore" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "allScore" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "netIn" ) ) );
        gameDataRecord.setRevenue( String.valueOf( remoteGameDatum.get( "sharesScore" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "table" ) ) );
        gameDataRecord.setChairId( String.valueOf( remoteGameDatum.get( "bank" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "gameBeginTime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "gameFinishTime" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
