package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.mapper.PayAgentPlatformMapper;
import tv.game88.pay.api.service.PayAgentPlatformService;

import java.util.List;

@Service
public class PayAgentPlatformServiceImpl extends ServiceImpl<PayAgentPlatformMapper, PayAgentPlatform> implements PayAgentPlatformService {
    @Override
    public List<PayAgentPlatform> selectPayAgentPlatformList( PayAgentPlatform payAgentPlatform ) {
        return this.baseMapper.selectPayAgentPlatformList( payAgentPlatform );
    }
}

