package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.general.api.entity.AgentSecure;

import java.util.List;

/**
 * 域名加密管理Mapper接口
 *
 * @author 77tv
 * @date 2021-04-01
 */
public interface AgentSecureMapper extends BaseMapper<AgentSecure> {

	/**
	 * 查询域名加密管理列表
	 *
	 * @param agentSecure 域名加密管理
	 * @return 域名加密管理集合
	 */
	public List<AgentSecure> selectAgentSecureList(AgentSecure agentSecure);
}
