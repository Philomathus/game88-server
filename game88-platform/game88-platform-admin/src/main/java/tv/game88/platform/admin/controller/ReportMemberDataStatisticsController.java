package tv.game88.platform.admin.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspEntity;
import tv.game88.platform.api.dto.ReqReportMemberStatistics;
import tv.game88.platform.api.service.ReportMemberStatisticsService;

import java.math.BigDecimal;

@RestController
@RequestMapping( "/report/memberData" )
public class ReportMemberDataStatisticsController extends BaseController {

    @Resource
    private ReportMemberStatisticsService reportMemberStatisticsService;

    @PostMapping( "/recharge" )
    public RspEntity<BigDecimal> getTotalRecharge(@RequestBody ReqReportMemberStatistics req) {
        return RspEntity.ok( reportMemberStatisticsService.getTotalRecharge(req) );
    }

    @PostMapping( "/withdrawal" )
    public RspEntity<BigDecimal> getTotalWithdrawal(@RequestBody ReqReportMemberStatistics req) {
        return RspEntity.ok( reportMemberStatisticsService.getTotalWithdrawal(req));
    }

    @PostMapping( "/balance" )
    public RspEntity<BigDecimal> getUserBalance(@RequestBody ReqReportMemberStatistics req) {
        return RspEntity.ok( reportMemberStatisticsService.getUserBalance(req) );
    }

    @PostMapping( "/registration" )
    public RspEntity<Long> getTotalRegistration( @RequestBody ReqReportMemberStatistics req) {
        return RspEntity.ok( reportMemberStatisticsService.getTotalRegistration(req) );
    }

}
