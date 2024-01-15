package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Repository( value = ConstantsGame.CG + "GamePullProcessor" )
public class GamePullDockCG extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 4 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        String startTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC0( start ),
                LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER );
        String endTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertToUTC0( end ),
                LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put( "startTime", startTime );
        data.put( "endTime", endTime );
        data.put( "method", "data" );
        data.put( "removeComma", "True" );

        final Map<String, Object> resultMap = execute( gamePlatform, data, "/client/api/getGameRecord.php" );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( isSuccess( resultMap ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                return ( List<Object> ) resultMap.getOrDefault( "data", Collections.EMPTY_LIST );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "SerialNumber" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        String account  = String.valueOf( remoteGameDatum.get( "ThirdPartyAccount" ) ).toLowerCase();
        String agent    = account.substring( 0, account.lastIndexOf( "m" ) );
        String memberId = agent + "_" + account.substring( account.lastIndexOf( "m" ) ).toUpperCase();
        gameDataRecord.setAccount( memberId );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "GameType" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        String startTime = remoteGameDatum.get( "LogTime" ).toString();
        LocalDateTime startTimeLocal = LocalDateTimeUtils.convertUTC0ToDefault( startTime,
                LocalDateTimeUtils.YYYY_MM_DD_T_HH_MM_SSS_XXXFORMATTER );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( startTimeLocal ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( LocalDateTime.now() ) );

        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "ValidBet" ) ) );
        BigDecimal betAmount = new BigDecimal( String.valueOf( remoteGameDatum.get( "BetMoney" ) ) ).negate();
        gameDataRecord.setAllBet( betAmount.toString() );
        String payoffAmount = String.valueOf( remoteGameDatum.get( "MoneyWin" ) );
        gameDataRecord.setProfit( new BigDecimal( payoffAmount ).subtract( betAmount ).toString() );
        return gameDataRecord;
    }

    private Map<String, Object> execute( GamePlatform gamePlatform, final Map<String, Object> data, final String endpoint ) {
        final LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put( "channelId", gamePlatform.getAgent() );
        params.put( "data", getEncryptedData( JsonUtil.object2Json( data ), gamePlatform ) );

        String body = params.keySet().stream().map( key -> key + "=" + params.get( key ) ).collect( Collectors.joining( "&" ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.TEXT_PLAIN );
        HttpEntity<String> requestEntity = new HttpEntity<>( body, httpHeaders );

        String url = gamePlatform.getApiUrl() + endpoint;

        log.warn( url + " ::: " + body );

        final String response = restTemplate.exchange( url, HttpMethod.POST, requestEntity, String.class ).getBody();
        return JsonUtil.json2Object( getDecryptedResponse( response, gamePlatform ), Map.class );
    }

    private String getEncryptedData( final String content, GamePlatform gamePlatform ) {
        try {
            return AESCoder.encryptByKeyIv7Padding( content, gamePlatform.getMd5(), gamePlatform.getDes() );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private String getDecryptedResponse( final String response, GamePlatform gamePlatform ) {
        try {
            return AESCoder.decryptByKeyIv7Padding( response, gamePlatform.getMd5(), gamePlatform.getDes() );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private boolean isSuccess( final Map<String, Object> resultMap ) {
        return "0".equals( resultMap.getOrDefault( "errorCode", "" ).toString() );
    }
}
