package tv.game88.general.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.general.api.dto.RspHostClient;
import tv.game88.general.api.entity.AgentHostClient;
import tv.game88.general.api.mapper.AgentHostClientMapper;
import tv.game88.general.api.service.IAgentHostClientService;

@Service
public class AgentHostClientServiceImpl extends ServiceImpl<AgentHostClientMapper, AgentHostClient> implements IAgentHostClientService {

    @Override
    public RspHostClient findLatestHostClient( Integer dev) {
        return this.baseMapper.findLatestHostClient(dev);
    }
}
