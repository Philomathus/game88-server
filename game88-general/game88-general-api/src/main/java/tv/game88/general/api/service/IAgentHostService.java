package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.dto.RspAgent;
import tv.game88.general.api.entity.AgentHost;

public interface IAgentHostService extends IService<AgentHost> {
    RspAgent findAgentHost( String agent);
}
