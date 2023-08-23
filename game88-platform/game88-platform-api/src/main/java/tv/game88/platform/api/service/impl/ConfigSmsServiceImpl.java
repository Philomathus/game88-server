package tv.game88.platform.api.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.utils.ValidatorUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigSmsCacheUtil;
import tv.game88.core.config.entity.ConfigSms;
import tv.game88.core.config.mapper.ConfigSmsMapper;
import tv.game88.core.utils.SmsApi;
import tv.game88.platform.api.service.ConfigSmsService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * SMS短信服务配置service impl
 *
 * @author Rajesh
 */

@Service
public class ConfigSmsServiceImpl implements ConfigSmsService {

    @Resource
    private ConfigSmsMapper    configSmsMapper;
    @Resource
    private ConfigSmsCacheUtil configSmsCacheUtil;
    @Resource
    private SmsApi             smsApi;

    /**
     * 查询SMS短信服务配置列表
     * Query SMS configuration list
     *
     * @param configSms SMS短信服务配置
     *
     * @return SMS短信服务配置集合
     */
    @Override
    public List<ConfigSms> selectConfigSmsList( ConfigSms configSms ) {
        return configSmsMapper.selectConfigSmsList( configSms );
    }

    /**
     * insert config sms
     *
     * @param configSms Sms插入配置短信
     *
     * @return Sms插入配置短信
     */
    @Override
    public int insertConfigSms( ConfigSms configSms ) {
        configSms.setEffect( false );
        configSms.setUpdateTime( LocalDateTime.now() );
        return configSmsMapper.insert( configSms );
    }

    /**
     * 修改SMS短信服务配置
     * Modify the SMS text message service configuration
     *
     * @param configSms SMS短信服务配置
     *
     * @return 结果
     */
    @Override
    public int updateConfigOSms( ConfigSms configSms ) {
        configSms.setUpdateTime( LocalDateTime.now() );
        int i = configSmsMapper.updateById( configSms );
        if ( i > 0 ) {
            ConfigSms newServerSms = configSmsMapper.selectById( configSms.getId() );
            if ( BooleanUtils.isTrue( newServerSms.getEffect() ) ) {
                configSmsCacheUtil.setConfigSmsCache( newServerSms );
            }
        }
        return configSmsMapper.updateById( configSms );

    }

    /**
     * 批量删除SMS短信服务配置
     * Delete SMS configuration in batches
     *
     * @param ids 需要删除的SMS短信服务配置ID
     *
     * @return 结果
     */
    @Override
    public int deleteServerSmsByIds( Long[] ids ) {
        List<Long>      idList    = Arrays.asList( ids );
        List<ConfigSms> configSms = configSmsMapper.selectBatchIds( idList );
        for ( ConfigSms configSm : configSms ) {
            if ( BooleanUtils.isTrue(configSm.getEffect()) ) {
                configSmsCacheUtil.clearCache( configSm.getId() );
            }
        }
        return configSmsMapper.deleteBatchIds( idList );
    }

    /**
     * 查询SMS短信服务配置
     * Query SMS SMS configuration
     *
     * @param id SMS短信服务配置ID
     *
     * @return SMS短信服务配置
     */
    @Override
    public ConfigSms selectConfigSmsById( Long id ) {
        return configSmsMapper.selectById( id );
    }

    @Override
    public boolean effect( Long id, String opName ) {
        ConfigSms update = new ConfigSms();
        update.setId( id );
        update.setEffect( true );
        update.setUpdateTime( LocalDateTime.now() );
        update.setUpdateBy( opName );
        int i = configSmsMapper.updateById( update );
        if ( i > 0 ) {
            ConfigSms newConfigSms = configSmsMapper.selectById( id );
            configSmsCacheUtil.setConfigSmsCache( newConfigSms );
        }
        return i > 0;
    }

    @Override
    public boolean noEffect( Long id, String opName ) {
        ConfigSms update = new ConfigSms();
        update.setId( id );
        update.setEffect( false );
        update.setUpdateTime( LocalDateTime.now() );
        update.setUpdateBy( opName );
        int i = configSmsMapper.updateById( update );
        if ( i > 0 ) {
            configSmsCacheUtil.clearCache( id );
        }
        return i > 0;
    }

    @Override
    public RspBase<?> smsTest( Long id, String mobile ) {
        if ( StringUtils.isBlank( mobile ) ) {
            return RspBase.businessError( "请输入你的手机号" );
        }
        if ( ValidatorUtil.isMobile( mobile ) ) {
            return RspBase.businessError( "手机号码不正确" );
        }
        try {
            String code = smsApi.sendSms( mobile, configSmsMapper.selectById( id ) );
            if ( StringUtils.isBlank( code ) ) {
                return RspBase.businessError( "短信发送失败" );
            }
            return RspBase.ok( "短信发送成功", code );
        } catch ( Exception e ) {
            return RspBase.businessError( e.getMessage() );
        }
    }
}
