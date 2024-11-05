package tv.game88.core.config.cache;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.core.config.entity.ConfigBankList;
import tv.game88.core.config.mapper.ConfigBankListMapper;

import jakarta.annotation.Resource;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Component
public class ConfigBankListCache {
    private static final String CONFIG_BANKLIST_CACHE = Constants.CONFIG_PREX + "bankList";
    private static final String CONFIG_BANK_CACHE     = Constants.CONFIG_PREX + "bank";

    @Resource
    private ConfigBankListMapper configBankListMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private Cache<String, Object> cache;

    public List<ConfigBankList> getEffectList() {
        List<ConfigBankList> localCacheIfPresent = ( List<ConfigBankList> ) cache.getIfPresent( CONFIG_BANKLIST_CACHE );
        if ( CollectionUtils.isEmpty( localCacheIfPresent ) ) {
            exists();
            List<ConfigBankList> configBankLists = redisUtils.hGetAll( CONFIG_BANKLIST_CACHE ).values().stream()
                    .map( obj -> JsonUtil.json2Object( obj.toString(), ConfigBankList.class ) ).toList();
            log.warn( configBankLists );
            configBankLists = configBankLists.stream().filter( ConfigBankList::getEffect )
                    .sorted( Comparator.comparing( o -> o.getSort() != null ? o.getSort() : 0 ) ).toList();
            cache.put( CONFIG_BANKLIST_CACHE, configBankLists );
            return configBankLists;
        }
        return localCacheIfPresent;
    }

    public ConfigBankList getConfigBank( Long id ) {
        ConfigBankList localCacheIfPresent = ( ConfigBankList ) cache.getIfPresent( CONFIG_BANK_CACHE );
        if ( localCacheIfPresent == null ) {
            exists();
            Object o = redisUtils.hGet( CONFIG_BANKLIST_CACHE, id.toString() );
            if ( o != null ) {
                ConfigBankList configBank = JsonUtil.json2Object( o.toString(), ConfigBankList.class );
                cache.put( CONFIG_BANK_CACHE, configBank );
                return configBank;
            }
        }
        return localCacheIfPresent;
    }

    private void exists() {
        if ( !redisUtils.exists( CONFIG_BANKLIST_CACHE ) ) {
            List<ConfigBankList> bankListList = new QueryChainWrapper<>( configBankListMapper ).list();
            redisUtils.hMSet( CONFIG_BANKLIST_CACHE, bankListList.stream()
                    .collect( Collectors.toMap( a -> a.getId().toString(), JsonUtil::object2Json ) ) );
        }
    }

    public void clear() {
        redisUtils.unlink( CONFIG_BANKLIST_CACHE );
    }
}
