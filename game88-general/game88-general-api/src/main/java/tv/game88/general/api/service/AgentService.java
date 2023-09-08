package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.general.api.entity.Agent;

import java.util.List;

/**
 * 代理管理Service接口
 *
 * @author 77tv
 * @date 2021-04-16
 */
public interface AgentService extends IService<Agent> {
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
