package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.PayPlatform;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayPlatformService extends IService<PayPlatform> {
    List<PayPlatform> selectPayPlatformList( PayPlatform payPlatform );
}

