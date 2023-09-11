package tv.game88.core.config.cache;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.core.config.entity.ConfigBankList;
import tv.game88.core.config.mapper.ConfigBankListMapper;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConfigBankListCache {
    private static final String CONFIG_BANKLIST_CACHE = Constants.CONFIG_PREX + "bankList";

    private static final Cache<String, List<RspConfigBankList>> LOCAL_CACHE = Caffeine.newBuilder()
            // 设置最后一次写入或访问后经过固定时间过期
            .expireAfterWrite( Duration.ofSeconds( 10 ) )
            // 初始的缓存空间大小
            .initialCapacity( 1 )
            // 缓存的最大条数
            .maximumSize( 1 )
            // 构建
            .build();

    @Resource
    private ConfigBankListMapper configBankListMapper;
    @Resource
    private RedisUtils           redisUtils;

    public List<RspConfigBankList> getEffectList() {
        List<RspConfigBankList> localCacheIfPresent = LOCAL_CACHE.getIfPresent( CONFIG_BANKLIST_CACHE );
        if ( CollectionUtils.isEmpty( localCacheIfPresent ) ) {
            exists();
            List<RspConfigBankList> rspConfigBankLists = redisUtils
                    .hGetAll( CONFIG_BANKLIST_CACHE )
                    .values()
                    .stream()
                    .map( obj -> JsonUtil.json2Object( obj.toString(), RspConfigBankList.class ) )
                    .sorted( Comparator.comparing( o -> o.getSort() ) )
                    .collect( Collectors.toList() );
            LOCAL_CACHE.put( CONFIG_BANKLIST_CACHE, rspConfigBankLists );
            return rspConfigBankLists;
        }
        return localCacheIfPresent;
    }

    public void exists() {
        if ( !redisUtils.exists( CONFIG_BANKLIST_CACHE ) ) {
            List<ConfigBankList> bankListList = new QueryChainWrapper<>( configBankListMapper )
                    .eq( "effect", 1 )
                    .select( "id", "bank_name", "bank_icon", "sort" )
                    .list();

            Map<String, String> map = new HashMap<>();
            for ( ConfigBankList configBankList : bankListList ) {
                RspConfigBankList rspConfigBankList = new RspConfigBankList();
                BeanUtils.copyProperties( configBankList, rspConfigBankList );
                map.put( configBankList.getId().toString(), JsonUtil.object2Json( rspConfigBankList ) );
            }

            redisUtils.hMSet( CONFIG_BANKLIST_CACHE, map );
        }
    }

    public void setEffectConfigBank( ConfigBankList configBankList ) {
        exists();
        RspConfigBankList rspConfigBankList = new RspConfigBankList();
        BeanUtils.copyProperties( configBankList, rspConfigBankList );
        redisUtils.hSet( CONFIG_BANKLIST_CACHE, configBankList.getId().toString(), JsonUtil.object2Json( rspConfigBankList ) );
    }

    public void delEffectConfigBank( long id ) {
        if ( redisUtils.exists( CONFIG_BANKLIST_CACHE ) ) {
            redisUtils.hRemove( CONFIG_BANKLIST_CACHE, String.valueOf( id ) );
        }
    }
}
