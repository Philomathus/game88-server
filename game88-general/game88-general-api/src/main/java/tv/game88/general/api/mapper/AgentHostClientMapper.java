package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.general.api.dto.RspHostClient;
import tv.game88.general.api.entity.AgentHostClient;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 77tv
 * @since 2021-03-18
 */
public interface AgentHostClientMapper extends BaseMapper<AgentHostClient> {
    RspHostClient findLatestHostClient( @Param( "dev" ) Integer dev);
}
