package tv.game88.general.api.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.general.api.entity.ReportPlamCom;
import tv.game88.general.api.mapper.ReportPlamComMapper;
import tv.game88.general.api.service.IReportPlamComService;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Calendar;
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
public class ReportPlamComServiceImpl extends ServiceImpl<ReportPlamComMapper, ReportPlamCom> implements IReportPlamComService {
    @Resource
    private RedisUtils redisUtil;

    /**
     * 查询综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间列表
     *
     * @param reportPlamCom 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间
     *
     * @return 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间
     */
    @Override
    public Object selectReportPlamComList( ReportPlamCom reportPlamCom ) {
        String dateNowStr = LocalDateTimeUtils.format( LocalDate.now() );
        if ( Strings.isBlank( reportPlamCom.getReporttime() ) ) {
            reportPlamCom.setReporttime( dateNowStr );
        }
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add( Calendar.MINUTE, -5 );// 5分钟之前的时间
        Map<String, Object> resultMap = new HashMap<>();
        if ( reportPlamCom.getReporttime().equals( dateNowStr ) ) {
            if ( !redisUtil.exists( "admin-reportPlamCom" ) ) {
                storage( dateNowStr, reportPlamCom.getAgentPlatform() );
            }
        }

        DynamicDataSourceContextHolder.push( "slave_" + reportPlamCom.getAgentPlatform() );

        List<ReportPlamCom> allList = this.baseMapper.selectReportPlamComList( reportPlamCom );

        DynamicDataSourceContextHolder.poll();

        resultMap.put( "rows", allList );
        return resultMap;
    }

    @Override
    public List<ReportPlamCom> exportPlamComList( ReportPlamCom reportPlamCom ) {
        DynamicDataSourceContextHolder.push( "slave_" + reportPlamCom.getAgentPlatform() );

        List<ReportPlamCom> allList = this.baseMapper.selectReportPlamComList( reportPlamCom );

        DynamicDataSourceContextHolder.poll();
        return allList;
    }

    public void storage( String dateNowStr, String reportPlamCom ) {
        redisUtil.strSet( "admin-reportPlamCom", "0", Duration.ofMinutes( 5 ) );
        DynamicDataSourceContextHolder.push( "slave_" + reportPlamCom );

        this.baseMapper.calldataProrepPlamcom( dateNowStr );

        DynamicDataSourceContextHolder.poll();
    }
}