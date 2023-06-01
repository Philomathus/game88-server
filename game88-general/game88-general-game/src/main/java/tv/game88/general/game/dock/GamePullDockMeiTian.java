package tv.game88.general.game.dock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
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
@Repository( value = ConstantsGame.MEITIAN + "GamePullProcessor" )
public class GamePullDockMeiTian extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData(GamePlatform gamePlatform) {
        LocalDateTime startTime = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        LocalDateTime endTime   = startTime.plusMinutes( 1 );

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put( "recordID", 0 );
        dataMap.put( "gameType", gamePlatform.getGameCategory() );
        dataMap.put( "startTime", LocalDateTimeUtils.format( startTime, LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        dataMap.put( "endTime",  LocalDateTimeUtils.format( endTime, LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        dataMap.put( "currency", "CNY" );

        ObjectMapper objectMapper = new ObjectMapper();
        String data = "";
        try {
            data = objectMapper.writeValueAsString(dataMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String url = String.format( gamePlatform.getRecordUrl() + "queryMerchantGameRecord2/%s/%s", gamePlatform.getAgent(), Base64Utils.encodeToUrlSafeString( data.getBytes() ) );
        Map<String, Object> resultMap = this.sendPostMap( url, packageJson( Collections.emptyMap() ) );

        if ( !CollectionUtils.isEmpty( resultMap ) && "1".equals(resultMap.getOrDefault("resultCode", "").toString() ) ) {
            return ( List<Object> ) resultMap.getOrDefault( "transList", Collections.emptyMap() );
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord gameDataRecord = new GameDataRecord();
        String logId      = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( logId );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "rowID" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "playerName" ) ) );
        gameDataRecord.setGameStartTime( remoteGameDatum.get( "gameDate" ).toString() );
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameCode" ) ) );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameType" ) ) );
        gameDataRecord.setAllBet( fenToYuan( String.valueOf( remoteGameDatum.get( "betAmount" ) ) ) );
        gameDataRecord.setRevenue( fenToYuan( String.valueOf( remoteGameDatum.get( "winAmount" ) ) ) );
        gameDataRecord.setCellScore( fenToYuan( String.valueOf( remoteGameDatum.get( "commissionable" ) ) ) );
        gameDataRecord.setProfit( fenToYuan( String.valueOf( remoteGameDatum.get( "income" ) ) ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "roundId" ) ) );

        return gameDataRecord;
    }
    private String fenToYuan( String money ) {
        return new BigDecimal( money ).divide( new BigDecimal( 100 ), 2, RoundingMode.HALF_UP ).toString();
    }
}
