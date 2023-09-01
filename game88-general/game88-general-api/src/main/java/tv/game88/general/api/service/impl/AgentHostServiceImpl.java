package tv.game88.general.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.general.api.dto.RspAgent;
import tv.game88.general.api.entity.AgentHost;
import tv.game88.general.api.mapper.AgentHostMapper;
import tv.game88.general.api.service.AgentHostService;

import java.util.List;

@Service
public class AgentHostServiceImpl extends ServiceImpl<AgentHostMapper, AgentHost> implements AgentHostService {
    /**
     * 查询主播域名管理列表
     *
     * @param agentHost 主播域名管理
     *
     * @return 主播域名管理
     */
    @Override
    public List<AgentHost> selectAgentHostList( AgentHost agentHost ) {
        return this.baseMapper.selectAgentHostList( agentHost );
    }

    @Override
    public RspAgent findAgentHost( String agent ) {
        return this.baseMapper.findAgentHost( agent );
    }
}
