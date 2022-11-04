package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayLog;
import tv.game88.pay.api.mapper.PayLogMapper;
import tv.game88.pay.api.service.PayLogService;

import java.util.List;

@Service
public class PayLogServiceImpl extends ServiceImpl<PayLogMapper, PayLog> implements PayLogService {
    @Override
    public List<PayLog> selectPayLogList( PayLog payLog ) {
        return this.baseMapper.selectPayLogList( payLog );
    }
}

