package tv.game88.platform.api.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.platform.api.dto.ReportPlamHome;
import tv.game88.platform.api.entity.ReportAgentcount;
import tv.game88.platform.api.mapper.ReportAgentcountMapper;
import tv.game88.platform.api.service.ReportAgentcountService;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 代理统计，主要用于代理渠道的统计Service业务层处理
 *
 * @author 77tv
 * @date 2021-01-26
 */
@Service
@Log4j2
public class ReportAgentcountServiceImpl implements ReportAgentcountService {
    @Resource
    private ReportAgentcountMapper reportAgentcountMapper;

    /**
     * 查询代理统计，主要用于代理渠道的统计列表
     *
     * @param reportAgentcount 代理统计，主要用于代理渠道的统计
     *
     * @return 代理统计，主要用于代理渠道的统计
     */
    @Override
    public Object selectReportAgentcountList( ReportAgentcount reportAgentcount ) throws Exception {
        String                 agenttime;
        if ( reportAgentcount.getAgenttime() == null ) {
            agenttime = LocalDateTimeUtils.format( LocalDate.now().minusDays( 1 ) );
            reportAgentcount.setAgentname( agenttime );
        } else {
            agenttime = reportAgentcount.getAgenttime();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date             date             = simpleDateFormat.parse( agenttime );
        boolean          flag             = date.before( new Date() );
        if ( !flag ) {
            agenttime = LocalDateTimeUtils.format( LocalDate.now() );
        }
        //预生成数据校验
        String nowStr = LocalDateTimeUtils.format( LocalDate.now() );
        if ( nowStr.equals( agenttime ) ) {
            //如果是当天，校验是否是一个小时之前的数据
            String s = reportAgentcountMapper.rmemberInfoLately();

            LocalDateTime time    = LocalDateTimeUtils.parseLocalDateTime( s );
            Duration      between = Duration.between( LocalDateTime.now(), time );
            if ( between.toSeconds() > 1200 ) {
                return RspBase.businessError( "请重新生成" + agenttime + "数据" );
            }
        } else {
            //昨天的数据，判断数量是否相等
            int i = reportAgentcountMapper.memberInfoCounts( agenttime + " 00:00:00", agenttime + " 23:59:59" );
            int r = reportAgentcountMapper.rmemberInfoCounts( agenttime + " 00:00:00", agenttime + " 23:59:59" );
            if ( i != r ) {
                return RspBase.businessError( "请重新生成" + agenttime + "数据" );
            }
        }
        if ( reportAgentcount.getAgentcode() != null ) {//判断代理号是否为空，代理号不为空，并且没有查询到数据，
            reportAgentcountMapper.calldataProrepPlamcom( agenttime, agenttime, reportAgentcount.getAgentcode().trim() );//调用存储过程
            List<ReportAgentcount> allList1 = reportAgentcountMapper.selectReportAgentcountList( reportAgentcount );
            return RspBase.ok( allList1 );
        }
        List<ReportAgentcount> allList = reportAgentcountMapper.selectReportAgentcountList( reportAgentcount );
        return RspBase.ok( allList );
    }

    @Override
    public List<ReportPlamHome> findChartsOne( String classTwo, String time ) {
        return reportAgentcountMapper.findChartsOne( classTwo, time );
    }

    @Override
    public RspBase<?> plamagent_data( ReportAgentcount reportAgentcount ) {
        reportAgentcountMapper.callplamagentData( reportAgentcount.getAgenttime() );
        return RspBase.ok( "预生成数据成功" );
    }

    @Override
    public List<ReportAgentcount> exportAgentcountList( ReportAgentcount reportAgentcount ) {
        return reportAgentcountMapper.selectReportAgentcountList( reportAgentcount );
    }
}
