package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.PayAgentPlatform;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayAgentPlatformService extends IService<PayAgentPlatform> {
    List<PayAgentPlatform> selectPayAgentPlatformList( PayAgentPlatform payAgentPlatform );
}

