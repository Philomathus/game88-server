package tv.game88.pay.app.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.pay.api.base.BasePayAgent;
import tv.game88.pay.api.base.PayAgentProcessorFactoryUtil;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay-agent/callBack" )
@Log4j2
public class PayAgentCallBackController {
    @Resource
    private PayAgentProcessorFactoryUtil payAgentProcessorFactoryUtil;

    //统一回调FROM请求
    @PostMapping( value = "/{code}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE } )
    public String callbackForm( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.callbackPay( map, realIp );
    }

    //统一回调JSON请求
    @PostMapping( value = "/{code}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    public String callbackJson( @RequestBody Map<String, Object> requestMap, @PathVariable( "code" ) String code ) throws Exception {
        String       realIp       = ServletUtil.getIp();
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( requestMap ) );
        return basePayAgent.callbackPay( requestMap, realIp );
    }

    //统一回调TEXT请求
    @PostMapping( value = "/{code}", consumes = { MediaType.TEXT_HTML_VALUE, MediaType.TEXT_PLAIN_VALUE } )
    public String callbackTextHtml( @RequestBody String body, @PathVariable( "code" ) String code ) throws Exception {
        String              realIp = ServletUtil.getIp();
        Map<String, Object> map    = new HashMap<>();
        map.put( "data", body );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, body );
        return basePayAgent.callbackPay( map, realIp );
    }

    //统一回调POST请求
    @PostMapping( value = "/{code}" )
    public String callbackPOST( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.callbackPay( map, realIp );
    }

    //统一回调GET请求
    @GetMapping( value = "/{code}" )
    public String callbackGet( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.callbackPay( map, realIp );
    }

    //-------------------------反查--------------------------
    //统一反查FROM请求
    @PostMapping( value = "/orderReverseCheck/{code}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE } )
    public Map<String, Object> orderReverseCheckFrom( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}反查 - realIp:{};requestMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.reverseCheckOrderPay( map, realIp );
    }

    //统一反查JSON请求
    @PostMapping( value = "/orderReverseCheck/{code}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    public Map<String, Object> orderReverseCheckJson( @RequestBody Map<String, Object> requestMap,
                                                      @PathVariable( "code" ) String code ) throws Exception {
        String       realIp       = ServletUtil.getIp();
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}反查 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( requestMap ) );
        return basePayAgent.reverseCheckOrderPay( requestMap, realIp );
    }

    //统一反查TEXT请求
    @PostMapping( value = "/orderReverseCheck/{code}", consumes = { MediaType.TEXT_HTML_VALUE, MediaType.TEXT_PLAIN_VALUE } )
    public Map<String, Object> orderReverseCheckText( @RequestBody String body, @PathVariable( "code" ) String code ) throws Exception {
        String              realIp = ServletUtil.getIp();
        Map<String, Object> map    = new HashMap<>();
        map.put( "data", body );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}反查 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, body );
        return basePayAgent.reverseCheckOrderPay( map, realIp );
    }

    //统一反查POST请求
    @PostMapping( value = "/orderReverseCheck/{code}" )
    public Map<String, Object> orderReverseCheckPost( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> map        = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   requestMap = new TreeMap<>();
        map.forEach( ( k, v ) -> requestMap.put( k, v[ 0 ] ) );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}反查 - realIp:{};requestMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.reverseCheckOrderPay( requestMap, realIp );
    }

    //统一反查GET请求
    @GetMapping( value = "/orderReverseCheck/{code}" )
    public Map<String, Object> orderReverseCheckGet( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( code );
        log.warn( "{}反查 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.reverseCheckOrderPay( map, realIp );
    }
}
