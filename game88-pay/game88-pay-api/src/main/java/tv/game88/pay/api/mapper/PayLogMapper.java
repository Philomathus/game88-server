package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.PayLog;

import java.util.List;

public interface PayLogMapper extends BaseMapper<PayLog> {
	public List<PayLog> selectPayLogList(PayLog payLog);
}
