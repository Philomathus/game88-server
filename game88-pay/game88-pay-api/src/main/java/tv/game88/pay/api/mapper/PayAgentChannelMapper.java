package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.PayAgentChannel;

import java.util.List;

public interface PayAgentChannelMapper extends BaseMapper<PayAgentChannel> {
	public List<PayAgentChannel> selectPayAgentChannelList(PayAgentChannel payAgentChannel);
}
