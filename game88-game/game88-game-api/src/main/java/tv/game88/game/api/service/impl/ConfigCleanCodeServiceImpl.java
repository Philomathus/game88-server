package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.game.api.entity.ConfigCleanCode;
import tv.game88.game.api.mapper.ConfigCleanCodeMapper;
import tv.game88.game.api.service.ConfigCleanCodeService;
import java.util.List;

/**
 * 洗码配置Service业务层处理
 *
 * @author krzystof
 */
@Service
public class ConfigCleanCodeServiceImpl extends ServiceImpl<ConfigCleanCodeMapper, ConfigCleanCode> implements ConfigCleanCodeService {

    /**
     * 查询洗码配置列表
     *
     * @param configCleanCode 洗码配置
     *
     * @return 洗码配置集合
     */
    @Override
    public List<ConfigCleanCode> selectConfigCleanCodeList(ConfigCleanCode configCleanCode) {
        return this.baseMapper.selectConfigCleanCodeList( configCleanCode );
    }
}
