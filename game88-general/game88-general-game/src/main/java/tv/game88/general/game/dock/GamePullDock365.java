package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Log4j2
@Repository( value = ConstantsGame.GAMING_365 + "GamePullProcessor" )
public class GamePullDock365 extends AbstractGamePull {
    @Override
    public List< Object > requestRemoteGameData(GamePlatform gamePlatform) {
        long startTime = Long.parseLong( gamePlatform.getVersionValue() );
        long endTime = startTime + 60000;

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "cert", gamePlatform.getLinecode() );
        requestMap.put( "extension1", gamePlatform.getAgent() );
        requestMap.put( "st",  startTime );
        requestMap.put( "et", endTime );

        String url = gamePlatform.getRecordUrl() + "getTransactionsByPayTime";
        Map<String, Object> resultMap = this.sendPostMap( url, packageJson( requestMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) && "1".equals( resultMap.getOrDefault("status", "").toString() ) ) {
            return ( List< Object > ) resultMap.getOrDefault( "transactions", Collections.emptyMap() );
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord gameDataRecord = new GameDataRecord();
        String logId      = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );
        String createTime = remoteGameDatum.get( "createTime" ).toString();

        gameDataRecord.setId( logId );
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameNumber" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "userId" ) ) );
        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "extension1" ) ) );
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameId" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "chair" ) ) );
        gameDataRecord.setProfit( fenToYuan( String.valueOf( remoteGameDatum.get( "profit" ) ) ) );
        gameDataRecord.setCellScore( fenToYuan( String.valueOf( remoteGameDatum.get( "validbet" ) ) ) );
        gameDataRecord.setAllBet( fenToYuan( String.valueOf( remoteGameDatum.get( "betAmount" ) ) ) );
        gameDataRecord.setRevenue( fenToYuan( String.valueOf( remoteGameDatum.get( "payAmount" ) ) ) );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        gameDataRecord.setGameStartTime( createTime );
        return gameDataRecord;
    }
    private String fenToYuan( String money ) {
        return new BigDecimal( money ).divide( new BigDecimal( 100 ), 2, RoundingMode.HALF_UP ).toString();
    }
}
