package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.pay.api.entity.PayRechargeUsdt;
import tv.game88.pay.api.mapper.PayRechargeUsdtMapper;
import tv.game88.pay.api.service.PayRechargeUsdtService;

import java.util.List;

@Service
public class PayRechargeUsdtServiceImpl extends ServiceImpl<PayRechargeUsdtMapper, PayRechargeUsdt> implements PayRechargeUsdtService {
    @Override
    public List<PayRechargeUsdt> selectPayRechargeUsdtList( PayRechargeUsdt payRechargeUsdt ) {
        List<PayRechargeUsdt> payRechargeUsdts = this.baseMapper.selectPayRechargeUsdtList( payRechargeUsdt );
        String                domainValue = ConfigDomainCacheUtil.me.getValue( "domain.oss" );
        if ( !CollectionUtils.isEmpty( payRechargeUsdts ) ) {
            for ( PayRechargeUsdt info : payRechargeUsdts ) {
                if ( StringUtils.isNotBlank( info.getIcon() ) && !info.getIcon().startsWith( "http" ) ) {
                    info.setIcon( domainValue + info.getIcon() );
                }
            }
        }
        return payRechargeUsdts;
    }
}

