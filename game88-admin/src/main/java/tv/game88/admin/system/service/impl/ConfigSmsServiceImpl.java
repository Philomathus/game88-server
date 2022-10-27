package tv.game88.admin.system.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.admin.system.service.IConfigSmsService;
import tv.game88.core.config.entity.ConfigSms;
import tv.game88.core.config.mapper.ConfigSmsMapper;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * SMS短信服务配置service impl
 *
 * @author Rajesh
 * @date 2022-10-27
 */

@Service
public class ConfigSmsServiceImpl implements IConfigSmsService {

    @Resource
    private ConfigSmsMapper configSmsMapper;

    /**
     * 查询SMS短信服务配置列表
     *Query SMS configuration list
     * @param configSms SMS短信服务配置
     *
     * @return SMS短信服务配置集合
     */
    @Override
    public List<ConfigSms> selectConfigSmsList(ConfigSms configSms) {
        return configSmsMapper.selectConfigSmsList(configSms);
    }

    @Override
    public List<ConfigSms> selectConfigSmsByEffect() {
        return configSmsMapper.selectConfigSmsByEffect();
    }


    /**
     * insert config sms
     * @param configSms Sms插入配置短信
     * @return Sms插入配置短信
     */
    @Override
    public int insertConfigSms(ConfigSms configSms) {
        configSms.setIsEffect(0);
        configSms.setUpdateTime(LocalDateTime.now());
        return configSmsMapper.insert(configSms);
    }

    /**
     * 修改SMS短信服务配置
     * Modify the SMS text message service configuration
     * @param configSms SMS短信服务配置
     * @return 结果
     */
    @Override
    public int updateConfigOSms(ConfigSms configSms) {
        configSms.setUpdateTime(LocalDateTime.now());
//        int i =  configSmsMapper.updateById(configSms);
//        if ( i > 0 ) {
//            ConfigSms newServerSms = configSmsMapper.updateById(configSms.getId())
//            if ( newServerSms.getIsEffect() == 1 ) {
//                serverSmsCacheUtil.setServerSmsCache( newServerSms );
//            }
//        }
        return configSmsMapper.updateById(configSms);

    }

    /**
     * 批量删除SMS短信服务配置
     * Delete SMS configuration in batches
     * @param ids 需要删除的SMS短信服务配置ID
     * @return 结果
     */
    @Override
    public int deleteServerSmsByIds(Long[] ids) {
        return configSmsMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 查询SMS短信服务配置
     *Query SMS SMS configuration
     * @param id SMS短信服务配置ID
     * @return SMS短信服务配置
     */
    @Override
    public ConfigSms selectConfigSmsById(Long id) {
        return configSmsMapper.selectById(id);
    }

}
