package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.member.entity.LogMoney;

import java.util.List;
import java.util.Map;

/**
 * 资金日志Service接口
 *
 * @author 77lm
 * @date 2021-10-09
 */
public interface LogMoneyService extends IService<LogMoney> {
    /**
     * 查询资金日志列表
     *
     * @param logMoney 资金日志
     *
     * @return 资金日志集合
     */
    List<LogMoney> selectLogMoneyList( LogMoney logMoney );

    /**
     * @param logMoney 资金日志
     *
     * @return RspBase
     */
    Map listCount( LogMoney logMoney );

    /**
     * @param logMoney 资金日志
     *
     * @return RspBase
     */
    Map totalCount( LogMoney logMoney );

}
