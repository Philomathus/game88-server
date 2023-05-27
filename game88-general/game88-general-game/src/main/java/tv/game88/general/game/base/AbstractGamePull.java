package tv.game88.general.game.base;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.JsonUtil;
import tv.game88.general.api.entity.GamePlatform;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

@Log4j2
public abstract class AbstractGamePull implements BaseGamePull {
    @Resource
    protected RestTemplate restTemplate;

    protected String createRecordId( GamePlatform info, String tarId ) {
        return String.valueOf( info.getId() ).concat( "-" ).concat( tarId );
    }

    protected static HttpEntity<MultiValueMap<String, Object>> packageForm( Map<String, Object> params ) {
        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( params );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        return new HttpEntity<>( requestMap, httpHeaders );
    }

    protected static HttpEntity<Map<String, Object>> packageJson( Map<String, Object> params ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        return new HttpEntity<>( params, httpHeaders );
    }

    protected Map<String, Object> sendPostMap( String url, HttpEntity<?> httpEntity ) {
        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return resultMap;
    }

    protected String sendPostString( String url, HttpEntity<?> httpEntity ) {
        String resultStr = null;
        try {
            resultStr = restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return text;
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return resultStr;
    }

    protected String sendGetString( String url ) {
        String resultStr = null;
        try {
            resultStr = restTemplate.execute( url, HttpMethod.GET, restTemplate.httpEntityCallback( null ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return text;
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return resultStr;
    }

    protected Map<String, Object> sendGetMap( String url ) {
        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( url, HttpMethod.GET, restTemplate.httpEntityCallback( null ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return resultMap;
    }

    protected String assemblyUrl( Map<String, ?> bodyMap ) {
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }
}
