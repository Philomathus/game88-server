package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.dto.RspPayChannel;
import tv.game88.pay.api.dto.RspPayChannelName;
import tv.game88.pay.api.entity.PayChannel;

import java.util.List;
import java.util.Set;

public interface PayChannelMapper extends BaseMapper<PayChannel> {
    public List<PayChannel> selectPayChannelList( PayChannel payChannel );

    List<RspPayChannel> selectRspListByTypeId( Long typeId );

    List<RspPayChannelName> selectPayChannelName( @Param( "array" ) Set<Long> channelIds );
}
