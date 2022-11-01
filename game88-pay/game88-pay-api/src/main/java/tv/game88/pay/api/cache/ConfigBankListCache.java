package tv.game88.pay.api.cache;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.pay.api.dto.RspConfigBankList;
import tv.game88.pay.api.entity.ConfigBankList;
import tv.game88.pay.api.mapper.ConfigBankListMapper;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConfigBankListCache {
    private static final String CONFIG_BANKLIST_CACHE = Constants.CONFIG_PREX + "bankList";

    @Resource
    private ConfigBankListMapper configBankListMapper;
    @Resource
    private RedisUtils           redisUtils;

    public List<RspConfigBankList> getEffectList() {
        exists();
        return redisUtils.hGetAll( CONFIG_BANKLIST_CACHE ).values().stream()
                .map( obj -> JsonUtil.json2Object( obj.toString(), RspConfigBankList.class ) )
                .sorted( Comparator.comparing( o -> o.getSort() ) ).collect( Collectors.toList() );
    }

    public void exists() {
        if ( !redisUtils.exists( CONFIG_BANKLIST_CACHE ) ) {
            List<ConfigBankList> bankListList = new QueryChainWrapper<>( configBankListMapper ).eq( "effect", 1 )
                    .select( "id", "bank_name", "bank_icon", "sort" ).list();

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
