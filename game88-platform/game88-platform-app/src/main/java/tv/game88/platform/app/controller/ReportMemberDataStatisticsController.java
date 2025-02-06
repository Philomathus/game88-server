package tv.game88.platform.app.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.vo.RspBase;
import tv.game88.platform.api.constant.RecordConstants;
import tv.game88.platform.api.dto.ReqReportMemberStatistics;
import tv.game88.platform.api.service.ReportMemberStatisticsService;

@Anonymous
@RestController
@RequestMapping( "/report/memberData" )
public class ReportMemberDataStatisticsController {

    @Resource
    private ReportMemberStatisticsService reportMemberStatisticsService;

    @PostMapping( "/getMemberStats" )
    @Anonymous
    public RspBase<RecordConstants.RspMemberStats> getMemberStats( @RequestBody ReqReportMemberStatistics req ) {
        return RspBase.ok( reportMemberStatisticsService.getMemberStats( req ) );
    }
}
