package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.game.api.entity.ConfigWashCode;
import tv.game88.game.api.mapper.ConfigWashCodeMapper;
import tv.game88.game.api.service.ConfigWashCodeService;
import java.util.List;

/**
 * 洗码配置Service业务层处理
 *
 * @author krzystof
 */
@Service
public class ConfigWashCodeServiceImpl extends ServiceImpl<ConfigWashCodeMapper, ConfigWashCode> implements ConfigWashCodeService {

    /**
     * 查询洗码配置列表
     *
     * @param configCleanCode 洗码配置
     *
     * @return 洗码配置集合
     */
    @Override
    public List<ConfigWashCode> selectConfigWashCodeList( ConfigWashCode configCleanCode ) {
        return this.baseMapper.selectConfigWashCodeList( configCleanCode );
    }
}
