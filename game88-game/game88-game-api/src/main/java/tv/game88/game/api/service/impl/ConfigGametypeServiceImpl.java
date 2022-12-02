package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.game.api.entity.ConfigGametype;
import tv.game88.game.api.mapper.ConfigGametypeMapper;
import tv.game88.game.api.service.ConfigGametypeService;

import java.util.List;

/**
 * Service业务层处理
 *
 * @author MengJun
 */
@Service
public class ConfigGametypeServiceImpl extends ServiceImpl<ConfigGametypeMapper, ConfigGametype> implements ConfigGametypeService {
    /**
     * 查询列表
     *
     * @param configGametype 
     * @return 
     */
    @Override
    public List<ConfigGametype> selectConfigGametypeList(ConfigGametype configGametype) {
        return this.baseMapper.selectConfigGametypeList(configGametype);
    }
}