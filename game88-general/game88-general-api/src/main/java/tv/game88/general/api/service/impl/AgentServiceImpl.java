package tv.game88.general.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.general.api.entity.Agent;
import tv.game88.general.api.mapper.AgentMapper;
import tv.game88.general.api.service.AgentService;

import java.util.List;

/**
 * 代理管理Service业务层处理
 *
 * @author 77tv
 * @date 2021-04-16
 */
@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {
    /**
     * 查询代理管理列表
     *
     * @param Agent 代理管理
     * @return 代理管理
     */
    @Override
    public List<Agent> selectAgentList( Agent Agent) {
        return this.baseMapper.selectAgentList(Agent);
    }

    @Override
    public List<Agent> selectAllAgentList() {
        return this.baseMapper.selectAllAgentList();
    }
}
