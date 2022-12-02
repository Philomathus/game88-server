package tv.game88.platform.api.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.platform.api.entity.ReportMoneyinfo;
import tv.game88.platform.api.mapper.ReportMoneyinfoMapper;
import tv.game88.platform.api.service.ReportMoneyinfoService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;


/**
 * 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额Service业务层处理
 *
 * @author 77tv
 * @date 2021-01-25
 */
@Service
public class ReportMoneyinfoServiceImpl implements ReportMoneyinfoService {
    @Resource
    private ReportMoneyinfoMapper reportMoneyinfoMapper;

    /**
     * 查询平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额列表
     *
     * @param reportMoneyinfo 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额
     *
     * @return 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额
     */
    @Override
    public Object selectReportMoneyinfoList( ReportMoneyinfo reportMoneyinfo ) throws ParseException {
        String dateNowStr = LocalDateTimeUtils.format( LocalDate.now() );//获取当天时间字符串
        setSelectTime( dateNowStr, reportMoneyinfo );//首次进入查询7天的数据
        Map<String, Object> resultMap = new HashMap<>();
        reportMoneyinfoMapper.calldataProrepPlamcom( dateNowStr, dateNowStr );
        List<ReportMoneyinfo> allList = reportMoneyinfoMapper.selectReportMoneyinfoList( reportMoneyinfo );
        resultMap.put( "rows", allList );
        return resultMap;
    }

    //统计表头数据
    @Override
    public ReportMoneyinfo countMoneyData( ReportMoneyinfo reportMoneyinfo ) throws ParseException {
        String dateNowStr = LocalDateTimeUtils.format( LocalDate.now() );//获取当天时间字符串
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
        ReportMoneyinfo reportMoneyinfo1 = reportMoneyinfoMapper.countMoneyInfoData( reportMoneyinfo );
        if ( !ObjectUtils.isEmpty( reportMoneyinfo1 ) ) {
            BigDecimal paymentAmount = reportMoneyinfo1.getPaymentAmount();//入款总金额
            paymentAmount = paymentAmount == null ? BigDecimal.ZERO : paymentAmount;
            BigDecimal outMoney = reportMoneyinfo1.getOutMoney();//出款总金额
            outMoney = outMoney == null ? BigDecimal.ZERO : outMoney;
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
        List<ReportMoneyinfo> allList = reportMoneyinfoMapper.selectReportMoneyinfoList( reportMoneyinfo );
        return allList;
    }

    private void setSelectTime( String dateNowStr, ReportMoneyinfo reportMoneyinfo ) {
        if ( null == reportMoneyinfo.getParams() || reportMoneyinfo.getParams().size() == 0
                || reportMoneyinfo.getParams().get( "beginTime" ) == "" ) {
            HashMap m = new HashMap<>();
            m.put( "beginTime", LocalDateTimeUtils.format( LocalDate.now().minusDays( 7 ) ) );
            m.put( "endTime", dateNowStr );
            reportMoneyinfo.setParams( m );
        }
    }
}