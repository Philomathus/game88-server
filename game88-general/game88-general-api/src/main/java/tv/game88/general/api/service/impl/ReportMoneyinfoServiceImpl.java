package tv.game88.general.api.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.general.api.entity.ReportMoneyinfo;
import tv.game88.general.api.mapper.ReportMoneyinfoMapper;
import tv.game88.general.api.service.IReportMoneyinfoService;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额Service业务层处理
 *
 * @author 77tv
 * @date 2021-01-25
 */
@Service
@Log4j2
public class ReportMoneyinfoServiceImpl extends ServiceImpl<ReportMoneyinfoMapper, ReportMoneyinfo> implements IReportMoneyinfoService {

    /**
     * 查询平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额列表
     *
     * @param reportMoneyinfo 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额
     *
     * @return 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额
     */
    @Override
    public Object selectReportMoneyinfoList( ReportMoneyinfo reportMoneyinfo ) throws ParseException {
        log.info( "请求参数对象:" + reportMoneyinfo );
        String dateNowStr = LocalDateTimeUtils.format( LocalDate.now() );
        setSelectTime( dateNowStr, reportMoneyinfo );//如果不选择日期查询最近7天的数据
        String beginTime = ( String ) reportMoneyinfo.getParams().get( "beginTime" );
        log.info( "beginTime:" + beginTime );
        String endTime = ( String ) reportMoneyinfo.getParams().get( "endTime" );
        log.info( "endTime:" + endTime );
        Map<String, Object> resultMap = new HashMap<>();

        DynamicDataSourceContextHolder.push( "slave_" + reportMoneyinfo.getAgentPlatform() );

        log.info( "请求对象:" + reportMoneyinfo.getParams().get( "beginTime" ) + "," + reportMoneyinfo.getParams()
                                                                                                      .get( "endTime" ) );

        List<ReportMoneyinfo> allList = this.baseMapper.selectReportMoneyinfoList( reportMoneyinfo );
        log.info( "查询结果:" + allList );

        DynamicDataSourceContextHolder.poll();
        resultMap.put( "rows", allList );
        return resultMap;
    }

    //统计表头数据
    @Override
    public ReportMoneyinfo countMoneyData( ReportMoneyinfo reportMoneyinfo ) throws ParseException {
        String dateNowStr = LocalDateTimeUtils.format( LocalDate.now() );
        setSelectTime( dateNowStr, reportMoneyinfo );//首次进入查询7天的数据
        String           beginTime        = ( String ) reportMoneyinfo.getParams().get( "beginTime" );
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date             date             = simpleDateFormat.parse( beginTime );
        boolean          flag             = date.before( new Date() );
        if ( !flag ) {
            reportMoneyinfo.setPaymentAmount( BigDecimal.ZERO );
            reportMoneyinfo.setOutMoney( BigDecimal.ZERO );
            reportMoneyinfo.setCountMoney( BigDecimal.ZERO );
            reportMoneyinfo.setTotalAccountGifts( BigDecimal.ZERO );
            return reportMoneyinfo;
        }
        DynamicDataSourceContextHolder.push( "slave_" + reportMoneyinfo.getAgentPlatform() );

        ReportMoneyinfo reportMoneyinfo1 = this.baseMapper.countMoneyInfoData( reportMoneyinfo );

        DynamicDataSourceContextHolder.poll();
        if ( !ObjectUtils.isEmpty( reportMoneyinfo1 ) ) {
            BigDecimal paymentAmount = reportMoneyinfo1.getPaymentAmount();//入款总金额
            BigDecimal outMoney      = reportMoneyinfo1.getOutMoney();//出款总金额
            reportMoneyinfo1.setCountMoney( paymentAmount.subtract( outMoney ) );
            return reportMoneyinfo1;
        } else {
            reportMoneyinfo.setPaymentAmount( BigDecimal.ZERO );
            reportMoneyinfo.setOutMoney( BigDecimal.ZERO );
            reportMoneyinfo.setCountMoney( BigDecimal.ZERO );
            reportMoneyinfo.setTotalAccountGifts( BigDecimal.ZERO );
            return reportMoneyinfo;
        }
    }

    @Override
    public List<ReportMoneyinfo> exportMoneyinfoList( ReportMoneyinfo reportMoneyinfo ) {
        DynamicDataSourceContextHolder.push( "slave_" + reportMoneyinfo.getAgentPlatform() );

        List<ReportMoneyinfo> allList = this.baseMapper.selectReportMoneyinfoList( reportMoneyinfo );

        DynamicDataSourceContextHolder.poll();
        return allList;
    }

    private void setSelectTime( String dateNowStr, ReportMoneyinfo reportMoneyinfo ) {
        if ( null == reportMoneyinfo.getParams() || reportMoneyinfo.getParams().isEmpty()
                || reportMoneyinfo.getParams().get( "beginTime" ) == "" ) {
            log.info( "我进来了" );
            HashMap m = new HashMap<>();
            m.put( "beginTime", LocalDateTimeUtils.format( LocalDate.now().minusDays( 7 ) ) );
            m.put( "endTime", dateNowStr );
            reportMoneyinfo.setParams( m );
        }
    }
}