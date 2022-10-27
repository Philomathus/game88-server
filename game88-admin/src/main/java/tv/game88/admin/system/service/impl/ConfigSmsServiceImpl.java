package tv.game88.admin.system.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.admin.system.service.IConfigSmsService;
import tv.game88.core.config.entity.ConfigSms;
import tv.game88.core.config.mapper.ConfigSmsMapper;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ConfigSmsServiceImpl implements IConfigSmsService {

    @Resource
    private ConfigSmsMapper configSmsMapper;

    @Override
    public List<ConfigSms> selectConfigSmsList(ConfigSms configSms) {
        return configSmsMapper.selectConfigSmsList(configSms);
    }

    @Override
    public List<ConfigSms> selectConfigSmsByEffect() {
        return configSmsMapper.selectConfigSmsByEffect();
    }
}
