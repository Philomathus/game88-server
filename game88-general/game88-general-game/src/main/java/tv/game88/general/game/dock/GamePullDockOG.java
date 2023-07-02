package tv.game88.general.game.dock;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.dto.RspOGData;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository ( value = ConstantsGame.OG + "GamePullProcessor" )
public class GamePullDockOG extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "Operator", gamePlatform.getDes() );
        requestMap.put( "Key", gamePlatform.getMd5() );
        requestMap.put( "Provider", "ogplus" );
        requestMap.put( "SDate", LocalDateTimeUtils.format( start ) );
        requestMap.put( "EDate", LocalDateTimeUtils.format( end ) );

        String res = restTemplate.execute( gamePlatform.getRecordUrl(), HttpMethod.POST,
                restTemplate.httpEntityCallback( packageForm( requestMap ) ), response -> {
                    InputStream bodyStream = response.getBody();
                    String      text;
                    try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                        text = IOUtils.toString( reader );
                    }
                    return text;
                } );

        // log.warn( res );

        if ( res == null ) {
            return null;
        }

        gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
        List<RspOGData> list = JsonUtil.json2Array( res, new TypeReference<>() {
        } );
        return new ArrayList<>( list );
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        RspOGData      remoteGameDatum = ( RspOGData ) object;
        GameDataRecord gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( remoteGameDatum.getBettingcode() );
        gameDataRecord.setId( this.createRecordId( gamePlatform, remoteGameDatum.getId() ) );
        gameDataRecord.setGameRound( remoteGameDatum.getRoundno() );
        String   membername = remoteGameDatum.getMembername();
        String[] splitParam = membername.split( "_" );
        String agent = splitParam[ 1 ].toLowerCase();
        gameDataRecord.setAccount( agent + "_" + splitParam[ 2 ].toUpperCase() );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setKindId( remoteGameDatum.getGamename() );
        gameDataRecord.setTableId( remoteGameDatum.getGameid() );
        gameDataRecord.setChairId( remoteGameDatum.getRoundno() );
        gameDataRecord.setCellScore( remoteGameDatum.getValidbet() );
        gameDataRecord.setAllBet( remoteGameDatum.getBettingamount() );
        gameDataRecord.setProfit( remoteGameDatum.getWinloseamount() );
        gameDataRecord.setGameStartTime( remoteGameDatum.getBettingdate() );
        gameDataRecord.setGameEndTime( remoteGameDatum.getBettingdate() );//og 注单延迟大,结束时间可能在5分钟之前
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
