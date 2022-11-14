package tv.game88.pay.api.base;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.mapper.MemberRechargeOnlineMapper;
import tv.game88.pay.api.service.PayService;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
public abstract class AbstractPay implements BasePay {

    @Resource
    protected MemberRechargeOnlineMapper memberRechargeOnlineMapper;
    @Resource
    protected PayService                 payService;
    @Resource
    protected RestTemplate               restTemplate;
    @Resource
    protected ConfigEnvCacheUtil         configEnvCacheUtil;
    @Resource
    protected PayCacheUtil               payCacheUtil;

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;" + "]+[-A-Za-z0-9+&@#/%=~_|]" );

    protected String assemblyUrl( Map<String, ?> bodyMap ) {
        //字典顺序a----z
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }

    protected String assemblyReverseUrl( Map<String, ?> bodyMap ) {
        //字典反序z---a
        StringBuilder buffer = new StringBuilder();
        if ( CollectionUtils.isEmpty( bodyMap ) ) {
            return null;
        }
        List<String> keyList = new ArrayList<>( bodyMap.keySet() );
        Collections.reverse( keyList );
        for ( String key : keyList ) {
            buffer.append( key ).append( "=" ).append( bodyMap.get( key ) ).append( "&" );
        }
        return buffer.substring( 0, buffer.length() - 1 );
    }

    protected String assemblyUrl2( Map<String, ?> bodyMap ) {
        //字典顺序a----z
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( v ) );
        return sb.substring( 0, sb.length() );
    }

    protected String assemblyUrl3( Map<String, ?> bodyMap ) {
        //字典顺序a----z
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "+" ) );
        return sb.substring( 0, sb.length() - 1 );
    }

    protected boolean diffPayTime12Hour( LocalDateTime payTime, String merOrderNo ) {
        // 计算当前时间与下单时间相差的小时数
        // 超过48小时拒绝回调，可人工补单
        if ( LocalDateTime.now().minusHours( 48 ).compareTo( payTime ) > 0 ) {
            log.warn( "超过48小时拒绝回调 - orderNo:{};payTime:{}", merOrderNo, payTime );
            return true;
        }
        return false;
    }

    protected boolean verifyIP( Map<String, ?> requestMap, String realIp, PayPlatform payPlatform ) {
        if ( StringUtils.isNotBlank( payPlatform.getWhiteIp() ) ) {
            Set<String> whiteIpSet = Arrays.stream( payPlatform.getWhiteIp().split( "," ) ).collect( Collectors.toSet() );
            if ( !whiteIpSet.contains( realIp ) && !"0:0:0:0:0:0:0:1".equals( realIp ) && !"127.0.0.1".equals( realIp ) ) {
                log.warn( "请求ip非白名单:{};支付平台:{};request:{}", realIp, payPlatform.getName(), JsonUtil.object2Json( requestMap ) );
                return true;
            }
        }
        return false;
    }

    /**
     * 参数1 regex:我们的正则字符串
     * 参数2 就是一大段文本，这里用data表示
     */
    protected String filterSpecialStr( String data ) {
        //sb存放正则匹配的结果
        StringBuilder sb = new StringBuilder();
        //利用正则去匹配
        Matcher matcher = URL_PATTERN.matcher( data );
        //如果找到了我们正则里要的东西
        while ( matcher.find() ) {
            //保存到sb中，"\r\n"表示找到一个放一行，就是换行
            sb.append( matcher.group() );
        }
        return sb.toString();
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

    @NotNull
    protected static String packageFormHtml( String url, ReqPayRecharge reqPayRecharge, Map<String, String> bodyMap ) {
        StringBuilder sb = new StringBuilder( "<form id='Form1' name='Form1' method='post' action='" + url + "'>" );

        bodyMap.forEach( ( k, v ) -> sb
                .append( "<input type='hidden' name='" )
                .append( k )
                .append( "' value='" )
                .append( v )
                .append( "'>" ) );

        sb.append( "</form><script>var form = document.getElementById('Form1');form.submit();</script>" );
        reqPayRecharge.setUrlType( 1 );
        return sb.toString();
    }

    protected Map<String, Object> sendPostMap( String url, HttpEntity<?> httpEntity, ReqPayRecharge reqPayRecharge ) {
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
            if ( reqPayRecharge != null ) {
                reqPayRecharge.setFailReason( e.getMessage() );
            }
        }
        return resultMap;
    }

    protected String sendPostString( String url, HttpEntity<?> httpEntity, ReqPayRecharge reqPayRecharge ) {
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
            if ( reqPayRecharge != null ) {
                reqPayRecharge.setFailReason( e.getMessage() );
            }
        }
        return resultStr;
    }

    protected String sendGetString( String url, ReqPayRecharge reqPayRecharge ) {
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
            if ( reqPayRecharge != null ) {
                reqPayRecharge.setFailReason( e.getMessage() );
            }
        }
        return resultStr;
    }

    protected Map<String, Object> sendGetMap( String url, ReqPayRecharge reqPayRecharge ) {
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
            if ( reqPayRecharge != null ) {
                reqPayRecharge.setFailReason( e.getMessage() );
            }
        }
        return resultMap;
    }
}
	
