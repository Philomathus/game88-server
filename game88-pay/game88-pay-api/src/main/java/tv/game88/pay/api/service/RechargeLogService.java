package tv.game88.pay.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.pay.api.entity.RechargeLog;

import java.util.List;

/**
 * 充值日志服务 interface
 *
 * @author Rajesh
 * @date 2023-05-20
 */
public interface RechargeLogService extends IService<RechargeLog> {

    /**
     * 充值日志列表 - recharge log list
     *
     * @param rechargeLog  - list of recharge log
     * @return 返回充值日志列表 - ist of recharge log
     */
    List<RechargeLog> selectAllRechargeLog( RechargeLog rechargeLog);

}
