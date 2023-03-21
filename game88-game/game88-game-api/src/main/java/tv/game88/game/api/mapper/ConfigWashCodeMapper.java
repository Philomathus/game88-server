package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.entity.ConfigWashCode;
import java.util.List;

/**
 * 洗码配置Mapper接口
 *
 * @author krzystof
 */
public interface ConfigWashCodeMapper extends BaseMapper<ConfigWashCode> {

    /**
     * 查询洗码配置列表
     *
     * @param configWashCode 洗码配置
     *
     * @return 洗码配置集合
     */
    List<ConfigWashCode> selectConfigWashCodeList( ConfigWashCode configWashCode );
}
