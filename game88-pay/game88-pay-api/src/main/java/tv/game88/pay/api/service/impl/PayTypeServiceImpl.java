package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayType;
import tv.game88.pay.api.mapper.PayTypeMapper;
import tv.game88.pay.api.service.PayTypeService;

@Service
public class PayTypeServiceImpl extends ServiceImpl<PayTypeMapper, PayType> implements PayTypeService {
}

