package tv.game88.core.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.config.entity.ConfigSms;

import java.util.List;

/**
 * SMS短信服务配置Mapper接口
 *
 * @author MengJun
 */
public interface ConfigSmsMapper extends BaseMapper<ConfigSms> {

    /**
     * 查询SMS短信服务配置列表
     *
     * @param configSms SMS短信服务配置
     *
     * @return SMS短信服务配置集合
     */
    public List<ConfigSms> selectConfigSmsList( ConfigSms configSms );

    List<ConfigSms> selectConfigSmsByEffect();

}
