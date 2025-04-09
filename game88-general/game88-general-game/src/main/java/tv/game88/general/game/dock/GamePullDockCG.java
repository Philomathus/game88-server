package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.CG + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockCG extends AbstractGamePull {

    private static final Map<String, BigDecimal> RATE_MAP = Map.of( "IDR", new BigDecimal( 1000 ), "INR", BigDecimal.ONE );

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 4 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        String startTime = LocalDateTimeUtils.RFC3339_FORMATTER.format( LocalDateTimeUtils.convertToUTC7( start )
                .atZone( ZoneId.of( "UTC+7" ) ) );
        String endTime = LocalDateTimeUtils.RFC3339_FORMATTER.format( LocalDateTimeUtils.convertToUTC7( end )
                .atZone( ZoneId.of( "UTC+7" ) ) );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put( "startTime", startTime );
        data.put( "endTime", endTime );
        data.put( "method", "data" );
        data.put( "removeComma", "True" );

        final Map<String, Object> resultMap = execute( gamePlatform, data, "/client/api/getGameRecord.php" );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( isSuccess( resultMap ) ) {

                List<Object> objectList = ( List<Object> ) resultMap.getOrDefault( "data", Collections.EMPTY_LIST );
                if ( !CollectionUtils.isEmpty( objectList ) ) {
                    Map<String, Object> last    = ( Map<String, Object> ) objectList.getLast();
                    String              logTime = last.get( "LogTime" ).toString();
                    LocalDateTime startTimeLocal = LocalDateTimeUtils.convertUTC7ToDefault( logTime,
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME );
                    // 从游戏记录最后时间开始查询
                    gamePlatform.setVersionValue( ( LocalDateTimeUtils.localDateToTimestamp( startTimeLocal ) + 1000 ) + "" );
                } else {
                    // 状态正常,无论是否有数据,从结束时间开始查询
                    gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                }
                return objectList;
            } else {
                log.error( JsonUtil.object2Json( resultMap ) );
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
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "ThirdPartyAccount" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "GameType" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        String startTime = remoteGameDatum.get( "LogTime" ).toString();
        LocalDateTime startTimeLocal = LocalDateTimeUtils.parseLocalDateTime( startTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( startTimeLocal ) );
        gameDataRecord.setGameEndTime( gameDataRecord.getGameStartTime() );

        BigDecimal RATE = BigDecimal.ONE;
        if ( accounts[ 0 ].startsWith( "99" ) ) {
            RATE = RATE_MAP.get( "IDR" );
        }

        BigDecimal validBet = new BigDecimal( String.valueOf( remoteGameDatum.get( "ValidBet" ) ) ).multiply( RATE );
        gameDataRecord.setCellScore( validBet.toString() );
        BigDecimal betMoney = new BigDecimal( String.valueOf( remoteGameDatum.get( "BetMoney" ) ) ).multiply( RATE );
        gameDataRecord.setAllBet( betMoney.toString() );
        BigDecimal payoffAmount = new BigDecimal( String.valueOf( remoteGameDatum.get( "MoneyWin" ) ) ).multiply( RATE );
        gameDataRecord.setProfit( payoffAmount.subtract( validBet ).toString() );
        return gameDataRecord;
    }

    private Map<String, Object> execute( GamePlatform gamePlatform, final Map<String, Object> data, final String endpoint ) {
        String content = JsonUtil.object2Json( data );
        log.warn( "加密前参数:{}", content );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "channelId", gamePlatform.getAgent() );
        params.add( "data", getEncryptedData( content, gamePlatform ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, httpHeaders );

        String url = gamePlatform.getRecordUrl() + endpoint;

        // log.warn( url + " ::: " + JsonUtil.object2Json( params ) );

        final String response = restTemplate.exchange( url, HttpMethod.POST, requestEntity, String.class ).getBody();

        String decryptedResponse = getDecryptedResponse( response, gamePlatform );
        return JsonUtil.json2Object( decryptedResponse, Map.class );
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
