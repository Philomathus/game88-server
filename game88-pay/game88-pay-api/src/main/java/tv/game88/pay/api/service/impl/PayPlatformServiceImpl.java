package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.mapper.PayPlatformMapper;
import tv.game88.pay.api.service.PayPlatformService;

import java.util.List;

@Service
public class PayPlatformServiceImpl extends ServiceImpl<PayPlatformMapper, PayPlatform> implements PayPlatformService {
    @Override
    public List<PayPlatform> selectPayPlatformList( PayPlatform payPlatform ) {
        List<PayPlatform> payPlatforms = this.baseMapper.selectPayPlatformList( payPlatform );
        for ( PayPlatform platform : payPlatforms ) {
            platform.setSignMd5( null );
            platform.setSignPublicKey( null );
            platform.setSignPrivateKey( null );
        }
        return payPlatforms;
    }
}

