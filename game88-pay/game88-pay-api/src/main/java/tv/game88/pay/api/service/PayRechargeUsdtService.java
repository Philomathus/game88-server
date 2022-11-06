package tv.game88.pay.api.service;

import tv.game88.pay.api.dto.RspPayRechargeUsdt;
import tv.game88.pay.api.entity.PayRechargeUsdt;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayRechargeUsdtService extends IService<PayRechargeUsdt> {
    List<PayRechargeUsdt> selectPayRechargeUsdtList( PayRechargeUsdt payRechargeUsdt );

    List<RspPayRechargeUsdt> selectList( String memberId, Integer vip );
}

