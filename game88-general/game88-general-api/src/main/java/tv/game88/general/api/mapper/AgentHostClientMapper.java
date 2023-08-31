package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.general.api.dto.RspHostClient;
import tv.game88.general.api.entity.AgentHostClient;

public interface AgentHostClientMapper extends BaseMapper<AgentHostClient> {
    RspHostClient findLatestHostClient( @Param( "dev" ) Integer dev);
}
