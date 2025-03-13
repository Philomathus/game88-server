package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.EVO + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockEvo extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusHours( 2 );

        Map<String, Object> params = new HashMap<>();
        params.put( "start_date", LocalDateTimeUtils.convertToUTC0Zoned( start )
                .format( LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XFORMATTER ) );
        params.put( "end_date", LocalDateTimeUtils.convertToUTC0Zoned( end )
                .format( LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XFORMATTER ) );
        params.put( "txn_type", 1 );
        params.put( "date_filter_type", 2 );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.set( "ag-code", gamePlatform.getAgent() );
        httpHeaders.set( "ag-token", gamePlatform.getMd5() );
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        String url = gamePlatform.getApiUrl() + "/transaction/lists";

        Map<String, Object> resultMap = this.sendPostMap( url, requestEntity );

        /*log.warn( "url:{} - x:{} - request:{} - result:{}", url, json, JsonUtil.object2Json( requestMap ),
                JsonUtil.object2Json( resultMap ) );*/
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "1".equals( resultMap.getOrDefault( "status", "-1" ).toString() ) ) {
                LocalDateTime now        = LocalDateTime.now();
                LocalDateTime cutoffTime = now.minusMinutes( 3 );
                if ( end.isBefore( cutoffTime ) || end.isEqual( cutoffTime ) ) {
                    // end在3分钟前，保留原始end
                    gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                } else {
                    // end仍在最近3分钟范围内，使用cutoffTime
                    gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( now.minusMinutes( 2 ) ) ) );
                }
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                return ( List<Object> ) dataMap.getOrDefault( "bets", new ArrayList<>() );
            } else {
                log.error( url + " ::: " + JsonUtil.object2Json( requestEntity ) + " ::: " + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        if ( !"completed".equals( String.valueOf( remoteGameDatum.get( "type" ) ) ) ) {
            return null;
        }
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "txn_id" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "round_id" ) ) );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "username" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "table_id" ) ) );
        String bet = String.valueOf( remoteGameDatum.get( "stake" ) );
        gameDataRecord.setCellScore( bet );
        gameDataRecord.setAllBet( bet );
        String payout = String.valueOf( remoteGameDatum.get( "payout" ) );
        gameDataRecord.setProfit( new BigDecimal( payout ).subtract( new BigDecimal( bet ) ).stripTrailingZeros()
                .toPlainString() );
        String startTime = remoteGameDatum.get( "created_at" ).toString();
        LocalDateTime startTimeLocal = LocalDateTimeUtils.convertUTC0ToDefault( startTime,
                LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS_FORMATTER );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( startTimeLocal ) );
        String endTime = remoteGameDatum.get( "settled_at" ).toString();
        LocalDateTime endTimeLocal = LocalDateTimeUtils.convertUTC0ToDefault( endTime,
                LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS_FORMATTER );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( endTimeLocal ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
