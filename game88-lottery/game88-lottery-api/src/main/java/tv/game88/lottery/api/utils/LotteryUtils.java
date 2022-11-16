package tv.game88.lottery.api.utils;

import tv.game88.common.utils.LocalDateTimeUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class LotteryUtils {
    public static int getKindId( int lotteryId ) {
        int kindId;
        if ( lotteryId == 2001 ) {
            kindId = 11;
        } else {
            kindId = lotteryId % 5;
        }
        return kindId;
    }

    public static String getLotteryIssue( Integer cycle, LocalDateTime time ) {
        if ( cycle <= 0 ) {
            cycle = 1;
        }
        LocalDateTime localDateTime = time.plus( 100, ChronoUnit.MILLIS );
        int           minute        = localDateTime.getMinute() + localDateTime.getHour() * 60;
        return LocalDateTimeUtils
                .format( localDateTime, LocalDateTimeUtils.YYYYMMDD_FORMATTER )
                .concat( "-" )
                .concat( String.format( "%04d", minute / cycle + 1 ) );
    }

    public static void main( String[] args ) {
        for ( int i = 0; i < 10; i++ ) {
            System.out.println( getLotteryIssue( 1, LocalDateTime.now().plusMinutes( i ) ) );
        }
    }
}
