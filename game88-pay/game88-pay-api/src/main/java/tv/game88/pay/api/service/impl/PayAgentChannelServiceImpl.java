package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.mapper.PayAgentChannelMapper;
import tv.game88.pay.api.service.PayAgentChannelService;

import java.util.List;

@Service
public class PayAgentChannelServiceImpl extends ServiceImpl<PayAgentChannelMapper, PayAgentChannel> implements PayAgentChannelService {
    @Override
    public List<PayAgentChannel> selectPayAgentChannelList( PayAgentChannel payAgentChannel ) {
        List<PayAgentChannel> payAgentChannelList = this.baseMapper.selectPayAgentChannelList( payAgentChannel );
        for ( PayAgentChannel agentChannel : payAgentChannelList ) {
            agentChannel.setHeaderValue( null );
            agentChannel.setSignMd5( null );
            agentChannel.setSignPublicKey( null );
            agentChannel.setSignPrivateKey( null );
        }
        return payAgentChannelList;
    }
}

