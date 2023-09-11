package tv.game88.core.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.service.ConfigBankListService;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.entity.ConfigBankList;
import tv.game88.core.config.mapper.ConfigBankListMapper;

import java.util.List;

@Service
public class ConfigBankListServiceImpl extends ServiceImpl<ConfigBankListMapper, ConfigBankList> implements ConfigBankListService {
    @Override
    public List<ConfigBankList> selectConfigBankListList( ConfigBankList configBankList ) {
        List<ConfigBankList> configBankLists = this.baseMapper.selectConfigBankListList( configBankList );
        String               domainValue     = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( ConfigBankList bankList : configBankLists ) {
            if ( StringUtils.isNotBlank( bankList.getBankIcon() ) && !bankList.getBankIcon().startsWith( "http" ) ) {
                bankList.setBankIcon( domainValue + bankList.getBankIcon() );
            }
        }
        return configBankLists;
    }
}

