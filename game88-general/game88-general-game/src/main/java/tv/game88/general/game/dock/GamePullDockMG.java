package tv.game88.general.game.dock;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.MG + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockMG extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {

        String url = gamePlatform.getApiUrl() + gamePlatform.getAgent() + "/bets?limit=1000";
        if ( !StringUtils.isEmpty( gamePlatform.getVersionValue() ) ) {
            url = url.concat( "&startingAfter=" ).concat( gamePlatform.getVersionValue() );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer " + getToken( gamePlatform ) );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( headers );

        String resultStr = restTemplate.execute( url, HttpMethod.GET, restTemplate.httpEntityCallback( requestEntity ),
                response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return text;
        } );

        if ( StringUtils.isNotBlank( resultStr ) ) {
            List<Object> resultMap = JsonUtil.json2Array( resultStr, new TypeReference<>() {} );
            if ( !CollectionUtils.isEmpty( resultMap ) ) {
                Map obj = ( Map ) resultMap.get( resultMap.size() - 1 );
                gamePlatform.setVersionValue( obj.get( "betUID" ).toString() );

                return resultMap;
            } else {
                log.warn( url + "::" + resultStr );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "betUID" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        // gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "betUID" ) ) );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "playerId" ) ) );
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameCode" ) ) );
        String     betAmount     = String.valueOf( remoteGameDatum.get( "betAmount" ) );
        BigDecimal betAmountDeci = new BigDecimal( betAmount.equals( "0" ) ? "0" : betAmount );
        String     bet           = betAmountDeci.setScale( 2, RoundingMode.HALF_UP ).toString();
        gameDataRecord.setCellScore( bet );
        gameDataRecord.setAllBet( bet );
        String payoutAmount = String.valueOf( remoteGameDatum.get( "payoutAmount" ) );
        String profit = new BigDecimal( payoutAmount.equals( "0" ) ? "0" : payoutAmount ).subtract( betAmountDeci )
                .setScale( 2, RoundingMode.HALF_UP ).toString();
        gameDataRecord.setProfit( profit );
        String gameStartTime = String.valueOf( remoteGameDatum.get( "gameStartTimeUTC" ) ).substring( 0, 19 );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( LocalDateTimeUtils.convertUTC0ToDefault( gameStartTime,
                LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER ) ) );
        String gameEndTime = String.valueOf( remoteGameDatum.get( "gameEndTimeUTC" ) ).substring( 0, 19 );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( LocalDateTimeUtils.convertUTC0ToDefault( gameEndTime,
                LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }

    public String getToken( GamePlatform gamePlatform ) {
        if ( !redisUtils.exists( Constants.GAME_TOKEN_PREX + gamePlatform.getId() ) ) {
            Map<String, Object> params = new HashMap<>();
            params.put( "client_id", gamePlatform.getAgent() );
            params.put( "client_secret", gamePlatform.getMd5() );
            params.put( "grant_type", "client_credentials" );

            Map<String, Object> resultMap = this.sendPostMap( gamePlatform.getRecordUrl(), packageForm( params ) );
            Object              obj       = resultMap.get( "access_token" );
            String              token     = obj == null ? null : obj.toString();
            if ( StringUtils.isBlank( token ) ) {
                throw new BusinessException( gamePlatform.getName() + " - 获取token失败" );
            }
            redisUtils.strSet( Constants.GAME_TOKEN_PREX + gamePlatform.getId(), token, Duration.ofMinutes( 50 ) );
            return token;
        } else {
            return redisUtils.strGet( Constants.GAME_TOKEN_PREX + gamePlatform.getId() );
        }
    }
}
