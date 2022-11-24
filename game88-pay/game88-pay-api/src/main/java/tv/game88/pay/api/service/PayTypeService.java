package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.PayType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayTypeService extends IService<PayType> {
    List<PayType> selectPayTypeList( PayType payType );

    Long minId();
}

