package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.PayPlatform;

import java.util.List;

public interface PayPlatformMapper extends BaseMapper<PayPlatform> {

	public List<PayPlatform> selectPayPlatformList(PayPlatform PayPlatform);
}
