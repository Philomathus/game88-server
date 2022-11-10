package tv.game88.platform.api.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.platform.api.dto.RspDetailCommission;
import tv.game88.platform.api.entity.LogCommission;

import java.util.List;

public interface LogCommissionMapper extends BaseMapper<LogCommission> {
    List<RspDetailCommission> findByMemberId( @Param( "memberId" ) String memberId );

    List<LogCommission> selectCommissionRecordsList( LogCommission commissionRecords );
}
