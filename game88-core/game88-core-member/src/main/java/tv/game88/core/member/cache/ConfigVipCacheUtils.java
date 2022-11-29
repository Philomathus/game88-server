package tv.game88.core.member.cache;

import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.member.entity.ConfigVip;
import tv.game88.core.member.mapper.ConfigVipMapper;

import javax.annotation.Resource;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConfigVipCacheUtils {
    public static final String CONFIG_VIP_KEY = Constants.CONFIG_PREX + "vip";

    @Resource
    private RedisUtils      redisUtils;
    @Resource
    private ConfigVipMapper configVipMapper;

    public Map<Integer, ConfigVip> getConfigVipMap() {
        if ( !redisUtils.exists( CONFIG_VIP_KEY ) ) {
            Map<Integer, ConfigVip> billMap = configVipMapper
                    .selectList( null )
                    .stream()
                    .collect( Collectors.toMap( ConfigVip::getLevel, Function.identity() ) );
            Map<String, String> collect = billMap
                    .entrySet()
                    .stream()
                    .collect( Collectors.toMap( e -> e.getKey().toString(), e -> JsonUtil.object2Json( e.getValue() ) ) );
            redisUtils.hMSet( CONFIG_VIP_KEY, collect );
            return billMap;
        }
        return redisUtils
                .hGetAll( CONFIG_VIP_KEY )
                .entrySet()
                .stream()
                .collect( Collectors.toMap( e -> Integer.parseInt( e.getKey().toString() ), e -> JsonUtil.json2Object( e
                        .getValue()
                        .toString(), ConfigVip.class ) ) );
    }

    public void clear() {
        redisUtils.unlink( CONFIG_VIP_KEY );
    }
}
