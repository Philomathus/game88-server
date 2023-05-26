package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.RICH88 + "GamePullProcessor" )
public class GamePullDockRich88 extends AbstractGamePull {

    @Override
    public List<Map<String, Object>> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是1分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 1 ) ) ) {
            return null;
        }

        LocalDateTime end = start.plusMinutes( 1 );

        String startDateTime = LocalDateTimeUtils.format( start );
        String endDateTime   = LocalDateTimeUtils.format( end );

        String time   = String.valueOf( System.currentTimeMillis() );
        String params = String.format( "from=%s&to=%s", startDateTime, endDateTime );

        String apiKey = DigestUtils.sha256Hex( gamePlatform.getAgent() + gamePlatform.getMd5() + time );

        Map<String, Object> headerParams = new LinkedHashMap<>();
        headerParams.put( "api_key", apiKey );
        headerParams.put( "pf_id", gamePlatform.getAgent() );
        headerParams.put( "timestamp", time );

        HttpEntity<MultiValueMap<String, Object>> httpHeaders = this.packageForm(headerParams);

        String url = gamePlatform.getRecordUrl() + "/v2/platform/bet/records?" + params;

        Map<String, Object> resultMap = this.sendGetMap( url , httpHeaders);
        if ( ! CollectionUtils.isEmpty( resultMap ) ) {
            List<Map<String, Object>> d = ( List<Map<String, Object>> ) resultMap.getOrDefault( "data", new
                    ArrayList<Map<>>() );
            if ( !CollectionUtils.isEmpty( d ) && "0".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( endDateTime ) );
                return d;
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Map<String, Object> remoteGameDatum, GamePlatform gamePlatform ) {
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "game_code" ) ) );

        String logId   = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( logId );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "round_id" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "account" ) )  );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_code" ) ) );
        gameDataRecord.setCellScore(String.valueOf( remoteGameDatum.get( "bet_valid" ) )  );
        gameDataRecord.setAllBet(String.valueOf( remoteGameDatum.get( "bet" ) ) ) ;
        gameDataRecord.setProfit(String.valueOf( remoteGameDatum.get( "profit" ) ) );
//        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "" ) ) );
        gameDataRecord.setGameStartTime( remoteGameDatum.get( "round_start_at" ).toString() );
        gameDataRecord.setGameEndTime( remoteGameDatum.get( "round_end_at" ).toString() );
//        gameDataRecord.setAgent( agent );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
