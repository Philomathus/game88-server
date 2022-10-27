package tv.game88.admin.system.service;

import tv.game88.core.config.entity.ConfigSms;

import java.util.List;

public interface IConfigSmsService {

    /**
     * 查询SMS短信服务配置列表
     *
     * @param configSms SMS短信服务配置
     *
     * @return SMS短信服务配置集合
     */
    public List<ConfigSms> selectConfigSmsList(ConfigSms configSms );

    List<ConfigSms> selectConfigSmsByEffect();


}
