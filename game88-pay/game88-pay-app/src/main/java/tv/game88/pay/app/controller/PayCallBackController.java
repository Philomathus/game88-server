package tv.game88.pay.app.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.pay.api.base.BasePay;
import tv.game88.pay.api.base.BasePayAgent;
import tv.game88.pay.api.base.PayAgentProcessorFactoryUtil;
import tv.game88.pay.api.base.PayProcessorFactoryUtil;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.service.PayService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping( "/pay" )
@Hidden
@Log4j2
public class PayCallBackController {
    @Resource
    private PayProcessorFactoryUtil      payProcessorFactoryUtil;
    @Resource
    private PayAgentProcessorFactoryUtil payAgentProcessorFactoryUtil;
    @Resource
    private PayService                   payService;
    @Resource
    private PayCacheUtil                 payCacheUtil;

    @GetMapping( value = "/orderRedirect/{orderNo}", produces = MediaType.TEXT_HTML_VALUE )
    @ResponseBody
    public ResponseEntity<String> payRedirect( @PathVariable String orderNo ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add( "client_header_buffer_size", "512k" );
        headers.add( "large_client_header_buffers", "4 512k" );
        return new ResponseEntity<>( payService.payRedirect( orderNo ), headers, HttpStatus.OK );
    }

    //-------------------------支付回调--------------------------

    //统一回调POST请求
    @PostMapping( value = "/callBack/{payCode}" )
    public String callbackPost( @PathVariable( "payCode" ) String payCode ) {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePay basePay = payProcessorFactoryUtil.createPayProcessor( payCode );
        log.warn( "{}回调数据 - realIp:{};result:{}", basePay.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePay.callbackPay( map, realIp );
    }

    //统一回调FROM请求
    @PostMapping( value = "/callBack/{payCode}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE } )
    public String callbackForm( @PathVariable( "payCode" ) String payCode ) {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePay basePay = payProcessorFactoryUtil.createPayProcessor( payCode );
        log.warn( "{}回调数据 - realIp:{};result:{}", basePay.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePay.callbackPay( map, realIp );
    }

    //统一回调JSON请求
    @PostMapping( value = "/callBack/{payCode}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    public String callbackJson( @RequestBody Map<String, Object> requestMap, @PathVariable( "payCode" ) String payCode ) {
        String  realIp  = ServletUtil.getIp();
        BasePay basePay = payProcessorFactoryUtil.createPayProcessor( payCode );
        log.warn( "{}回调数据 - realIp:{};result:{}", basePay.getName(), realIp, JsonUtil.object2Json( requestMap ) );
        return basePay.callbackPay( requestMap, realIp );
    }

    //统一回调GET请求
    @GetMapping( value = "/callBack/{payCode}" )
    public String callbackGet( @PathVariable( "payCode" ) String payCode ) {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePay basePay = payProcessorFactoryUtil.createPayProcessor( payCode );
        log.warn( "{}回调数据 - realIp:{};result:{}", basePay.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePay.callbackPay( map, realIp );
    }

    //统一回调TEXT请求
    @PostMapping( value = "/callBack/{payCode}", consumes = { MediaType.TEXT_PLAIN_VALUE } )
    public String callbackText( @RequestBody String body, @PathVariable( "payCode" ) String payCode ) {
        String              realIp = ServletUtil.getIp();
        Map<String, Object> map    = new HashMap<>();
        map.put( "data", body );
        BasePay basePay = payProcessorFactoryUtil.createPayProcessor( payCode );
        log.warn( "{}回调数据 - realIp:{};result:{}", basePay.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePay.callbackPay( map, realIp );
    }

    //-------------------------代付回调--------------------------

    @PostMapping( value = "/agentCallBack/{code}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE } )
    public String agentCallBackForm( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.callbackPay( payAgentPlatform, map, realIp );
    }

    //统一回调JSON请求
    @PostMapping( value = "/agentCallBack/{code}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    public String agentCallBackJson( @RequestBody Map<String, Object> requestMap, @PathVariable( "code" ) String code ) throws Exception {
        String           realIp           = ServletUtil.getIp();
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( requestMap ) );
        return basePayAgent.callbackPay( payAgentPlatform, requestMap, realIp );
    }

    //统一回调TEXT请求
    @PostMapping( value = "/agentCallBack/{code}", consumes = { MediaType.TEXT_HTML_VALUE, MediaType.TEXT_PLAIN_VALUE } )
    public String agentCallBackTextHtml( @RequestBody String body, @PathVariable( "code" ) String code ) throws Exception {
        String              realIp = ServletUtil.getIp();
        Map<String, Object> map    = new HashMap<>();
        map.put( "data", body );
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, body );
        return basePayAgent.callbackPay( payAgentPlatform, map, realIp );
    }

    //统一回调POST请求
    @PostMapping( value = "/agentCallBack/{code}" )
    public String agentCallBackPOST( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.callbackPay( payAgentPlatform, map, realIp );
    }

    //统一回调GET请求
    @GetMapping( value = "/agentCallBack/{code}" )
    public String agentCallBackGet( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}回调 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.callbackPay( payAgentPlatform, map, realIp );
    }

    //-------------------------反查--------------------------
    //统一反查FROM请求
    @PostMapping( value = "/agentReverseCheck/{code}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE } )
    public Map<String, Object> orderReverseCheckFrom( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}反查 - realIp:{};requestMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.reverseCheckOrderPay( payAgentPlatform, map, realIp );
    }

    //统一反查JSON请求
    @PostMapping( value = "/agentReverseCheck/{code}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    public Map<String, Object> orderReverseCheckJson( @RequestBody Map<String, Object> requestMap,
                                                      @PathVariable( "code" ) String code ) throws Exception {
        String           realIp           = ServletUtil.getIp();
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}反查 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( requestMap ) );
        return basePayAgent.reverseCheckOrderPay( payAgentPlatform, requestMap, realIp );
    }

    //统一反查TEXT请求
    @PostMapping( value = "/agentReverseCheck/{code}", consumes = { MediaType.TEXT_HTML_VALUE, MediaType.TEXT_PLAIN_VALUE } )
    public Map<String, Object> orderReverseCheckText( @RequestBody String body, @PathVariable( "code" ) String code ) throws Exception {
        String              realIp = ServletUtil.getIp();
        Map<String, Object> map    = new HashMap<>();
        map.put( "data", body );
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}反查 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, body );
        return basePayAgent.reverseCheckOrderPay( payAgentPlatform, map, realIp );
    }

    //统一反查POST请求
    @PostMapping( value = "/agentReverseCheck/{code}" )
    public Map<String, Object> orderReverseCheckPost( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> map        = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   requestMap = new TreeMap<>();
        map.forEach( ( k, v ) -> requestMap.put( k, v[ 0 ] ) );
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}反查 - realIp:{};requestMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.reverseCheckOrderPay( payAgentPlatform, requestMap, realIp );
    }

    //统一反查GET请求
    @GetMapping( value = "/agentReverseCheck/{code}" )
    public Map<String, Object> orderReverseCheckGet( @PathVariable( "code" ) String code ) throws Exception {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePayAgent     basePayAgent     = payAgentProcessorFactoryUtil.createPayProcessor( code );
        PayAgentPlatform payAgentPlatform = payCacheUtil.getPayAgentPlatform( code );
        log.warn( "{}反查 - realIp:{};bodyMap:{}", basePayAgent.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePayAgent.reverseCheckOrderPay( payAgentPlatform, map, realIp );
    }
}
