package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.pay.api.entity.PayType;
import tv.game88.pay.api.mapper.PayTypeMapper;
import tv.game88.pay.api.service.PayTypeService;

import java.util.List;

@Service
public class PayTypeServiceImpl extends ServiceImpl<PayTypeMapper, PayType> implements PayTypeService {
    @Override
    public List<PayType> selectPayTypeList( PayType payType ) {
        List<PayType> payTypes = this.baseMapper.selectPayTypeList( payType );
        String        domain   = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( PayType type : payTypes ) {
            type.setIconUrl( domain + payType.getIconUrl() );
        }
        return payTypes;
    }
}

