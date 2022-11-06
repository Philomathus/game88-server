package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.dto.RspPayRechargeUsdt;
import tv.game88.pay.api.entity.PayRechargeUsdt;

import java.util.List;

public interface PayRechargeUsdtMapper extends BaseMapper<PayRechargeUsdt> {
    public List<PayRechargeUsdt> selectPayRechargeUsdtList( PayRechargeUsdt payRechargeUsdt );

    List<RspPayRechargeUsdt> selectEffectRspList();
}