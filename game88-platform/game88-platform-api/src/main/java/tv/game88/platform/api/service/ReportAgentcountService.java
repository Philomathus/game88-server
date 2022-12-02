package tv.game88.platform.api.service;


import tv.game88.common.vo.RspBase;
import tv.game88.platform.api.dto.ReportPlamHome;
import tv.game88.platform.api.entity.ReportAgentcount;

import java.util.List;

/**
 * 代理统计，主要用于代理渠道的统计Service接口
 *
 * @author 77tv
 * @date 2021-01-26
 */
public interface ReportAgentcountService {
    public Object selectReportAgentcountList( ReportAgentcount reportAgentcount ) throws Exception;

    List<ReportPlamHome> findChartsOne( String classTwo, String time );

    RspBase<?> plamagent_data( ReportAgentcount reportAgentcount );

    List<ReportAgentcount> exportAgentcountList( ReportAgentcount reportAgentcount );

}
