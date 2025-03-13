package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;

@Log4j2
@Repository ( value = ConstantsGame.BOLE + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockBoLe extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是6分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 6 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        long startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long endTime   = LocalDateTimeUtils.localDateToTimestamp( end );

        Map<String, Object> params = new HashMap<>();
        params.put( "AccessKeyId", gamePlatform.getDes() );
        long time = System.currentTimeMillis() / 1000;
        params.put( "Timestamp", time );
        String nonce = UUID.randomUUID().toString();
        params.put( "Nonce", nonce );
        params.put( "Sign", DigestUtils.sha1Hex( gamePlatform.getMd5() + nonce + time ) );

        params.put( "start_time", startTime / 1000 );
        params.put( "end_time", endTime / 1000 );
        params.put( "page", 1 );
        params.put( "page_size", 10000 );

        String url = gamePlatform.getApiUrl() + "/v1/game/get_all_record_list";

        Map<String, Object> resultMap = this.sendPostMap( url, packageForm( params ) );

        // log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> respMsgMap = ( Map<String, Object> ) resultMap.getOrDefault( "resp_msg", new HashMap<>() );
            if ( "200".equals( respMsgMap.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( endTime ) );

                Map<String, Object> respDataMap = ( Map<String, Object> ) resultMap.getOrDefault( "resp_data", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( respDataMap ) ) {
                    return ( List<Object> ) respDataMap.getOrDefault( "data", new ArrayList<Map<String,
                            Object>>() );
                }
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
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "sn" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "player_account" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_code" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "bet_num_valid" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "bet_num" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "gain_gold" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "report_id" ) ) );
        long gameStartTime = Long.parseLong( remoteGameDatum.get( "start_time" ) + "000" );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( LocalDateTimeUtils.getDateTimeFromTimestamp( gameStartTime ) ) );
        long gameEndTime = Long.parseLong( remoteGameDatum.get( "end_time" ) + "000" );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( LocalDateTimeUtils.getDateTimeFromTimestamp( gameEndTime ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
