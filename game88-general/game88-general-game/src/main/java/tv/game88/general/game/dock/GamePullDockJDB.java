package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@Log4j2
@Repository( value = ConstantsGame.JDB + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockJDB extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 4 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        long intervalTime = LocalDateTimeUtils.getIntervalTime( end, LocalDateTime.now() );

        Map<String, Object> params = new HashMap<>();
        params.put( "action", intervalTime > 7200000L ? "64" : "29" );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "parent", gamePlatform.getAgent() );
        params.put( "starttime", LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC_4( start ),
                LocalDateTimeUtils.DDMMYYYYHHMM00_FORMATTER ) );
        params.put( "endtime", LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC_4( end ),
                LocalDateTimeUtils.DDMMYYYYHHMM00_FORMATTER ) );

        String json         = JsonUtil.object2Json( params );
        String encodedParam = null;
        try {
            encodedParam = AESCoder.encryptByKeyIvNoPadding( json, gamePlatform.getMd5(), gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        // log.warn( json );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "dc", gamePlatform.getLinecode() );
        requestMap.put( "x", encodedParam );

        String url = gamePlatform.getApiUrl() + "/apiRequest.do";

        Map<String, Object> resultMap = this.sendPostMap( url, packageJson( requestMap ) );

        log.warn( "url:{} - x:{} - request:{} - result:{}", url, json, JsonUtil.object2Json( requestMap ),
                JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0000".equals( resultMap.getOrDefault( "status", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
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
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        String              historyId       = String.valueOf( remoteGameDatum.get( "historyId" ) );
        if ( StringUtils.isBlank( historyId ) ) {
            log.error( "seqNo为空" + JsonUtil.object2Json( object ) );
            return null;
        }
        gameDataRecord.setGameId( historyId );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String account  = String.valueOf( remoteGameDatum.get( "playerId" ) ).toLowerCase();
        String agent    = null;
        String memberId = null;
        if ( account.startsWith( "88" ) || account.startsWith( "99" ) ) {
            if ( account.startsWith( "88ky" ) && !account.contains( "m" ) ) {
                Matcher matcher = GET_NUMBER.matcher( account );
                if ( matcher.find() ) {
                    String memberAccount = matcher.group();
                    agent    = account.substring( 0, account.lastIndexOf( memberAccount ) ).toLowerCase();
                    memberId = agent + "_" + memberAccount;
                }
            } else {
                agent    = account.substring( 0, account.lastIndexOf( "m" ) );
                memberId = agent + "_" + account.substring( account.lastIndexOf( "m" ) ).toUpperCase();
            }
        } else if ( account.startsWith( "77" ) ) {
            Matcher matcher = GET_NUMBER.matcher( account );
            if ( matcher.find() ) {
                String memberAccount = matcher.group();
                agent    = account.substring( 0, account.lastIndexOf( memberAccount ) ).toLowerCase();
                memberId = agent + "_" + memberAccount;
            }
        }
        if ( agent == null ) {
            return null;
        }
        gameDataRecord.setAccount( memberId );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setKindId( remoteGameDatum.get( "gType" ) + "-" + remoteGameDatum.get( "mtype" ) );
        String bet = new BigDecimal( String.valueOf( remoteGameDatum.get( "bet" ) ) ).negate().toString();
        gameDataRecord.setCellScore( bet );
        gameDataRecord.setAllBet( bet );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "total" ) ) );
        String startTime = remoteGameDatum.get( "gameDate" ).toString();
        LocalDateTime startTimeLocal = LocalDateTimeUtils.convertUTC_4ToDefault( startTime,
                LocalDateTimeUtils.DDMMYYYYHHMMSS_FORMATTER );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( startTimeLocal ) );
        String endTime = remoteGameDatum.get( "lastModifyTime" ).toString();
        LocalDateTime endTimeLocal = LocalDateTimeUtils.convertUTC_4ToDefault( endTime,
                LocalDateTimeUtils.DDMMYYYYHHMMSS_FORMATTER );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( endTimeLocal ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        gameDataRecord.setCurrency( String.valueOf( remoteGameDatum.get( "currency" ) ) );
        return gameDataRecord;
    }
}
