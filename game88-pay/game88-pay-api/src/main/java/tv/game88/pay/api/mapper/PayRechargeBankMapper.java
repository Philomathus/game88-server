package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.dto.RspPayRechargeBank;
import tv.game88.pay.api.entity.PayRechargeBank;

import java.util.List;

public interface PayRechargeBankMapper extends BaseMapper<PayRechargeBank> {

	public List<PayRechargeBank> selectPayRechargeBankList( PayRechargeBank payRechargeBank );

    List<RspPayRechargeBank> selectRspList( Integer vip );
}
