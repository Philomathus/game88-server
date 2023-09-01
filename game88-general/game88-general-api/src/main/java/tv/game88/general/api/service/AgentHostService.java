package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.dto.RspAgent;
import tv.game88.general.api.entity.AgentHost;

import java.util.List;

public interface AgentHostService extends IService<AgentHost> {
    /**
     * 查询主播域名管理列表
     *
     * @param agentHost 主播域名管理
     *
     * @return 主播域名管理集合
     */
    public List<AgentHost> selectAgentHostList( AgentHost agentHost );

    RspAgent findAgentHost( String agent );
}
