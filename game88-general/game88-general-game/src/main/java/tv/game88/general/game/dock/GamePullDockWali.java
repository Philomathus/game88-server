package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.*;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.WALI + "GamePullProcessor" )
public class GamePullDockWali extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime from = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( from.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime until = from.plusMinutes( 1 );

        String startTime = LocalDateTimeUtils.format( from, LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER );
        String endTime   = LocalDateTimeUtils.format( until, LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER );

        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "from", startTime );
        paramMap.put( "until", endTime );
        paramMap.put( "detail", "2" );

        String unixTimeSeconds = String.valueOf( System.currentTimeMillis() / 1000 );
        String params;
        try {
            params = AESCoder.encryptByKey( assembleParameters( paramMap ), gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl( gamePlatform.getApiUrl() ).path( "/getRecordV2" )
                                                          .queryParam( "a", gamePlatform.getLinecode() )
                                                          .queryParam( "t", unixTimeSeconds )
                                                          .queryParam( "p", UriUtils.encode( params, StandardCharsets.UTF_8 ) )
                                                          .queryParam( "k", DigestUtils.md5Hex(
                                                                  params + unixTimeSeconds + gamePlatform.getMd5() ) )
                                                          .build( true );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( StringUtils.isBlank( code ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( until ) ) );
                return ( List<Object> ) resultMap.getOrDefault( "list", new ArrayList<>() );
            } else {
                log.error( JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    private static String assembleParameters( Map<String, ?> paramMap ) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "gameId" ) ) );
        String id = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );

        gameDataRecord.setId( id );
        //        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "" ) ) );
        gameDataRecord.setAccount( String.valueOf( remoteGameDatum.get( "uid" ) ) );
        //        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "" ) ) );
        gameDataRecord.setCellScore( fenToYuan( String.valueOf( remoteGameDatum.get( "bet" ) ) ) );
        gameDataRecord.setAllBet( fenToYuan( String.valueOf( remoteGameDatum.get( "validBet" ) ) ) );
        gameDataRecord.setProfit( fenToYuan( String.valueOf( remoteGameDatum.get( "profit" ) ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "game" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "gameStartTime" ) ) );
        //        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "" ) ) );
        //        gameDataRecord.setAgent( String.valueOf( remoteGameDatum.get( "" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }

    private String fenToYuan( String money ) {
        return new BigDecimal( money ).divide( new BigDecimal( 100 ), 2, RoundingMode.HALF_UP ).toString();
    }
}
