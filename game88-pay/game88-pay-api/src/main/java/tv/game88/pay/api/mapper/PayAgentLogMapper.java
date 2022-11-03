package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.PayAgentLog;

import java.util.List;

public interface PayAgentLogMapper extends BaseMapper<PayAgentLog> {
	public List<PayAgentLog> selectPayAgentLogList( PayAgentLog payAgentLog );

    List<PayAgentLog> findNoCallback();
}
