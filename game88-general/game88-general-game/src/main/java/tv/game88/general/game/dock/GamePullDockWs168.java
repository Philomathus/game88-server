package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Repository( value = ConstantsGame.WS168 + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockWs168 extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        String startTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC0Zoned( start ),
                LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XXXFORMATTER );
        String endTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC0Zoned( end ),
                LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XXXFORMATTER );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "time_type", "settled_at" );
        requestMap.put( "page", 1 );
        requestMap.put( "page_size", 10000 );
        requestMap.put( "start_time", startTime );
        requestMap.put( "end_time", endTime );

        String url = gamePlatform.getApiUrl() + "/api/merchant/bets";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.set( "Authorization", "Bearer " + gamePlatform.getMd5() );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            Pattern pattern = Pattern.compile( "\\{.*\\}" );
            Matcher matcher = pattern.matcher( e.getMessage() );
            if ( matcher.find() ) {
                String jsonContent = matcher.group();
                resultMap = JsonUtil.json2Map( jsonContent );
            } else {
                log.error( e.getMessage(), e );
            }
        }

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "OK".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( LocalDateTimeUtils.localDateToTimestamp( end ) + "" );
                return ( List<Object> ) resultMap.getOrDefault( "data", new ArrayList<>() );
            } else {
                log.error( url + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        if ( !"settled".equals( String.valueOf( remoteGameDatum.get( "status" ) ) ) ) {
            return null;
        }
        GameDataRecord gameDataRecord = new GameDataRecord();
        LocalDateTime betTime = LocalDateTimeUtils.convertUTC0ToDefault( String.valueOf( remoteGameDatum.get( "bet_at" ) ),
                LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XXXFORMATTER );
        LocalDateTime settledTime =
                LocalDateTimeUtils.convertUTC0ToDefault( String.valueOf( remoteGameDatum.get( "settled_at" ) ),
                        LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XXXFORMATTER );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( betTime ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( settledTime ) );

        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "slug" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "account" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "round_id" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "category" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "valid_amount" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "bet_amount" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "net_income" ) ) );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        return gameDataRecord;
    }
}
