package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.mapper.PayPlatformMapper;
import tv.game88.pay.api.service.PayPlatformService;

@Service
public class PayPlatformServiceImpl extends ServiceImpl<PayPlatformMapper, PayPlatform> implements PayPlatformService {
}

