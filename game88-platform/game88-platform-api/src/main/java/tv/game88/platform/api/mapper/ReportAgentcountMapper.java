package tv.game88.platform.api.mapper;

import org.apache.ibatis.annotations.Param;
import tv.game88.platform.api.dto.ReportPlamHome;
import tv.game88.platform.api.entity.ReportAgentcount;

import java.util.List;

/**
 * 代理统计，主要用于代理渠道的统计Mapper接口
 *
 * @author 77tv
 * @date 2021-01-26
 */
public interface ReportAgentcountMapper {

    /**
     * 查询代理统计，主要用于代理渠道的统计列表
     *
     * @param reportAgentcount 代理统计，主要用于代理渠道的统计
     *
     * @return 代理统计，主要用于代理渠道的统计集合
     */
    public List<ReportAgentcount> selectReportAgentcountList( ReportAgentcount reportAgentcount );

    String calldataProrepPlamcom( @Param( "beginTime" ) String beginTime, @Param( "endTime" ) String endTime, @Param(
            "agentcode" ) String agentcode );

    List<ReportPlamHome> findChartsOne( @Param( "classTwo" ) String classTwo, @Param( "time" ) String time );

    //List<RspMemberAgent> selectMemberAgent( ReportAgentcount reportAgentcount );

    String callplamagentData( @Param( "p_begintime" ) String p_begintime );

    int memberInfoCounts( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime );

    int rmemberInfoCounts( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime );

    String rmemberInfoLately();
}
