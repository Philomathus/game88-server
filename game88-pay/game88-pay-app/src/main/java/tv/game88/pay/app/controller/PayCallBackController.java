package tv.game88.pay.app.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.pay.api.base.BasePay;
import tv.game88.pay.api.base.PayProcessorFactoryUtil;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping( "/pay/callBack" )
@Log4j2
public class PayCallBackController {
    @Resource
    private PayProcessorFactoryUtil payProcessorFactoryUtil;

    //统一回调POST请求
    @PostMapping( value = "/{payCode}" )
    public String callbackPost( @PathVariable( "payCode" ) String payCode ) {
        String                realIp     = ServletUtil.getIp();
        Map<String, String[]> requestMap = ServletUtil.getHttpServletRequest().getParameterMap();
        Map<String, Object>   map        = new HashMap<>();
        requestMap.forEach( ( k, v ) -> map.put( k, v[ 0 ] ) );
        BasePay basePay = payProcessorFactoryUtil.createPayProcessor( payCode );
        log.warn( "{}回调数据 - realIp:{};result:{};requestMap:{}", basePay.getName(), realIp, JsonUtil.object2Json( map ),
                JsonUtil.object2Json( requestMap ) );
        return basePay.callbackPay( map, realIp );
    }

    //统一回调FROM请求
    @PostMapping( value = "/{payCode}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
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
    @PostMapping( value = "/{payCode}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    public String callbackJson( @RequestBody Map<String, Object> requestMap, @PathVariable( "payCode" ) String payCode ) {
        String  realIp  = ServletUtil.getIp();
        BasePay basePay = payProcessorFactoryUtil.createPayProcessor( payCode );
        log.warn( "{}回调数据 - realIp:{};result:{}", basePay.getName(), realIp, JsonUtil.object2Json( requestMap ) );
        return basePay.callbackPay( requestMap, realIp );
    }

    //统一回调GET请求
    @GetMapping( value = "/{payCode}" )
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
    @PostMapping( value = "/{payCode}", consumes = { MediaType.TEXT_PLAIN_VALUE } )
    public String callbackText( @RequestBody String body, @PathVariable( "payCode" ) String payCode ) {
        String              realIp = ServletUtil.getIp();
        Map<String, Object> map    = new HashMap<>();
        map.put( "data", body );
        BasePay basePay = payProcessorFactoryUtil.createPayProcessor( payCode );
        log.warn( "{}回调数据 - realIp:{};result:{}", basePay.getName(), realIp, JsonUtil.object2Json( map ) );
        return basePay.callbackPay( map, realIp );
    }
}
