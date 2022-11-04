package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.PayAgentLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayAgentLogService extends IService<PayAgentLog> {
    List<PayAgentLog> selectPayAgentLogList( PayAgentLog payAgentLog );
}

