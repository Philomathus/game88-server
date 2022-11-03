package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.PayType;

import java.util.List;

public interface PayTypeMapper extends BaseMapper<PayType> {
    public List<PayType> selectPayTypeList( PayType payType );

    List<PayType> selectCachePayTypeList();
}
