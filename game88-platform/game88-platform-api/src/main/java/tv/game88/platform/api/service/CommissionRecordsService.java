package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.LogCommission;

import java.util.List;


/**
 * 推广佣金领取记录service
 *
 * @author aleng
 */
public interface CommissionRecordsService extends IService<LogCommission> {
    /**
     * 查询佣金领取日志列表
     *
     * @param commissionRecords 佣金领取日志
     *
     * @return 佣金领取日志集合
     */
    public List<LogCommission> selectLogCommissionList( LogCommission commissionRecords );

}
