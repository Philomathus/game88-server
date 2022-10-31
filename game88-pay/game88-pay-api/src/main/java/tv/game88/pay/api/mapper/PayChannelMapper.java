package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.PayChannel;

import java.util.List;

public interface PayChannelMapper extends BaseMapper<PayChannel> {
	public List<PayChannel> selectPayChannelList(PayChannel payChannel);

}
