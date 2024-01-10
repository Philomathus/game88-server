package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

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
