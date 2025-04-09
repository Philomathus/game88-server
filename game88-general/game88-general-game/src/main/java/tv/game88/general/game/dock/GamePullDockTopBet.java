package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Log4j2
@Repository( value = ConstantsGame.TOP_BET + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockTopBet extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        long gamePlatformVersion = Long.parseLong( gamePlatform.getVersionValue() );

        Map<String, Object> params = new TreeMap<>();
        params.put( "pid", gamePlatform.getAgent() );
        params.put( "ver", "2.0.0" );
        params.put( "method", "GAMELOG" );
        params.put( "index", gamePlatformVersion );
        params.put( "count", 5000 );
        params.put( "org", 0 );
        params.put( "sign", DigestUtils.md5Hex( assemblyUrl( params ) + "&apikey=" + gamePlatform.getMd5() ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( gamePlatform.getApiUrl(), HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        // log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
            List<Object> dataList = ( List<Object> ) resultMap.getOrDefault( "list", Collections.EMPTY_LIST );
            if ( !CollectionUtils.isEmpty( dataList ) ) {
                Map obj = ( Map ) dataList.getLast();
                gamePlatform.setVersionValue( obj.get( "index" ).toString() );
                return dataList;
            }
            if ( StringUtils.isNotBlank( resultMap.getOrDefault( "message", "" ).toString() ) ) {
                log.warn( JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        // 9901_M22611
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "index" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "game_record" ) ) );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "user_name" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "app_id" ) ) );
        gameDataRecord.setCurrency( String.valueOf( remoteGameDatum.get( "currency" ) ) );
        String bet = String.valueOf( remoteGameDatum.get( "total_pay" ) );
        gameDataRecord.setCellScore( bet );
        gameDataRecord.setAllBet( bet );
        BigDecimal win = new BigDecimal( String.valueOf( remoteGameDatum.get( "profit" ) ) );
        gameDataRecord.setProfit( win.subtract( new BigDecimal( bet ) ).stripTrailingZeros().toPlainString() );
        LocalDateTime startDate = LocalDateTimeUtils.parseLocalDateTime( String.valueOf( remoteGameDatum.get( "start_time" ) ),
                LocalDateTimeUtils.LOCALTIME_FORMATTER );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( startDate ) );
        LocalDateTime endDate = LocalDateTimeUtils.parseLocalDateTime( String.valueOf( remoteGameDatum.get( "end_time" ) ),
                LocalDateTimeUtils.LOCALTIME_FORMATTER );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( endDate ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
