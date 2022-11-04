package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.mapper.PayAgentLogMapper;
import tv.game88.pay.api.service.PayAgentLogService;

import java.util.List;

@Service
public class PayAgentLogServiceImpl extends ServiceImpl<PayAgentLogMapper, PayAgentLog> implements PayAgentLogService {
    @Override
    public List<PayAgentLog> selectPayAgentLogList( PayAgentLog payAgentLog ) {
        return this.baseMapper.selectPayAgentLogList( payAgentLog );
    }
}

