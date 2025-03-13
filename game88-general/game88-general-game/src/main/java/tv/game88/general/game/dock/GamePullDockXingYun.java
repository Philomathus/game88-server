package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.XINGYUN + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockXingYun extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        long startTime = LocalDateTimeUtils.localDateToTimestamp( start );
        long endTime   = LocalDateTimeUtils.localDateToTimestamp( end );

        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "platformno", gamePlatform.getAgent() );
        params.put( "requesttime", System.currentTimeMillis() / 1000 );
        params.put( "starttime", startTime / 1000 );
        params.put( "endtime", endTime / 1000 );
        params.put( "page", 1 );
        params.put( "pagesize", 2000 );
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign  = DigestUtils.md5Hex( sb + "key=" + gamePlatform.getMd5() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( sb + "sign=" + sign.toUpperCase(), gamePlatform.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "platformno", gamePlatform.getAgent() );
        requestMap.put( "parameter", param );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        String url = gamePlatform.getApiUrl() + "/Game/pullRoundRecord";

        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.get( "code" ).toString();
            if ( "0".equals( code ) || "30007".equals( code ) ) {
                gamePlatform.setVersionValue( String.valueOf( endTime ) );
                return ( List<Object> ) resultMap.getOrDefault( "result", new ArrayList<>() );
            }
        }
        log.error( url + ":::" + JsonUtil.object2Json( resultMap ) );
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "recordid" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( String.valueOf( remoteGameDatum.get( "recordid" ) ) );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "username" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameid" ) ) );
        gameDataRecord.setCellScore( String.valueOf( remoteGameDatum.get( "effectivebet" ) ) );
        gameDataRecord.setAllBet( String.valueOf( remoteGameDatum.get( "totalbet" ) ) );
        gameDataRecord.setProfit( String.valueOf( remoteGameDatum.get( "losewincoin" ) ) );
        gameDataRecord.setRevenue( String.valueOf( remoteGameDatum.get( "winextract" ) ) );
        gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "tableno" ) ) );
        gameDataRecord.setGameStartTime( String.valueOf( remoteGameDatum.get( "starttime" ) ) );
        gameDataRecord.setGameEndTime( String.valueOf( remoteGameDatum.get( "endtime" ) ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        gameDataRecord.setDetail( String.valueOf( remoteGameDatum.get( "showpage" ) ) );
        return gameDataRecord;
    }
}
