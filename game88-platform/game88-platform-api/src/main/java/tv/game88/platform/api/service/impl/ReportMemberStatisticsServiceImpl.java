package tv.game88.platform.api.service.impl;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.platform.api.dto.ReqReportMemberStatistics;
import tv.game88.platform.api.mapper.ReportMemberStatisticsMapper;
import tv.game88.platform.api.service.ReportMemberStatisticsService;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ReportMemberStatisticsServiceImpl implements ReportMemberStatisticsService {

    @Resource
    private ReportMemberStatisticsMapper reportMemberStatisticsMapper;

    @Override
    public BigDecimal getTotalRecharge( ReqReportMemberStatistics req ) {
        setDefaults(req);
        return reportMemberStatisticsMapper.getTotalRecharge(req) ;
    }

    @Override
    public BigDecimal getTotalWithdrawal( ReqReportMemberStatistics req ) {
        setDefaults(req);
        return reportMemberStatisticsMapper.getTotalWithdrawal(req);
    }

    @Override
    public BigDecimal getUserBalance( ReqReportMemberStatistics req ) {
        setDefaults(req);
        return reportMemberStatisticsMapper.getUserBalance(req);
    }

    @Override
    public Long getTotalRegistration( ReqReportMemberStatistics req ) {
        setDefaults(req);
        return reportMemberStatisticsMapper.getTotalRegistration(req);
    }

    private void setDefaults(ReqReportMemberStatistics reqReportMemberStatistics) {
        if( StringUtils.isBlank(reqReportMemberStatistics.getInclusive_date()) ){
            reqReportMemberStatistics.setInclusive_date( LocalDateTimeUtils.format( LocalDate.now() ) );
        }
    }

    @Override
    public Long getDailyRechargeCount( ReqReportMemberStatistics req ) {
        setDefaults(req);
        return reportMemberStatisticsMapper.getDailyRechargeCount(req) ;
    }

    @Override
    public Long getDailyFirstRechargeCount( ReqReportMemberStatistics req ) {
        setDefaults(req);
        return reportMemberStatisticsMapper.getDailyFirstRechargeCount(req) ;
    }
}
