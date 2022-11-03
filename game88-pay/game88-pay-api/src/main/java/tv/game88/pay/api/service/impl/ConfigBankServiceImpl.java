package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.ConfigBank;
import tv.game88.pay.api.mapper.ConfigBankMapper;
import tv.game88.pay.api.service.ConfigBankService;

import java.util.List;

@Service
public class ConfigBankServiceImpl extends ServiceImpl<ConfigBankMapper, ConfigBank> implements ConfigBankService {
    @Override
    public List<ConfigBank> selectConfigBankList( ConfigBank configBank ) {
        return this.baseMapper.selectConfigBankList( configBank );
    }
}

