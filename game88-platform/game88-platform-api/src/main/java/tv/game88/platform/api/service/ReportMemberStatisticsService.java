package tv.game88.platform.api.service;

import tv.game88.platform.api.dto.ReqReportMemberStatistics;

import java.math.BigDecimal;

public interface ReportMemberStatisticsService {

    BigDecimal getTotalRecharge(ReqReportMemberStatistics req );

    BigDecimal getTotalWithdrawal( ReqReportMemberStatistics req );

    BigDecimal getUserBalance( ReqReportMemberStatistics req );

    Long getTotalRegistration( ReqReportMemberStatistics req );

     Long getDailyRechargeCount( ReqReportMemberStatistics req );

    Long getDailyFirstRechargeCount( ReqReportMemberStatistics req );
}
