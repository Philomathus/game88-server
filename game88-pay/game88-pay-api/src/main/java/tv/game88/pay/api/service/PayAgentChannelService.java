package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.PayAgentChannel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayAgentChannelService extends IService<PayAgentChannel> {
    List<PayAgentChannel> selectPayAgentChannelList( PayAgentChannel payAgentChannel );
}

