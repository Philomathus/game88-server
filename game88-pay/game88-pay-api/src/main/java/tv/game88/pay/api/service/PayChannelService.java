package tv.game88.pay.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.entity.PayChannel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayChannelService extends IService<PayChannel> {
    List<PayChannel> selectPayChannelList( PayChannel payChannel );

    RspBase<?> insertPayChannel( PayChannel payChannel );

    RspBase<?> updatePayChannel( PayChannel payChannel );

    RspBase<?> updateEffect( Long id, Boolean effect );
}

