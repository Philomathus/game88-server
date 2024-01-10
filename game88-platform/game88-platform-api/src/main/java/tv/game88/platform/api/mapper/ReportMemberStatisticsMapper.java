package tv.game88.platform.api.mapper;

import org.apache.ibatis.annotations.Param;
import tv.game88.platform.api.dto.ReqReportMemberStatistics;

import java.math.BigDecimal;

public interface ReportMemberStatisticsMapper {
    BigDecimal getTotalRecharge(@Param("req") ReqReportMemberStatistics req);

    BigDecimal getTotalWithdrawal(@Param("req") ReqReportMemberStatistics req);

    BigDecimal getUserBalance(@Param("req") ReqReportMemberStatistics req);

    Long getTotalRegistration(@Param("req") ReqReportMemberStatistics req);
}
