package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.dto.RspConfigBankList;
import tv.game88.pay.api.entity.ConfigBankList;
import tv.game88.pay.api.mapper.ConfigBankListMapper;
import tv.game88.pay.api.service.ConfigBankListService;

import java.util.List;

@Service
public class ConfigBankListServiceImpl extends ServiceImpl<ConfigBankListMapper, ConfigBankList> implements ConfigBankListService {
    @Override
    public List<ConfigBankList> selectConfigBankListList( ConfigBankList configBankList ) {
        return this.baseMapper.selectConfigBankListList( configBankList );
    }
}

