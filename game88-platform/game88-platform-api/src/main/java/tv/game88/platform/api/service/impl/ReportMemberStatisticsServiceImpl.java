package tv.game88.platform.api.service.impl;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.platform.api.constant.RecordConstants;
import tv.game88.platform.api.dto.RechargeStatsDto;
import tv.game88.platform.api.dto.ReqReportMemberStatistics;
import tv.game88.platform.api.mapper.ReportMemberStatisticsMapper;
import tv.game88.platform.api.service.ReportMemberStatisticsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static tv.game88.core.config.constants.Constants.MEMBER_REPORT_STATS;

@Service
public class ReportMemberStatisticsServiceImpl implements ReportMemberStatisticsService {

    @Resource
    private ReportMemberStatisticsMapper reportMemberStatisticsMapper;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public BigDecimal getTotalRecharge( ReqReportMemberStatistics req ) {
        setDefaults( req );
        return reportMemberStatisticsMapper.getTotalRecharge( req );
    }

    @Override
    public BigDecimal getTotalWithdrawal( ReqReportMemberStatistics req ) {
        setDefaults( req );
        return reportMemberStatisticsMapper.getTotalWithdrawal( req );
    }

    @Override
    public BigDecimal getUserBalance( ReqReportMemberStatistics req ) {
        setDefaults( req );
        return reportMemberStatisticsMapper.getUserBalance( req );
    }

    @Override
    public Long getTotalRegistration( ReqReportMemberStatistics req ) {
        setDefaults( req );
        return reportMemberStatisticsMapper.getTotalRegistration( req );
    }

    private void setDefaults( ReqReportMemberStatistics reqReportMemberStatistics ) {
        if ( StringUtils.isBlank( reqReportMemberStatistics.getInclusive_date() ) ) {
            reqReportMemberStatistics.setInclusive_date( LocalDateTimeUtils.format( LocalDate.now() ) );
        }
    }

    @Override
    public Long getDailyRechargeCount( ReqReportMemberStatistics req ) {
        setDefaults( req );
        return reportMemberStatisticsMapper.getDailyRechargeCount( req );
    }

    @Override
    public Long getDailyFirstRechargeCount( ReqReportMemberStatistics req ) {
        setDefaults( req );
        return reportMemberStatisticsMapper.getDailyFirstRechargeCount( req );
    }

    @Override
    public RecordConstants.RspMemberStats getMemberStats( ReqReportMemberStatistics req ) {

        setDefaults( req );

        String key = MEMBER_REPORT_STATS + req.getChannelCode() + ":" + req.getInclusive_date();

        if ( redisUtils.exists( key ) ) {
            return JsonUtil.json2Object( redisUtils.strGet( key ), RecordConstants.RspMemberStats.class );
        }

        RechargeStatsDto rechargeStatsDto = reportMemberStatisticsMapper.getRechargeStats( req );
        RecordConstants.RspMemberStats rspMemberStats = RecordConstants.RspMemberStats.builder()
                .totalRegistration( reportMemberStatisticsMapper.getTotalRegistration( req ) )
                .dailyRechargeCount( rechargeStatsDto.getDailyRechargeCount() )
                .totalRechargeAmount( rechargeStatsDto.getTotalRecharge().setScale( 2, RoundingMode.HALF_DOWN ) )
                .dailyFirstRechargeCount( reportMemberStatisticsMapper.getDailyFirstRechargeCount( req ) )
                .totalWithdrawCount( reportMemberStatisticsMapper.getDailyWithdrawCount( req ) )
                .build();

        redisUtils.strSet( key, JsonUtil.object2Json( rspMemberStats ) );
        redisUtils.expireAt( key, LocalDateTimeUtils.getEndOfToday().toInstant( ZoneOffset.ofHoursMinutes( 3, 55 ) ) );

        return rspMemberStats;
    }
}
