package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.LogCommission;
import tv.game88.platform.api.mapper.LogCommissionMapper;
import tv.game88.platform.api.service.CommissionRecordsService;

import java.util.List;

/**
 * 佣金领取记录service实现
 * @author aleng
 */
@Service
public class CommissionRecordsServiceImpl extends ServiceImpl<LogCommissionMapper, LogCommission> implements CommissionRecordsService {

    /**
     * 查询佣金领取日志列表
     *
     * @param commissionRecords 佣金领取日志
     * @return 佣金领取日志
     */
    @Override
    public List<LogCommission> selectLogCommissionList(LogCommission commissionRecords) {
        return this.baseMapper.selectCommissionRecordsList(commissionRecords);
    }
}
