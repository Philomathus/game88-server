package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.general.api.dto.RspAgent;
import tv.game88.general.api.entity.AgentHost;

public interface AgentHostMapper extends BaseMapper<AgentHost> {
    RspAgent findAgentHost( @Param( "code" ) String agent);

}
