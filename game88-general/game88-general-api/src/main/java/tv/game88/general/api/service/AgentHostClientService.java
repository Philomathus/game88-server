package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.dto.RspHostClient;
import tv.game88.general.api.entity.AgentHostClient;

import java.util.List;

public interface AgentHostClientService extends IService<AgentHostClient> {

    /**
     * 查询代理人管理列表
     *
     * @param agentHostClient 代理人管理
     *
     * @return 代理人管理集合
     */
    public List<AgentHostClient> selectAgentHostClientList( AgentHostClient agentHostClient );

    RspHostClient findLatestHostClient( Integer dev );

}
