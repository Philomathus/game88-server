package tv.game88.general.api.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.general.api.dto.RspPlamGamesMonth;
import tv.game88.general.api.entity.ReportPlamGames;
import tv.game88.general.api.mapper.ReportPlamGamesMapper;
import tv.game88.general.api.service.IReportPlamGamesService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
@Log4j2
public class ReportPlamGamesServiceImpl extends ServiceImpl<ReportPlamGamesMapper, ReportPlamGames> implements IReportPlamGamesService {
    @Resource
    private RedisUtils redisUtil;

    @Override
    public void storage( ReportPlamGames reportPlamGames ) {
        String dateNowStr = LocalDateTimeUtils.format( LocalDate.now() );
        if ( dateNowStr.equals( reportPlamGames.getBegindate() ) ) {
            if ( !redisUtil.exists( "admin-reportPlamGames" ) ) {

                redisUtil.strSet( "admin-reportPlamGames", "0", Duration.ofMinutes( 5 ) );

                DynamicDataSourceContextHolder.push( "slave_" + reportPlamGames.getAgentPlatform() );

                this.baseMapper.calldataProrepPlamcom( dateNowStr, reportPlamGames.getAgentPlatform() );

                DynamicDataSourceContextHolder.poll();
            }
        }
    }

    @Override
    public List<ReportPlamGames> selectReportPlamGamesList( ReportPlamGames reportPlamGames ) {
        DynamicDataSourceContextHolder.push( "slave_" + reportPlamGames.getAgentPlatform() );
        List<ReportPlamGames> allList = this.baseMapper.selectReportPlamGamesList( reportPlamGames );
        DynamicDataSourceContextHolder.poll();
        return allList;
    }

    @Override
    public ReportPlamGames countBetData( ReportPlamGames reportPlamGames ) {
        DynamicDataSourceContextHolder.push( "slave_" + reportPlamGames.getAgentPlatform() );

        ReportPlamGames reportPlamGames1 = this.baseMapper.countBetData( reportPlamGames );

        DynamicDataSourceContextHolder.poll();
        return reportPlamGames1;
    }


//    public void storage( String dateNowStr, String agentPlatform ) {
//        redisUtil.strSet( "admin-reportPlamGames", "0", Duration.ofMinutes( 5 ) );
//
//        DynamicDataSourceContextHolder.push( "slave_" + agentPlatform );
//
//        this.baseMapper.calldataProrepPlamcom( dateNowStr, agentPlatform );
//
//        DynamicDataSourceContextHolder.poll();
//    }

    @Override
    public List<ReportPlamGames> exportPlamGamesList( ReportPlamGames reportPlamGames ) {
        DynamicDataSourceContextHolder.push( "slave_" + reportPlamGames.getAgentPlatform() );

        List<ReportPlamGames> allList = this.baseMapper.selectReportPlamGamesList( reportPlamGames );

        DynamicDataSourceContextHolder.poll();
        return allList;
    }

    @Override
    public List<RspPlamGamesMonth> selectReportPlamGamesListMonth( ReportPlamGames reportPlamGames ) throws ParseException {
        ReportPlamGames reportPlamGames1 = getTime( reportPlamGames );

        DynamicDataSourceContextHolder.push( "slave_" + reportPlamGames.getAgentPlatform() );

        List<RspPlamGamesMonth> allList = this.baseMapper.selectReportPlamGamesListMonth( reportPlamGames1 );

        DynamicDataSourceContextHolder.poll();

        for ( RspPlamGamesMonth rsplist : allList ) {
            rsplist.setDate( reportPlamGames1.getBegindate().substring( 0, 7 ) );
        }
        return allList;
    }

    @Override
    public RspPlamGamesMonth countBet( ReportPlamGames reportPlamGames ) throws ParseException {
        ReportPlamGames reportPlamGames1 = getTime( reportPlamGames );

        DynamicDataSourceContextHolder.push( "slave_" + reportPlamGames.getAgentPlatform() );

        List<RspPlamGamesMonth> allList = this.baseMapper.selectReportPlamGamesListMonth( reportPlamGames1 );

        DynamicDataSourceContextHolder.poll();

        BigDecimal countBetMoney   = BigDecimal.ZERO;
        BigDecimal countPaiCai     = BigDecimal.ZERO;
        BigDecimal countGameProfit = BigDecimal.ZERO;
        for ( RspPlamGamesMonth rsplist : allList ) {
            if ( rsplist.getGamecell() != null ) {
                countBetMoney = countBetMoney.add( rsplist.getGamecell() );
            }
            if ( rsplist.getPaicai() != null ) {
                countPaiCai = countPaiCai.add( rsplist.getPaicai() );
            }
            if ( rsplist.getGameprofit() != null && rsplist.getGameprofit().compareTo( new BigDecimal( 0 ) ) > 0 ) {
                countGameProfit = countGameProfit.add( rsplist.getGameprofit() );
            }
        }
        RspPlamGamesMonth rspPlamGamesMonth = new RspPlamGamesMonth();
        rspPlamGamesMonth.setCountBetMoney( countBetMoney );
        rspPlamGamesMonth.setCountPaiCai( countPaiCai );
        rspPlamGamesMonth.setCountGameProfit( countGameProfit );
        return rspPlamGamesMonth;
    }

    private ReportPlamGames getTime( ReportPlamGames reportPlamGames ) throws ParseException {
        String begindate = null;
        if ( reportPlamGames.getBegindate() == null ) {
            Date             d          = new Date();
            SimpleDateFormat sdf        = new SimpleDateFormat( "yyyy-MM" );
            String           dateNowStr = sdf.format( d );
            begindate = dateNowStr + "-01";
            reportPlamGames.setDateTime( dateNowStr );
            reportPlamGames.setBegindate( begindate );
        } else {
            begindate = reportPlamGames.getBegindate();
            String dateTime = begindate.substring( 0, 7 );
            reportPlamGames.setDateTime( dateTime );
        }
        String endDate = getEndDate( begindate );
        reportPlamGames.setEndDate( endDate );
        return reportPlamGames;
    }

    private String getEndDate( String begindate ) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );//注意月份是MM
        Date             date             = simpleDateFormat.parse( begindate );

        int      month = date.getMonth() + 1;
        Calendar cal   = Calendar.getInstance();
        // 设置月份
        cal.set( Calendar.MONTH, month - 1 );
        // 获取月份的最大天数
        int lastDay = 0;
        //2月份每年的天数不固定
        if ( month == 2 ) {
            lastDay = cal.getLeastMaximum( Calendar.DAY_OF_MONTH );
        } else {
            lastDay = cal.getActualMaximum( Calendar.DAY_OF_MONTH );
        }
        String endDate = begindate.substring( 0, 8 ) + lastDay;
        return endDate;
    }
}