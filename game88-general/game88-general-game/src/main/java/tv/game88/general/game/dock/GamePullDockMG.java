package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.api.service.GamePlatformService;
import tv.game88.general.game.base.AbstractGamePull;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.MG + "GamePullProcessor" )
public class GamePullDockMG extends AbstractGamePull {

    @Resource
    private GamePlatformService gamePlatformService;

    @Override
    public List<Map<String, Object>> requestRemoteGameData( GamePlatform gamePlatform ) {

        String url = gamePlatform.getRecordUrl() + "/agents/" + gamePlatform.getAgent() + "/bets?";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "limit", "20000" );
        params.add( "channel", "true" );

        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer " + getToken( gamePlatform ) );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );

        ResponseEntity<Map> responseGameResult = restTemplate.exchange( url, HttpMethod.GET, requestEntity, Map.class );
        if ( responseGameResult.getStatusCode().is2xxSuccessful() ) {
            List<Map<String, Object>> resultMap = ( List<Map<String, Object>> ) responseGameResult.getBody();

            if ( ! CollectionUtils.isEmpty( resultMap ) ) {
                gamePlatform.setVersionValue( String.valueOf( System.currentTimeMillis() ) );
                return resultMap;
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Map<String, Object> remoteGameDatum, GamePlatform gamePlatform ) {
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId(               String.valueOf( remoteGameDatum.get( "gameCode" ) ) );

        String logId = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( logId );
        gameDataRecord.setGameRound(            String.valueOf( remoteGameDatum.get( "betUID" ) ) );
        gameDataRecord.setAccount(              String.valueOf( remoteGameDatum.get( "playerId" ) ) );
        gameDataRecord.setKindId(               String.valueOf( remoteGameDatum.get( "platform" ) ) );
        gameDataRecord.setCellScore(            String.valueOf( remoteGameDatum.get( "betAmount" ) ) );
        gameDataRecord.setAllBet(               String.valueOf( remoteGameDatum.get( "PCA" ) ) );
        gameDataRecord.setProfit(               String.valueOf( remoteGameDatum.get( "payoutAmount" ) ) );
//        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "seat_id" ) ) );
        gameDataRecord.setGameStartTime(        String.valueOf( remoteGameDatum.get( "gameStartTimeUTC" ) ) );
        gameDataRecord.setGameEndTime(          String.valueOf( remoteGameDatum.get( "gameEndTimeUTC" ) ) );
        gameDataRecord.setAgent(                String.valueOf( remoteGameDatum.get( "channel" ) ) );
        gameDataRecord.setGameAgent(            gamePlatform.getAgent() );
        gameDataRecord.setPlatformId(           gamePlatform.getId() );
        return gameDataRecord;
    }

    public String getToken( GamePlatform gamePlatform ) {

        String retToken = "";

        if ( ! redisUtils.exists( "token:" + gamePlatform.getId() ) ) {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add( "client_id", gamePlatform.getAgent() );
            params.add( "client_secret", gamePlatform.getMd5() );
            params.add( "grant_type", "client_credentials" );
            headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>( params, headers );

            Map<String, Object> resultMap = restTemplate.postForObject( gamePlatform.getApiUrl(), httpEntity, Map.class );
            Object obj = resultMap.get( "access_token" );
            retToken = obj == null ? null : obj.toString();
            if ( StringUtils.isBlank( retToken ) ) {
                throw new BusinessException( gamePlatform.getGameCategory().getDes() + " - 获取token失败" );
            }
            redisUtils.strSet( "token:" + gamePlatform.getId(), retToken, Duration.ofMinutes( 50 ) );
        } else {
            retToken = redisUtils.strGet( "token:" + gamePlatform.getId() );
        }

        return retToken;
    }
}
