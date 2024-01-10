package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.*;
import java.util.List;

@Log4j2
@Repository( value = ConstantsGame.PP + "GamePullProcessor" )
public class GamePullDockPP extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "login", gamePlatform.getAgent() );
        params.add( "password", gamePlatform.getMd5() );
        params.add( "timepoint", gamePlatform.getVersionValue() );
        params.add( "dataType", "RNG" );
        params.add( "options", "addBonusBetWin" );

        final String url = gamePlatform.getApiUrl() + "/IntegrationService/v3/DataFeeds/gamerounds/finished/";

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( url ).queryParams( params ).build( true );

        log.warn( uriComponents.toUriString() );

        String resultStr = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null )
                , response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return text;
        } );
        log.warn( resultStr );
        if ( StringUtils.isNotBlank( resultStr ) ) {
            //找到第一个换行符的位置
            int index = resultStr.indexOf( "\n" );
            //取出第一行数据
            String firstLine = resultStr.substring( 0, index );
            if ( StringUtils.isBlank( firstLine ) && !firstLine.contains( "=" ) ) {
                return null;
            } else {
                String time = firstLine.split( "=" )[ 1 ];
                gamePlatform.setVersionValue( time );
            }
            //删除第一行数据
            resultStr = resultStr.substring( index + 1 );

            try {
                CSVParser csvParser = new CSVParser( new StringReader( resultStr ), CSVFormat.DEFAULT );
                for ( CSVRecord csvRecord : csvParser ) {
                    // 遍历每一行的字段
                    for ( String field : csvRecord ) {
                        System.out.print( field + " " );
                    }
                    System.out.println(); // 换行
                }
            } catch ( IOException e ) {
                log.error( e.getMessage(), e );
            }
        }
        return null;
    }

    public static void main( String[] args ) {
        String str = """
                timepoint=1704365926870
                playerID,extPlayerID,gameID,playSessionID,parentSessionID,startDate,endDate,status,type,bet,win,currency,jackpot,bonusBet,bonusWin
                """;
        //找到第一个换行符的位置
        int index = str.indexOf( "\n" );
        //取出第一行数据
        String firstLine = str.substring( 0, index );
        if ( StringUtils.isBlank( firstLine ) && !firstLine.contains( "=" ) ) {
        } else {
            String time = firstLine.split( "=" )[ 1 ];
            System.out.println( time );
        }
        //删除第一行数据
        str = str.substring( index + 1 );
        System.out.println( str );
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        return null;
    }
}
