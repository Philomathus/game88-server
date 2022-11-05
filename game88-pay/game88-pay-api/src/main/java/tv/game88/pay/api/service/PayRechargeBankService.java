package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.PayRechargeBank;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayRechargeBankService extends IService<PayRechargeBank> {
    List<PayRechargeBank> selectPayRechargeBankList( PayRechargeBank payRechargeBank );
}

