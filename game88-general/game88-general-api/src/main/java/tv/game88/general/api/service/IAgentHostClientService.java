package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.dto.RspHostClient;
import tv.game88.general.api.entity.AgentHostClient;

public interface IAgentHostClientService extends IService<AgentHostClient> {

    RspHostClient findLatestHostClient( Integer dev);

}
