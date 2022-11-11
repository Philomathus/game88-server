package tv.game88.pay.api.base;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.utils.Sets;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.mapper.MemberRechargeOnlineMapper;
import tv.game88.pay.api.mapper.PayPlatformMapper;

import javax.annotation.Resource;
import java.util.*;

@Log4j2
public abstract class AbstractPay implements BasePay {

    @Resource
    protected MemberRechargeOnlineMapper memberRechargeOnlineMapper;
    @Resource
    protected PayPlatformMapper          payPlatformMapper;
    @Resource
    protected RestTemplate               restTemplate;
    @Resource
    protected ConfigEnvCacheUtil         configEnvCacheUtil;
    @Resource
    protected PayCacheUtil               payCacheUtil;

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

    protected boolean diffPayTime12Hour( Date payTime, String merOrderNo ) {
        // 计算当前时间与下单时间相差的小时数
        int hourDiff = ( int ) ( ( System.currentTimeMillis() - payTime.getTime() ) / ( 1000 * 60 * 60 ) );
        // 超过48小时拒绝回调，可人工补单
        if ( hourDiff >= 48 ) {
            log.warn( "超过48小时拒绝回调 - orderNo:{};payTime:{}", merOrderNo, payTime );
            return true;
        }
        return false;
    }

    protected boolean verifyIP( Map<String, ?> requestMap, String realIp, PayPlatform payPlatform ) {
        if ( StringUtils.hasText( payPlatform.getWhiteIp() ) ) {
            Set<String> whiteIpSet = Sets.newHashSet( payPlatform.getWhiteIp().split( "," ) );
            if ( !whiteIpSet.contains( realIp ) && !"0:0:0:0:0:0:0:1".equals( realIp ) ) {
                log.warn( "请求ip非白名单:{};支付平台:{};request:{}", realIp, payPlatform.getName(), JsonUtil.object2Json( requestMap ) );
                return true;
            }
        }
        return false;
    }
}
	
