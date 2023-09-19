package tv.game88.general.api.service.impl;

import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.general.api.dto.RspHostClient;
import tv.game88.general.api.entity.AgentHostClient;
import tv.game88.general.api.mapper.AgentHostClientMapper;
import tv.game88.general.api.service.AgentHostClientService;

import java.util.List;

@Service
@Master
public class AgentHostClientServiceImpl extends ServiceImpl<AgentHostClientMapper, AgentHostClient> implements AgentHostClientService {

    /**
     * 查询代理人管理列表
     *
     * @param agentHostClient 代理人管理
     * @return 代理人管理
     */
    @Override
    public List<AgentHostClient> selectAgentHostClientList( AgentHostClient agentHostClient) {
        return this.baseMapper.selectAgentHostClientList(agentHostClient);
    }

    @Override
    public RspHostClient findLatestHostClient( Integer dev) {
        return this.baseMapper.findLatestHostClient(dev);
    }
}
