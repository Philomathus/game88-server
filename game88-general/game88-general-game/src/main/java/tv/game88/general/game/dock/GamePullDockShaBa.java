package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.SHABA + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockShaBa extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add( "vendor_id", gamePlatform.getAgent() );
        map.add( "version_key", gamePlatform.getVersionValue() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>( map, httpHeaders );

        String              url       = gamePlatform.getApiUrl() + "/GetBetDetail";
        Map<String, Object> resultMap = restTemplate.postForObject( url, httpEntity, Map.class );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataMap    = ( Map<String, Object> ) resultMap.getOrDefault( "Data", new HashMap<>() );
            String              resultCode = resultMap.getOrDefault( "error_code", -1 ).toString();
            if ( "0".equals( resultCode ) && !CollectionUtils.isEmpty( dataMap ) ) {
                gamePlatform.setVersionValue( dataMap.get( "last_version_key" ).toString() );

                List<Object> resultList = new ArrayList<>();

                List<Map<String, Object>> BetDetails    = ( List<Map<String, Object>> ) dataMap.get( "BetDetails" );
                List<Map<String, Object>> BetNumDetails = ( List<Map<String, Object>> ) dataMap.get( "BetNumberDetails" );
                List<Map<String, Object>> BetVSDetails  = ( List<Map<String, Object>> ) dataMap.get( "BetVirtualSportDetails" );

                if ( !CollectionUtils.isEmpty( BetDetails ) ) {
                    resultList.addAll( BetDetails );
                }
                if ( !CollectionUtils.isEmpty( BetNumDetails ) ) {
                    resultList.addAll( BetNumDetails );
                }
                if ( !CollectionUtils.isEmpty( BetVSDetails ) ) {
                    resultList.addAll( BetVSDetails );
                }

                return resultList;
            } else {
                log.error( url + ":::" + JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        String              ticket_status   = String.valueOf( remoteGameDatum.get( "ticket_status" ) );
        if ( StringUtils.equals( "waiting", ticket_status ) || StringUtils.equals( "running", ticket_status ) ) {
            return null;
        }
        GameDataRecord gameDataRecord = new GameDataRecord();

        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "trans_id" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "trans_id" ) ) );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "vendor_member_id" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        Object sportType = remoteGameDatum.get( "sport_type" );
        if ( sportType == null ) {
            List<Map<String, Object>> ParlayDatas = ( List<Map<String, Object>> ) remoteGameDatum.get( "ParlayData" );
            if ( !CollectionUtils.isEmpty( ParlayDatas ) ) {
                Map<String, Object> ParlayData = ParlayDatas.getFirst();
                gameDataRecord.setKindId( String.valueOf( ParlayData.getOrDefault( "sport_type", "" ) ) );
            }
        } else {
            gameDataRecord.setKindId( String.valueOf( sportType ) );
        }
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "stake" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "stake" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "winlost_amount" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.getOrDefault( "match_id",
                remoteGameDatum.get( "league_id" ) ) ) );
        gameDataRecord.setChairId( String.valueOf( remoteGameDatum.getOrDefault( "away_id", remoteGameDatum.get( "team_id" ) ) ) );

        gameDataRecord.setPlatformId( gamePlatform.getId() );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );


        String transactionTimeStr = String.valueOf( remoteGameDatum.get( "transaction_time" ) );
        String settlementTimeStr  = String.valueOf( remoteGameDatum.get( "settlement_time" ) );
        if ( transactionTimeStr == null || transactionTimeStr.equals( "null" ) ) {
            return null;
        }
        LocalDateTime transactionTime = LocalDateTimeUtils.convertMeiDongToDefault( transactionTimeStr.substring( 0, 19 ),
                LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( transactionTime ) );
        if ( settlementTimeStr == null || settlementTimeStr.equals( "null" ) ) {
            gameDataRecord.setGameEndTime( gameDataRecord.getGameStartTime() );
        } else {
            LocalDateTime settlementTime = LocalDateTimeUtils.convertMeiDongToDefault( settlementTimeStr.substring( 0, 19 ),
                    LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER );
            gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( settlementTime ) );
        }
        return gameDataRecord;
    }
}
