package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.mapper.PayChannelMapper;
import tv.game88.pay.api.service.PayChannelService;

import java.util.List;

@Service
public class PayChannelServiceImpl extends ServiceImpl<PayChannelMapper, PayChannel> implements PayChannelService {
    @Override
    public List<PayChannel> selectPayChannelList( PayChannel payChannel ) {
        return this.baseMapper.selectPayChannelList( payChannel );
    }
}

