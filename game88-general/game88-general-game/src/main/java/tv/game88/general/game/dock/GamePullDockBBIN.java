package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
@Repository( value = ConstantsGame.BBIN + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockBBIN extends AbstractGamePull {
    private static final List<String> WAGER_TYPE_LIST    = Arrays.asList( "3", "5-1", "5-2", "5-3", "5-5", "12", /*"30",*/ "31",
            "38", "66", /*"93",*/ "99" );
    private static final String       WAGERS_RECORD_KEY8 = "SNjv90Bz";

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 7 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        LocalDateTime startMD = LocalDateTimeUtils.convertToMeiDong( start );
        LocalDateTime endMD   = LocalDateTimeUtils.convertToMeiDong( end );

        if ( !LocalDateTimeUtils.isSameDay( startMD, endMD ) ) {
            endMD = startMD.toLocalDate().atTime( 23, 59, 59 );
            end = LocalDateTimeUtils.convertMeiDongToDefault( LocalDateTimeUtils.format( endMD.plusSeconds( 1 ) ) );
        }

        String date      = LocalDateTimeUtils.format( startMD, LocalDateTimeUtils.YYYY_MM_DD_FORMATTER );
        String startTime = LocalDateTimeUtils.format( startMD, LocalDateTimeUtils.HH_MM_SS_FORMATTER );
        String endTime   = LocalDateTimeUtils.format( endMD, LocalDateTimeUtils.HH_MM_SS_FORMATTER );

        List<Callable<List<Map<String, Object>>>> forkJoinTasks = new ArrayList<>();
        for ( String wagerType : WAGER_TYPE_LIST ) {
            String[] wagerTypes = wagerType.split( "-" );
            forkJoinTasks.add( () -> queryList( gamePlatform, date, startTime, endTime, wagerTypes ) );
        }
        List<Future<List<Map<String, Object>>>> futures = null;
        try {
            futures = Executors.newVirtualThreadPerTaskExecutor().invokeAll( forkJoinTasks );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
        List<List<Map<String, Object>>> collect = futures.stream().map( t -> {
            try {
                return t.get();
            } catch ( Exception e ) {
                throw new IllegalStateException( e );
            }
        } ).filter( Objects::nonNull ).toList();
        List<Object> resultList = new ArrayList<>();
        for ( List<Map<String, Object>> mapList : collect ) {
            resultList.addAll( mapList );
        }

        // 状态正常,无论是否有数据,从结束时间开始查询
        gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
        return resultList;
    }

    private List<Map<String, Object>> queryList( GamePlatform gamePlatform, String date, String startTime, String endTime,
                                                 String[] wagerTypes ) {
        String a = RandomStringUtils.randomAlphabetic( 7 );
        String c = RandomStringUtils.randomAlphabetic( 4 );

        String dateMD = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToMeiDong( LocalDateTime.now() ),
                LocalDateTimeUtils.YYYYMMDD_FORMATTER );
        String md5 = DigestUtils.md5Hex( gamePlatform.getMd5() + WAGERS_RECORD_KEY8 + dateMD );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "website", gamePlatform.getMd5() );
        requestMap.set( "uppername", gamePlatform.getAgent() );
        requestMap.set( "action", "ModifiedTime" );
        requestMap.set( "date", date );
        requestMap.set( "starttime", startTime );
        requestMap.set( "endtime", endTime );
        requestMap.set( "key", a + md5 + c );
        if ( wagerTypes.length > 1 ) {
            requestMap.set( "subgamekind", wagerTypes[ 1 ] );
        }
        if ( "12".equals( wagerTypes[ 0 ] ) ) {
            requestMap.set( "gametype", "OTHER" );
        }

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( gamePlatform.getApiUrl() + "/WagersRecordBy" + wagerTypes[ 0 ] ).queryParams( requestMap )
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

        if ( !CollectionUtils.isEmpty( resultMap ) && BooleanUtils.toBoolean( resultMap.getOrDefault( "result", "false" )
                                                                                       .toString() ) ) {
            return ( List<Map<String, Object>> ) resultMap.get( "data" );
        } else {
            log.error( JsonUtil.object2Json( resultMap ) + ":::" + wagerTypes[ 0 ] );
            throw new BusinessException( JsonUtil.object2Json( resultMap ) );
        }
        //return new ArrayList<>();
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        if ( "X".equals( remoteGameDatum.get( "Result" ) ) ) {
            return null;
        }
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "WagersID" ) ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String   logId   = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );
        String   account = String.valueOf( remoteGameDatum.get( "UserName" ) ).replace( "bbin", "_" );
        String[] accounts = assemblyAccount( account );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );

        String endString;
        if ( remoteGameDatum.containsKey( "ModifiedDate" ) ) {
            LocalDateTime modifiedDate = LocalDateTimeUtils.convertMeiDongToDefault( String.valueOf( remoteGameDatum.get(
                    "ModifiedDate" ) ) );
            endString = LocalDateTimeUtils.format( modifiedDate );
        } else if ( remoteGameDatum.containsKey( "PayoutTime" ) ) {
            LocalDateTime payoutTime = LocalDateTimeUtils.convertMeiDongToDefault( String.valueOf( remoteGameDatum.get(
                    "PayoutTime" ) ) );
            endString = LocalDateTimeUtils.format( payoutTime );
        } else {
            log.error( JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }

        gameDataRecord.setId( logId );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "GameType" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "Commissionable" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "BetAmount" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "Payoff" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "GameCode" ) ) );

        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        gameDataRecord.setGameStartTime( endString );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( LocalDateTime.now() ) );
        return gameDataRecord;
    }
}
