package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayChannelMoney;
import tv.game88.pay.api.mapper.PayChannelMoneyMapper;
import tv.game88.pay.api.service.PayChannelMoneyService;

@Service
public class PayChannelMoneyServiceImpl extends ServiceImpl<PayChannelMoneyMapper, PayChannelMoney> implements PayChannelMoneyService {
}

