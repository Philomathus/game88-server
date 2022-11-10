package tv.game88.core.member.cache;

import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.member.entity.ConfigRecommend;
import tv.game88.core.member.mapper.ConfigRecommendMapper;

import javax.annotation.Resource;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConfigRecommendCacheUtils {
    public static final String CONFIG_RECOMMEND_KEY = Constants.CONFIG_PREX + "recommend";

    @Resource
    private RedisUtils            redisUtils;
    @Resource
    private ConfigRecommendMapper configRecommendMapper;

    public Map<Integer, ConfigRecommend> getBillMap() {
        if ( !redisUtils.exists( CONFIG_RECOMMEND_KEY ) ) {
            Map<Integer, ConfigRecommend> billMap = configRecommendMapper
                    .selectList( null )
                    .stream()
                    .collect( Collectors.toMap( ConfigRecommend::getLevel, Function.identity() ) );
            Map<String, String> collect = billMap
                    .entrySet()
                    .stream()
                    .collect( Collectors.toMap( e -> e.getKey().toString(), e -> JsonUtil.object2Json( e.getValue() ) ) );
            redisUtils.hMSet( CONFIG_RECOMMEND_KEY, collect );
            return billMap;
        }
        return redisUtils
                .hGetAll( CONFIG_RECOMMEND_KEY )
                .entrySet()
                .stream()
                .collect( Collectors.toMap( e -> Integer.parseInt( e.getKey().toString() ), e -> JsonUtil.json2Object( e
                        .getValue()
                        .toString(), ConfigRecommend.class ) ) );
    }

    public void clear() {
        redisUtils.unlink( CONFIG_RECOMMEND_KEY );
    }
}
