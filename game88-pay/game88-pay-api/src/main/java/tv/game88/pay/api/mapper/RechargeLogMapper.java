package tv.game88.pay.api.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.RechargeLog;

import java.util.List;

/**
 * 活动信息Mapper接口
 *
 * @author Rajesh
 * @date 2023-05-20
 */
public interface RechargeLogMapper extends BaseMapper<RechargeLog> {
	
	/**
	 * 选择所有充值标志
	 * select recharge log list
	 * @param rechargeLog 选择所有充值标志
	 * @return 充值日志 信息采集
	 */
	public List<RechargeLog> selectRechargeLogList( RechargeLog rechargeLog);

}
