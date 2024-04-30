package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.DesCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.SGWIN + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockSGWin extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        String startTime = String.valueOf( LocalDateTimeUtils.localDateToTimestamp( start ) / 1000L );
        String endTime   = String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) / 1000 );

        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "ac", "9" );
        paramMap.put( "all", "1" );
        paramMap.put( "startTime", startTime );
        paramMap.put( "endTime", endTime );

        long   unixTime = System.currentTimeMillis();
        String params;
        try {
            params = DesCoder.encrypt( assembleParameters( paramMap ), gamePlatform.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl( gamePlatform.getRecordUrl() )
                                                          .queryParam( "agentId", gamePlatform.getAgent() )
                                                          .queryParam( "timestamp", unixTime )
                                                          .queryParam( "param", URLEncoder.encode( params,
                                                                  StandardCharsets.UTF_8 ) )
                                                          .queryParam( "sign", DigestUtils.md5Hex(
                                                                  gamePlatform.getAgent() + unixTime + gamePlatform.getMd5() ) )
                                                          .build( true );

        log.warn( uriComponents.toUriString() );

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
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( d ) && "0".equals( d.getOrDefault( "code", "-1" ).toString() ) ) {
                // 状态正常,无论是否有数据,从结束时间开始查询
                gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
                return ( List<Object> ) d.getOrDefault( "list", new ArrayList<>() );
            } else {
                log.error( uriComponents.toUriString() + ":::" + JsonUtil.object2Json( resultMap ) );
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
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "id" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "dealId" ) ) );
        String account = String.valueOf( remoteGameDatum.get( "userCode" ) );
        String agent   = account.split( "_" )[ 0 ];
        gameDataRecord.setAccount( account );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameId" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "effectBet" ) ).replaceAll( ",", "" ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "totalBet" ) ).replaceAll( ",", "" ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "winLost" ) ).replaceAll( ",", "" ) );
        gameDataRecord.setRevenue( String.valueOf( remoteGameDatum.get( "fee" ) ).replaceAll( ",", "" ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "deskId" ) ) );
        gameDataRecord.setChairId( String.valueOf( remoteGameDatum.get( "seatId" ) ) );
        long gameStartTime = Long.parseLong( remoteGameDatum.get( "openTime" ) + "000" );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( LocalDateTimeUtils.getDateTimeFromTimestamp( gameStartTime ) ) );
        long gameEndTime = Long.parseLong( remoteGameDatum.get( "endTime" ) + "000" );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( LocalDateTimeUtils.getDateTimeFromTimestamp( gameEndTime ) ) );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }

}
