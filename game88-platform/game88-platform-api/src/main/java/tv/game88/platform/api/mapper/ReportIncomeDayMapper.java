package tv.game88.platform.api.mapper;

import org.apache.ibatis.annotations.Param;
import tv.game88.platform.api.entity.ReportIncomeDay;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author 77tv
 * @date 2021-01-26
 */
public interface ReportIncomeDayMapper {
    public List<ReportIncomeDay> selectReportIncomeDayList( ReportIncomeDay reportIncomeDay );

    String calldataProrepPlamcom( @Param( "timedateta" ) String timedateta );

    ReportIncomeDay countSuccessMoney( ReportIncomeDay reportIncomeDay );
}