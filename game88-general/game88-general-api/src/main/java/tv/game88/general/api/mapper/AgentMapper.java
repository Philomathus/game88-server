package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.general.api.entity.Agent;

import java.util.List;

/**
 * 代理管理Mapper接口
 *
 * @author 77tv
 * @date 2021-04-16
 */
public interface AgentMapper extends BaseMapper<Agent> {

    /**
     * 查询代理管理列表
     *
     * @param agent 代理管理
     *
     * @return 代理管理集合
     */
    public List<Agent> selectAgentList( Agent agent );

    List<Agent> selectAllAgentList();
}
