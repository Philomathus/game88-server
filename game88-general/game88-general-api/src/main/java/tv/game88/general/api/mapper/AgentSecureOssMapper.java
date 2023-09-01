package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.general.api.entity.AgentSecureOss;

import java.util.List;

/**
 * 代理域名ossMapper接口
 *
 * @author 77tv
 * @date 2021-04-05
 */
public interface AgentSecureOssMapper extends BaseMapper<AgentSecureOss> {

    /**
     * 查询代理域名oss列表
     *
     * @param agentSecureOss 代理域名oss
     *
     * @return 代理域名oss集合
     */
    public List<AgentSecureOss> selectAgentSecureOssList( AgentSecureOss agentSecureOss );
}
