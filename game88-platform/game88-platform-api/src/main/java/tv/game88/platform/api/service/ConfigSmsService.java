package tv.game88.platform.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.core.config.entity.ConfigSms;

import java.util.List;

/**
 * SMS短信服务配置Service
 *
 * @author Rajesh
 */

public interface ConfigSmsService {

    /**
     * 查询SMS短信服务配置列表
     *
     * @param configSms SMS短信服务配置
     *
     * @return SMS短信服务配置集合
     */
    public List<ConfigSms> selectConfigSmsList(ConfigSms configSms );

    /**
     * insert config sms
     * @param configSms Sms插入配置短信
     *
     * @return Sms插入配置短信
     */
    int insertConfigSms(ConfigSms configSms);

    public int updateConfigOSms( ConfigSms configSms );

    /**
     * 批量删除SMS短信服务配置
     * Delete SMS configuration in batches
     * @param ids 需要删除的SMS短信服务配置ID
     * @return 结果
     */
    public int deleteServerSmsByIds( Long[] ids );

    /**
     * 查询SMS短信服务配置
     *Query SMS SMS configuration
     * @param id SMS短信服务配置ID
     * @return SMS短信服务配置
     */
    public ConfigSms selectConfigSmsById( Long id );

    boolean effect( Long id, String opName );

    boolean noEffect( Long id, String opName );

    RspBase<?> smsTest( Long id, String mobile );
}
