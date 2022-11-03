package tv.game88.pay.api.base;

import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.pay.api.mapper.MemberWithdrawDetailMapper;
import tv.game88.pay.api.mapper.PayAgentChannelMapper;
import tv.game88.pay.api.mapper.PayAgentLogMapper;
import tv.game88.pay.api.service.PayAgentService;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

@Log4j2
public abstract class AbstractPayAgent implements BasePayAgent {
    @Resource
    protected PayAgentChannelMapper      payAgentChannelMapper;
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
}
