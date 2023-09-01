package tv.game88.general.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.entity.AgentSecure;

import java.util.List;

/**
 * 域名加密管理Service接口
 *
 * @author 77tv
 * @date 2021-04-01
 */
public interface AgentSecureService extends IService<AgentSecure> {
    /**
     * 查询域名加密管理列表
     *
     * @param agentSecure 域名加密管理
     *
     * @return 域名加密管理集合
     */
    public List<AgentSecure> selectAgentSecureList( AgentSecure agentSecure );

    public String uploadAgent( String agent );
}
