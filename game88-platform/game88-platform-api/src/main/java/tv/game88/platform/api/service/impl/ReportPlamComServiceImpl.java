package tv.game88.platform.api.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.platform.api.entity.ReportPlamCom;
import tv.game88.platform.api.mapper.ReportPlamComMapper;
import tv.game88.platform.api.service.ReportPlamComService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间Service业务层处理
 *
 * @author 77tv
 * @date 2021-01-25
 */
@Log4j2
@Service
public class ReportPlamComServiceImpl implements ReportPlamComService {
    @Resource
    private ReportPlamComMapper reportPlamComMapper;
    @Resource
    private RedisUtils          redisUtils;

    /**
     * 查询综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间列表
     *
     * @param reportPlamCom 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间
     *
     * @return 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间
     */
    @Override
    public Object selectReportPlamComList( ReportPlamCom reportPlamCom ) {
        LocalDate date = LocalDate.now().minusDays( 2 );
        if ( Strings.isBlank( reportPlamCom.getReporttime() ) ) {
            reportPlamCom.setReporttime( LocalDateTimeUtils.format( LocalDate.now() ) );
        }
        Map<String, Object> resultMap = new HashMap<>();
        if ( LocalDateTimeUtils.parseLocalDate( reportPlamCom.getReporttime() ).isAfter( date ) ) {
            if ( !redisUtils.exists( "admin-reportPlamCom" + reportPlamCom.getReporttime() ) ) {
                storage( reportPlamCom.getReporttime() );
            }
        }
        List<ReportPlamCom> allList = reportPlamComMapper.selectReportPlamComList( reportPlamCom );
        resultMap.put( "rows", allList );
        return resultMap;
    }

    @Override
    public List<ReportPlamCom> exportPlamComList( ReportPlamCom reportPlamCom ) {
        return reportPlamComMapper.selectReportPlamComList( reportPlamCom );
    }

    public void storage( String dateNowStr ) {
        redisUtils.strSet( "admin-reportPlamCom" + dateNowStr, "0", Duration.ofMinutes( 5 ) );
        reportPlamComMapper.calldataProrepPlamcom( dateNowStr );
    }

}