package tv.game88.pay.api.base;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.mapper.MemberWithdrawDetailMapper;
import tv.game88.pay.api.mapper.PayAgentLogMapper;
import tv.game88.pay.api.mapper.PayAgentPlatformMapper;
import tv.game88.pay.api.service.PayAgentService;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

@Log4j2
public abstract class AbstractPayAgent implements BasePayAgent {
    @Resource
    protected PayCacheUtil               payCacheUtil;
    @Resource
    protected PayAgentPlatformMapper     payAgentPlatformMapper;
    @Resource
    protected MemberWithdrawDetailMapper withdrawDetailMapper;
    @Resource
    protected PayAgentLogMapper          payAgentLogMapper;
    @Resource
    protected RestTemplate               restTemplate;
    @Resource
    protected PayAgentService            payAgentService;
    @Resource
    protected ConfigEnvCacheUtil         configEnvCacheUtil;

    protected String assemblyUrl( Map<String, ?> bodyMap ) {
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }

    protected boolean checkWhiteIp( String platWhiteIpList, String realIp ) {
        if ( StringUtils.hasText( platWhiteIpList ) ) {
            Set<String> whiteIpSet = Sets.newHashSet( platWhiteIpList.split( "," ) );
            return !whiteIpSet.contains( realIp ) && !"0:0:0:0:0:0:0:1".equals( realIp );
        }
        return false;
    }

    @NotNull
    protected static HttpEntity<MultiValueMap<String, Object>> packageForm( Map<String, Object> params ) {
        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( params );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        return new HttpEntity<>( requestMap, httpHeaders );
    }

    @NotNull
    protected static HttpEntity<Map<String, Object>> packageJson( Map<String, Object> params ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        return new HttpEntity<>( params, httpHeaders );
    }

    protected Map<String, Object> sendPostMap( String url, HttpEntity<?> httpEntity, ReqPayAgent reqPayAgent ) {
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
            if ( reqPayAgent != null ) {
                reqPayAgent.setFailReason( e.getMessage() );
            }
        }
        return resultMap;
    }

    protected String sendPostString( String url, HttpEntity<?> httpEntity, ReqPayAgent reqPayAgent ) {
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
            if ( reqPayAgent != null ) {
                reqPayAgent.setFailReason( e.getMessage() );
            }
        }
        return resultStr;
    }

    protected String sendGetString( String url, ReqPayAgent reqPayAgent ) {
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
            if ( reqPayAgent != null ) {
                reqPayAgent.setFailReason( e.getMessage() );
            }
        }
        return resultStr;
    }

    protected Map<String, Object> sendGetMap( String url, ReqPayAgent reqPayAgent ) {
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
            if ( reqPayAgent != null ) {
                reqPayAgent.setFailReason( e.getMessage() );
            }
        }
        return resultMap;
    }
}
