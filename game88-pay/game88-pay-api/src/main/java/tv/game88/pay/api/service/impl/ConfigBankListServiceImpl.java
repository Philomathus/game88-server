package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.pay.api.entity.ConfigBankList;
import tv.game88.pay.api.mapper.ConfigBankListMapper;
import tv.game88.pay.api.service.ConfigBankListService;

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

