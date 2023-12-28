package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpMethod;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Repository( value = ConstantsGame.JILI + "GamePullProcessor" )
public class GamePullDockJiLi extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是6分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 6 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        String startTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC_4( start ),
                LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER );
        String endTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC_4( end ),
                LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER );

        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "StartTime", startTime );
        params.put( "EndTime", endTime );
        params.put( "Page", 1 );
        params.put( "PageLimit", 25000 );

        params.put( "AgentId", gamePlatform.getAgent() );
        params.put( "Key", getKey( params, gamePlatform ) );

        final String url = getURL( gamePlatform.getApiUrl(), "/GetBetRecordByTime", params );

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.GET, restTemplate.httpEntityCallback( null ),
                response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( Integer.parseInt( resultMap.getOrDefault( "ErrorCode", "-1" ).toString() ) == 0 ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "Data", Collections.EMPTY_MAP );
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                return ( List<Object> ) dataMap.getOrDefault( "Result", Collections.EMPTY_LIST );
            }
            log.error( url + ":::" + JsonUtil.object2Json( resultMap ) );
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "WagersId" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String account  = String.valueOf( remoteGameDatum.get( "Account" ) ).toLowerCase();
        String agent    = account.substring( 0, account.lastIndexOf( "m" ) );
        String memberId = agent + "_" + account.substring( account.lastIndexOf( "m" ) ).toUpperCase();
        gameDataRecord.setAccount( memberId );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "GameId" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        String startTime = remoteGameDatum.get( "WagersTime" ).toString();
        LocalDateTime startTimeLocal = LocalDateTimeUtils.convertUTC_4ToDefault( startTime,
                LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XXXFORMATTER );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( startTimeLocal ) );
        String endTime = remoteGameDatum.get( "SettlementTime" ).toString();
        LocalDateTime endTimeLocal = LocalDateTimeUtils.convertUTC_4ToDefault( endTime,
                LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XXXFORMATTER );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( endTimeLocal ) );

        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "Turnover" ) ) );
        BigDecimal betAmount = new BigDecimal( String.valueOf( remoteGameDatum.get( "BetAmount" ) ) ).negate();
        gameDataRecord.setAllBet( betAmount.toString() );
        String payoffAmount = String.valueOf( remoteGameDatum.get( "PayoffAmount" ) );
        gameDataRecord.setProfit( new BigDecimal( payoffAmount ).subtract( betAmount ).toString() );
        return gameDataRecord;
    }

    private String getKey( final Map<String, Object> params, final GamePlatform gamePlatform ) {
        final String now = LocalDateTimeUtils.format( LocalDate.now( ZoneId.of( "UTC-4" ) ), DateTimeFormatter.ofPattern(
                "yyMMd" ) );
        final String keyG = DigestUtils.md5Hex( now + gamePlatform.getAgent() + gamePlatform.getMd5() );

        final String queryString = keyValStringFormat( params );
        final String md5string   = DigestUtils.md5Hex( queryString + keyG );

        return RandomStringUtils.randomAlphabetic( 6 ) + md5string + RandomStringUtils.randomAlphabetic( 6 );
    }

    private String keyValStringFormat( final Map<String, Object> params ) {
        return params.keySet().stream().map( key -> key + "=" + params.get( key ) ).collect( Collectors.joining( "&" ) );
    }

    private String getURL( final String apiURL, final String endpoint, final Map<String, Object> params ) {
        final StringBuilder sb = new StringBuilder()
                .append( apiURL )
                .append( endpoint )
                .append( "?" )
                .append( keyValStringFormat( params ) );
        return sb.toString();
    }
}
