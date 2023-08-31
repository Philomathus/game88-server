package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.general.api.dto.RspAgent;
import tv.game88.general.api.entity.AgentHost;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 77tv
 * @since 2021-03-11
 */
public interface AgentHostMapper extends BaseMapper<AgentHost> {
    RspAgent findAgentHost( @Param( "code" ) String agent);

}
