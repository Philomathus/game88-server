package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
@Repository( value = ConstantsGame.PG_SOFT + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockPGSoft extends AbstractGamePull {

    private static final Map<String, BigDecimal> RATE_MAP = Map.of( "IDR", new BigDecimal( 1000 ), "INR", BigDecimal.ONE, "CNY"
            , BigDecimal.ONE );

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "operator_token", gamePlatform.getDes() );
        params.add( "secret_key", gamePlatform.getMd5() );
        params.add( "count", "5000" );
        params.add( "bet_type", "1" );
        params.add( "row_version", gamePlatform.getVersionValue() );

        String url = gamePlatform.getApiUrl() + "/external-datagrabber/Bet/v4/GetHistory?trace_id=" + UUID.randomUUID();

        log.warn( url + " ::: " + JsonUtil.object2Json( params ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            List<Object> dataList = ( List<Object> ) resultMap.getOrDefault( "data", Collections.EMPTY_LIST );
            if ( !CollectionUtils.isEmpty( dataList ) ) {
                Map obj = ( Map ) dataList.getLast();
                gamePlatform.setVersionValue( obj.get( "rowVersion" ).toString() );
                return dataList;
            }
            if ( resultMap.get( "error" ) != null ) {
                log.warn( JsonUtil.object2Json( resultMap ) );
            } else {
                gamePlatform.setVersionValue( String.valueOf( Long.parseLong( gamePlatform.getVersionValue() ) + 60000 ) );
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

        BigDecimal RATE = BigDecimal.ONE;
        if ( agent.startsWith( "99in" ) ) {
            RATE = RATE_MAP.get( "INR" );
        } else if ( agent.startsWith( "99id" ) || agent.startsWith( "99" ) ) {
            RATE = RATE_MAP.get( "IDR" );
        } else if ( agent.startsWith( "88" ) ) {
            RATE = RATE_MAP.get( "CNY" );
        }

        BigDecimal betAmount = new BigDecimal( String.valueOf( remoteGameDatum.get( "betAmount" ) ) ).multiply( RATE );
        gameDataRecord.setAllBet( betAmount.toString() );
        gameDataRecord.setCellScore( gameDataRecord.getAllBet() );
        BigDecimal payoffAmount = new BigDecimal( String.valueOf( remoteGameDatum.get( "winAmount" ) ) ).multiply( RATE );
        gameDataRecord.setProfit( payoffAmount.subtract( betAmount ).toString() );
        return gameDataRecord;
    }
}
