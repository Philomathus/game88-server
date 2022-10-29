package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.PayAgentLog;

import java.util.List;

public interface PayAgentLogMapper extends BaseMapper<PayAgentLog> {
	/**
	 * 查询代付信息日志列表
	 *
	 * @param payAgentLog 代付信息日志
	 * @return 代付信息日志集合
	 */
	public List<PayAgentLog> selectPayAgentLogList( PayAgentLog payAgentLog );
}
