package tv.game88.general.game.base;

import com.google.common.collect.HashBiMap;
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
import tv.game88.common.utils.RedisUtils;
import tv.game88.general.api.entity.GamePlatform;

import jakarta.annotation.Resource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public abstract class AbstractGamePull implements BaseGamePull {
    @Resource( name = "restUploadTemplate" )
    protected RestTemplate restTemplate;
    @Resource
    protected RedisUtils   redisUtils;

    protected static final Pattern GET_NUMBER = Pattern.compile( "\\d+$" );

    protected static final HashBiMap<String, String> profileBiMap = HashBiMap.create();

    static {
        profileBiMap.put( "dev", "0AA" );
        profileBiMap.put( "9900", "1AA" );
        profileBiMap.put( "99115", "1PK" );
        profileBiMap.put( "99116", "1BD" );
        profileBiMap.put( "99126", "1IN" );
        profileBiMap.put( "99136", "1VI" );
    }

    protected static String[] assemblyAccount( String account ) {
        account = account.toLowerCase();
        if ( account.startsWith( "77" ) ) {
            Matcher matcher = GET_NUMBER.matcher( account );
            if ( matcher.find() ) {
                String memberNum = matcher.group();
                String agent     = account.substring( 0, account.lastIndexOf( memberNum ) - 1 ).toLowerCase();
                String memberId  = agent + "_" + memberNum;
                return new String[] { agent, memberId };
            }
        } else if ( account.startsWith( "88" ) || account.startsWith( "99" ) ) {
            if ( account.contains( "_" ) ) {
                String[] split = account.split( "_" );
                return new String[] { split[ 0 ], split[ 0 ] + "_" + split[ 1 ].toUpperCase() };
            } else {
                if ( account.startsWith( "88ky" ) && !account.contains( "m" ) ) {
                    String memberId = account.replaceFirst( "88ky", "" );
                    return new String[] { "88ky", "88ky_" + memberId };
                } else {
                    String agent    = account.substring( 0, account.lastIndexOf( "m" ) );
                    String memberId = agent + "_" + account.substring( account.lastIndexOf( "m" ) ).toUpperCase();
                    return new String[] { agent, memberId };
                }
            }
        } else {
            account = account.toUpperCase();
            Matcher matcher = GET_NUMBER.matcher( account );
            if ( matcher.find() ) {
                String memberNum  = matcher.group();
                String agentValue = account.substring( 1, account.lastIndexOf( memberNum ) );
                String agent      = profileBiMap.inverse().get( agentValue );
                return new String[] { agent, agent + "_" + "M" + agentValue + memberNum };
            }
        }
        return null;
    }

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
        return sendGetMap( url, null );
    }

    protected Map<String, Object> sendGetMap( String url, HttpEntity<?> httpEntity ) {
        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( url, HttpMethod.GET, restTemplate.httpEntityCallback( httpEntity ), response -> {
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

    protected static List<Object> convertMapArrayToListMap( Map<String, List<Object>> originalMap, String existKey ) {
        List<Object> resultList = new ArrayList<>();
        List<Object> objects    = originalMap.get( existKey );
        for ( int i = 0; i < objects.size(); i++ ) {
            Map<String, Object> resultMap = new HashMap<>();
            for ( String key : originalMap.keySet() ) {
                List<Object> values = originalMap.get( key );
                resultMap.put( key, values.get( i ) );
            }
            resultList.add( resultMap );
        }
        return resultList;
    }

    /**
     * 获取属性名数组
     */
    protected static String[] getFiledName( Object o ) {
        Field[]  fields     = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[ fields.length ];
        for ( int i = 0; i < fields.length; i++ ) {
            fieldNames[ i ] = fields[ i ].getName();
        }
        return fieldNames;
    }

    /* 根据属性名获取属性值
     * */
    protected static Object getFieldValueByName( String fieldName, Object o ) {
        try {
            String firstLetter = fieldName.substring( 0, 1 ).toUpperCase();
            String getter      = "get" + firstLetter + fieldName.substring( 1 );
            Method method      = o.getClass().getMethod( getter );
            return method.invoke( o );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return null;
    }
}
