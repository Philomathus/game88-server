package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.util.List;


/**
 * 代付平台Mapper接口
 *
 * @author 77tv
 * @date 2021-01-26
 */
public interface PayAgentPlatformMapper extends BaseMapper<PayAgentPlatform> {

    public List<PayAgentPlatform> selectPayAgentPlatformList( PayAgentPlatform payAgentPlatform );

    PayAgentPlatform selectByCode( @Param( "code" ) String code );
}
