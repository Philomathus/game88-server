package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayRechargeBank;
import tv.game88.pay.api.mapper.PayRechargeBankMapper;
import tv.game88.pay.api.service.PayRechargeBankService;

import java.util.List;

@Service
public class PayRechargeBankServiceImpl extends ServiceImpl<PayRechargeBankMapper, PayRechargeBank> implements PayRechargeBankService {
    @Override
    public List<PayRechargeBank> selectPayRechargeBankList( PayRechargeBank payRechargeBank ) {
        return this.baseMapper.selectPayRechargeBankList( payRechargeBank );
    }
}

