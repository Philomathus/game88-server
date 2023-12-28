package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Log4j2
@Repository( value = ConstantsGame.PP + "GamePullProcessor" )
public class GamePullDockPP extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "login", gamePlatform.getAgent() );
        params.put( "password", gamePlatform.getMd5() );
        params.put( "timepoint", gamePlatform.getVersionValue() );
        params.put( "dataType", "RNG" );
        params.put( "options", "addBonusBetWin" );

        final String url         = gamePlatform.getApiUrl() + "/DataFeeds/gamerounds/finished/";
        HttpHeaders  httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        httpHeaders.setCacheControl( "no-cache" );
        HttpEntity<String> requestEntity = new HttpEntity<>( keyValStringFormat( params ), httpHeaders );

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

        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {

        return null;
    }

    private String keyValStringFormat( final Map<String, Object> params ) {
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }
}
