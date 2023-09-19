package tv.game88.general.api.service.impl;

import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.general.api.entity.AgentSecureOss;
import tv.game88.general.api.mapper.AgentHostMapper;
import tv.game88.general.api.mapper.AgentSecureOssMapper;
import tv.game88.general.api.service.AgentSecureOssService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 代理域名ossService业务层处理
 *
 * @author 77tv
 * @date 2021-04-05
 */
@Service
@Master
public class AgentSecureOssServiceImpl extends ServiceImpl<AgentSecureOssMapper, AgentSecureOss> implements AgentSecureOssService {
    @Resource
    private AgentHostMapper      agentHostMapper;

    /**
     * 查询代理域名oss列表
     *
     * @param agentSecureOss 代理域名oss
     * @return 代理域名oss
     */
    @Override
    public List<AgentSecureOss> selectAgentSecureOssList(AgentSecureOss agentSecureOss) {
        return this.baseMapper.selectAgentSecureOssList(agentSecureOss);
    }

    @Override
    public Object getAgentList() {
        return agentHostMapper.getAgentList();
    }
}
