package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.MEITIAN + "GamePullProcessor" )
public class GamePullDockMeiTian extends AbstractGamePull {
    private static final String QUERY_RECORD_2 = "/services/dg/player/queryMerchantGameRecord2";

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        Map<String, String> rawData = new LinkedHashMap<>();
        rawData.put( "recordID", gamePlatform.getVersionValue() );
        String rawDataStr = JsonUtil.object2Json( rawData );

        String url = gamePlatform.getApiUrl() + QUERY_RECORD_2 + "/" + gamePlatform.getAgent() + "/" + Base64
                .getEncoder()
                .encodeToString( rawDataStr.getBytes() );

        Map<String, Object> resultMap = restTemplate.postForObject( url, null, Map.class );

        //log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( StringUtils.equals( "1", resultMap.getOrDefault( "resultCode", "-2" ).toString() ) ) {
                List<Object> transList = ( List<Object> ) resultMap.get( "transList" );
                if ( !CollectionUtils.isEmpty( transList ) ) {
                    Map<String, Object> o = ( Map<String, Object> ) transList.get( transList.size() - 1 );
                    gamePlatform.setVersionValue( String.valueOf( o.get( "recordID" ) ) );
                }
                return transList;
            } else {
                log.warn( url + "::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "rowID" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "period" ) ) );
        String account = String.valueOf( remoteGameDatum.get( "playerName" ) ).toUpperCase();
        if ( account.contains( "DEMO" ) ) {
            return null;
        }
        if ( account.contains( "WWUFA3TEST" ) ) {
            return null;
        }
        String[] split = account.split( "_" );
        String   agent = split[ 0 ].toLowerCase();
        gameDataRecord.setAccount( agent + "_" + split[ 1 ] );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameCode" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "betAmount" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "commissionable" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "income" ) ) );
        String gameDate = String.valueOf( remoteGameDatum.get( "gameDate" ) );
        gameDataRecord.setGameEndTime( gameDate );
        gameDataRecord.setGameStartTime( gameDate );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
