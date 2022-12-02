package tv.game88.platform.api.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.ReportIncomeDay;
import tv.game88.platform.api.mapper.ReportIncomeDayMapper;
import tv.game88.platform.api.service.ReportIncomeDayService;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ReportIncomeDayServiceImpl implements ReportIncomeDayService {
    @Resource
    private ReportIncomeDayMapper reportIncomeDayMapper;

    @Override
    public List<ReportIncomeDay> selectReportIncomeDayList( ReportIncomeDay reportIncomeDay ) {
        return reportIncomeDayMapper.selectReportIncomeDayList( reportIncomeDay );
    }

    @Override
    public ReportIncomeDay countSuccessData( ReportIncomeDay reportIncomeDay ) {
        return reportIncomeDayMapper.countSuccessMoney( reportIncomeDay );
    }
}