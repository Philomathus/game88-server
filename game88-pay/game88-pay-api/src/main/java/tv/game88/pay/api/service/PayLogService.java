package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.PayLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PayLogService extends IService<PayLog> {
    List<PayLog> selectPayLogList( PayLog payLog );
}

