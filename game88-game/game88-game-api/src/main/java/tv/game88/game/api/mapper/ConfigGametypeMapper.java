package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.entity.ConfigGametype;

import java.util.List;

/**
 * Mapper接口
 *
 * @author MengJun
 */
public interface ConfigGametypeMapper extends BaseMapper<ConfigGametype> {

    /**
     * 查询列表
     *
     * @param configGametype
     *
     * @return 集合
     */
    public List<ConfigGametype> selectConfigGametypeList( ConfigGametype configGametype );
}