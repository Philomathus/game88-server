package tv.game88.platform.api.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.platform.api.dto.RspPlamGamesMonth;
import tv.game88.platform.api.entity.ReportPlamGames;
import tv.game88.platform.api.mapper.ReportPlamGamesMapper;
import tv.game88.platform.api.service.ReportPlamGamesService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ReportPlamGamesServiceImpl implements ReportPlamGamesService {
    @Resource
    private ReportPlamGamesMapper reportPlamGamesMapper;
    @Resource
    private RedisUtils            redisUtils;

    @Override
    public List<ReportPlamGames> selectReportPlamGamesList(ReportPlamGames reportPlamGames ) {
        String dateNowStr = LocalDateTimeUtils.format( LocalDate.now() );

        if ( dateNowStr.equals( reportPlamGames.getEndDate() ) ) {
            if ( !redisUtils.exists( "admin-reportPlamGames" ) ) {
                storage( dateNowStr );
            }
        }
//        List<ReportPlamGames> allList   = reportPlamGamesMapper.selectReportPlamGamesList( reportPlamGames );
//        Map<String, Object>   resultMap = new HashMap<>();
//        resultMap.put( "rows", allList );
//        return resultMap;
        return reportPlamGamesMapper.selectReportPlamGamesList( reportPlamGames );
    }

    @Override
    public ReportPlamGames countBetData( ReportPlamGames reportPlamGames ) {
        return reportPlamGamesMapper.countBetData( reportPlamGames );
    }

    public void storage( String dateNowStr ) {
        redisUtils.strSet( "admin-reportPlamGames", "0", Duration.ofMinutes( 5 ) );
        reportPlamGamesMapper.calldataProrepPlamcom( dateNowStr );

    }

    @Override
    public List<ReportPlamGames> exportPlamGamesList( ReportPlamGames reportPlamGames ) {
        return reportPlamGamesMapper.selectReportPlamGamesList( reportPlamGames );
    }

    @Override
    public List<RspPlamGamesMonth> selectReportPlamGamesListMonth( ReportPlamGames reportPlamGames ) throws ParseException {
        ReportPlamGames         reportPlamGames1 = getTime( reportPlamGames );
        List<RspPlamGamesMonth> allList          = reportPlamGamesMapper.selectReportPlamGamesListMonth( reportPlamGames1 );
        for ( RspPlamGamesMonth rsplist : allList ) {
            rsplist.setDate( reportPlamGames1.getBegindate().substring( 0, 7 ) );
        }
        return allList;
    }

    @Override
    public RspPlamGamesMonth countBet( ReportPlamGames reportPlamGames ) throws ParseException {
        ReportPlamGames         reportPlamGames1 = getTime( reportPlamGames );
        List<RspPlamGamesMonth> allList          = reportPlamGamesMapper.selectReportPlamGamesListMonth( reportPlamGames1 );
        BigDecimal              countBetMoney    = BigDecimal.ZERO;
        BigDecimal              countPaiCai      = BigDecimal.ZERO;
        BigDecimal              countGameProfit  = BigDecimal.ZERO;
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
        String begindate;
        if ( reportPlamGames.getBegindate() == null ) {
            String dateNowStr = LocalDateTimeUtils.format( LocalDate.now() );
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
        Date date = new SimpleDateFormat( "yyyy-MM-dd" ).parse( begindate );

        int      month = date.getMonth() + 1;
        Calendar cal   = Calendar.getInstance();
        // 设置月份
        cal.set( Calendar.MONTH, month - 1 );
        // 获取月份的最大天数
        int lastDay;
        //2月份每年的天数不固定
        if ( month == 2 ) {
            lastDay = cal.getLeastMaximum( Calendar.DAY_OF_MONTH );
        } else {
            lastDay = cal.getActualMaximum( Calendar.DAY_OF_MONTH );
        }
        return begindate.substring( 0, 8 ) + lastDay;
    }
}