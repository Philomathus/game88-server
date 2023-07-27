package tv.game88.platform.api.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.platform.api.entity.YiDunDeviceConfigResult;
import tv.game88.platform.api.service.YidunDeviceConfigService;
import tv.game88.platform.api.util.YiDunHttpClient;
import tv.game88.platform.api.util.YiDunParamUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import static tv.game88.platform.api.constant.Constants.*;

/**
 * YiDun server impl
 *
 * @author Rajesh
 * @date 2023-07-23
 */

@Slf4j
@Service
public class YiDunDeviceConfigServiceImpl implements YidunDeviceConfigService {

    @Override
    public YiDunDeviceConfigResult getYiDunDeviceResult( String token ) {
        Map<String, String> requestParams = createRequestParams( token );
        String              res           = null;
        try {
            res = YiDunHttpClient.sendPost( HTTPCLIENT, URI_SEND_FINGERPRINT, requestParams );
        } catch( Exception ex ) {
            log.error( "Error sending request to YiDun: ", ex );
        }

        log.info( "Response from YiDun: {}", res );

        YiDunDeviceConfigResult yiDunDeviceResult = new YiDunDeviceConfigResult();
        if( StringUtils.isBlank( res ) ) {
            return yiDunDeviceResult;
        }

        Map<String, Object> resultMap = JsonUtil.json2Map( res );
        if( CollectionUtils.isEmpty( resultMap ) ) {
            return yiDunDeviceResult;
        }

        Map<String, Object> dataMap = (Map<String, Object>) resultMap.get( "data" );

        if( CollectionUtils.isEmpty( dataMap ) ) {
            return yiDunDeviceResult;
        }

        Map<String, Object> deviceMap = (Map<String, Object>) dataMap.get( "device" );

        if( CollectionUtils.isEmpty( deviceMap ) ) {
            return yiDunDeviceResult;
        }

        yiDunDeviceResult.setDeviceId( (String) deviceMap.getOrDefault( "deviceId", null ) );
        yiDunDeviceResult.setSdkType( (Integer) deviceMap.getOrDefault( "sdkType", null ) );
        yiDunDeviceResult.setCheckResult( ( Map<String, Integer>) deviceMap.getOrDefault( "checkResult", null ) );
        yiDunDeviceResult.setSerializedCheckResult( serializeCheckResult( yiDunDeviceResult.getCheckResult() ) );

        return yiDunDeviceResult;
    }

    private static Map<String, String> createRequestParams( String token ) {
        Map<String, String> params = new HashMap<>(7);
        params.put("nonce", YiDunParamUtils.createNonce());
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "v1");
        params.put("token", token);
        params.put("businessId", BUSINESS_ID);
        params.put("secretId", SECRET_ID);
        //参数赋值完成之后，最后生成签名 generating signature
        params.put("signature", YiDunParamUtils.genSignature(SECRET_KEY, params));
        return params;
    }

    private static String generateSignature( String secretKey, SortedMap<String, Object> params ) {
        StringBuilder stringBuilder = new StringBuilder();

        params.forEach( ( key, value ) -> stringBuilder.append( key ).append( value ) );
        stringBuilder.append( secretKey );

        return DigestUtils.md5DigestAsHex( stringBuilder.toString().getBytes( StandardCharsets.UTF_8 ) );
    }

    private static String serializeCheckResult( Map<String, Integer> checkResult ) {
        StringBuilder stringBuilder = new StringBuilder();

        int index = 0;
        if( checkResult.size() !=0 ){
            for ( Iterator<Integer> it = checkResult.values().iterator(); it.hasNext(); ++index ) {
                if( it.next() == 1 ) {
                    stringBuilder.append( index ).append( ',' );
                }
            }
            return stringBuilder.substring( 0, stringBuilder.length() - 1 );
        }
        return "";
    }

}
