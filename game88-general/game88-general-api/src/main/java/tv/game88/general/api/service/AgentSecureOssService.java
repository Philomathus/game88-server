package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.entity.AgentSecureOss;

import java.util.List;

/**
 * 代理域名ossService接口
 *
 * @author 77tv
 * @date 2021-04-05
 */
public interface AgentSecureOssService extends IService<AgentSecureOss> {
    /**
     * 查询代理域名oss列表
     *
     * @param agentSecureOss 代理域名oss
     *
     * @return 代理域名oss集合
     */
    public List<AgentSecureOss> selectAgentSecureOssList( AgentSecureOss agentSecureOss );

    public Object getAgentList();
}
