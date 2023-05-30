package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.JDB + "GamePullProcessor" )
public class GamePullDockJDB extends AbstractGamePull {

    @Override
    public List<Map<String, Object>> requestRemoteGameData( GamePlatform gamePlatform ) {
        long ts = System.currentTimeMillis();

        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        long startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long endTime = LocalDateTimeUtils.localDateToTimestamp( end );
        long intervalTime = endTime - ts;

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "action", intervalTime > 7200000L ?  "64" : "29");
        requestMap.put( "ts", String.valueOf( ts ) );
        requestMap.put( "parent", gamePlatform.getAgent() );
        requestMap.put( "starttime", LocalDateTimeUtils.format( start, LocalDateTimeUtils.DDMMYYYYHHMMSS_FORMATTER ) );
        requestMap.put( "endtime", LocalDateTimeUtils.format( end, LocalDateTimeUtils.DDMMYYYYHHMMSS_FORMATTER ) );
        requestMap.put( "gTypes", gamePlatform.getGameCategory().getType() );

        String url = gamePlatform.getRecordUrl() + "/apiRequest.do";

        Map<String, Object> resultMap = this.sendPostMap( url, packageJson( requestMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            List<Map<String, Object>> dataMapList = ( List<Map<String, Object>> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( dataMapList ) && "0000".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return dataMapList;
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Map<String, Object> remoteGameDatum, GamePlatform gamePlatform ) {
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "seqNo" ) ) );

        String logId = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( logId );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "roundSeqNo" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "playerId" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gType" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "bet" ) ) );
//        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "total" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "win" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "mtype" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "gameDate" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "lastModifyTime" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "mtype" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
