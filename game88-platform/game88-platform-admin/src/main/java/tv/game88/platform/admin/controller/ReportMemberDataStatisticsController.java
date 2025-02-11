package tv.game88.platform.admin.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.vo.RspBase;
import tv.game88.common.vo.RspEntity;
import tv.game88.platform.api.constant.RecordConstants;
import tv.game88.platform.api.dto.ReqReportMemberStatistics;
import tv.game88.platform.api.service.ReportMemberStatisticsService;

import java.math.BigDecimal;

@Anonymous
@RestController
@RequestMapping( "/report/memberData" )
public class ReportMemberDataStatisticsController extends BaseController {

    @Resource
    private ReportMemberStatisticsService reportMemberStatisticsService;

    @PostMapping( "/recharge" )
    public RspEntity<BigDecimal> getTotalRecharge( @RequestBody ReqReportMemberStatistics req ) {
        BigDecimal result = reportMemberStatisticsService.getTotalRecharge( req );
        return RspEntity.ok( result == null ? BigDecimal.ZERO : result );
    }

    @PostMapping( "/withdrawal" )
    public RspEntity<BigDecimal> getTotalWithdrawal( @RequestBody ReqReportMemberStatistics req ) {
        BigDecimal result = reportMemberStatisticsService.getTotalWithdrawal( req );
        return RspEntity.ok( result == null ? BigDecimal.ZERO : result );
    }

    @PostMapping( "/balance" )
    public RspEntity<BigDecimal> getUserBalance( @RequestBody ReqReportMemberStatistics req ) {
        BigDecimal result = reportMemberStatisticsService.getUserBalance( req );
        return RspEntity.ok( result == null ? BigDecimal.ZERO : result );
    }

    @PostMapping( "/registration" )
    public RspEntity<Long> getTotalRegistration( @RequestBody ReqReportMemberStatistics req ) {
        Long result = reportMemberStatisticsService.getTotalRegistration( req );
        return RspEntity.ok( result == null ? 0L : result );
    }

    @PostMapping( "/dailyFirstRechargeCount" )
    public RspEntity<Long> getDailyFirstRechargeCount( @RequestBody ReqReportMemberStatistics req ) {
        return RspEntity.ok( reportMemberStatisticsService.getDailyFirstRechargeCount( req ) );
    }

    @PostMapping( "/dailyRechargeCount" )
    public RspEntity<Long> getDailyRechargeCount( @RequestBody ReqReportMemberStatistics req ) {
        return RspEntity.ok( reportMemberStatisticsService.getDailyRechargeCount( req ) );
    }

    @PostMapping( "/gift" )
    public RspEntity<BigDecimal> getTotalGift( @RequestBody ReqReportMemberStatistics req ) {
        return RspEntity.ok( BigDecimal.ZERO );
    }

    @PostMapping( "/getMemberStats" )
    public RspBase<RecordConstants.RspMemberStats> getMemberStats( @RequestBody ReqReportMemberStatistics req ) {
        return RspBase.ok( reportMemberStatisticsService.getMemberStats( req ) );
    }
}
