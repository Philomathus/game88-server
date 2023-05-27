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
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.FG + "GamePullProcessor" )
public class GamePullDockFG extends AbstractGamePull {
    @Override
    public List<Map<String, Object>> requestRemoteGameData(GamePlatform gamePlatform) {
        String url = gamePlatform.getRecordUrl() + "v3/agent/log_by_page/gt/" + gamePlatform.getGameCategory().getType();

        Map<String, Object> resultMap = this.sendPostMap( url, packageJson( Collections.emptyMap() ) );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
            return ( List<Map<String, Object>> ) resultMap.getOrDefault( "data", new ArrayList<>() );
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult(Map<String, Object> remoteGameDatum, GamePlatform gamePlatform) {
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "game_id" ) ) );
        String logId   = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );
        String account = String.valueOf( remoteGameDatum.get( "player_name" ) ).toLowerCase(); //TODO: Not sure
        String createTime = remoteGameDatum.get( "time" ).toString();
        gameDataRecord.setId( logId );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "" ) ) ); //TODO: Not sure
        gameDataRecord.setAccount( account );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gt" ) ) );
        gameDataRecord.setCellScore( fenToYuan( String.valueOf( remoteGameDatum.get( "total_bets" ) ) ) );
        gameDataRecord.setAllBet( fenToYuan( String.valueOf( remoteGameDatum.get( "all_bets" ) ) ) );
        gameDataRecord.setProfit( fenToYuan( String.valueOf( remoteGameDatum.get( "all_wins" ) ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "scene_id" ) ) );
        gameDataRecord.setGameStartTime( createTime );
        gameDataRecord.setGameEndTime( createTime );
        gameDataRecord.setAgent( remoteGameDatum.get( "agent_uid" ).toString() );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
    private String fenToYuan( String money ) {
        return new BigDecimal( money ).divide( new BigDecimal( 100 ), 2, RoundingMode.HALF_UP ).toString();
    }
}
