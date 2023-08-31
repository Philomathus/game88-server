package tv.game88.general.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.general.api.dto.RspAgent;
import tv.game88.general.api.entity.AgentHost;
import tv.game88.general.api.mapper.AgentHostMapper;
import tv.game88.general.api.service.IAgentHostService;

@Service
public class AgentHostServiceImpl extends ServiceImpl<AgentHostMapper, AgentHost> implements IAgentHostService {

    @Override
    public RspAgent findAgentHost( String agent) {
        return this.baseMapper.findAgentHost(agent);
    }
}
