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
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.PG_SOFT + "GamePullProcessor" )
public class GamePullDockPGSoft extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put( "operator_token", gamePlatform.getDes() );
        params.put( "secret_key", gamePlatform.getMd5() );
        params.put( "count", "5000" );
        params.put( "bet_type", "1" );
        params.put( "row_version", gamePlatform.getVersionValue() );

        String url           = gamePlatform.getApiUrl() + "/external-datagrabber/Bet/v4/GetHistory?trace_id=" + UUID.randomUUID();
        String assemblyParam = assemblyUrl( params );

        log.warn( url + " ::: " + assemblyParam );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<String> requestEntity = new HttpEntity<>( assemblyParam, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            List<Object> dataList = ( List<Object> ) resultMap.getOrDefault( "data", Collections.EMPTY_LIST );
            if ( !CollectionUtils.isEmpty( dataList ) ) {
                Map obj = ( Map ) dataList.getLast();
                gamePlatform.setVersionValue( obj.get( "rowVersion" ).toString() );
                return dataList;
            }
            if ( resultMap.get( "error" ) != null ) {
                log.warn( JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "betId" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String account = String.valueOf( remoteGameDatum.get( "playerName" ) );
        String agent   = account.split( "_" )[ 0 ];
        gameDataRecord.setAccount( account );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameId" ) ) );
        gameDataRecord.setCurrency( String.valueOf( remoteGameDatum.get( "currency" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );

        String        startTime = remoteGameDatum.get( "betTime" ).toString();
        LocalDateTime start     = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( startTime ) );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( start ) );
        String        endTime = remoteGameDatum.get( "betEndTime" ).toString();
        LocalDateTime end     = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( endTime ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( end ) );

        BigDecimal betAmount = new BigDecimal( String.valueOf( remoteGameDatum.get( "betAmount" ) ) );
        gameDataRecord.setAllBet( betAmount.toString() );
        gameDataRecord.setCellScore( gameDataRecord.getAllBet() );
        String payoffAmount = String.valueOf( remoteGameDatum.get( "winAmount" ) );
        gameDataRecord.setProfit( new BigDecimal( payoffAmount ).subtract( betAmount ).toString() );
        return gameDataRecord;
    }
}
