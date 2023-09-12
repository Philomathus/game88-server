package tv.game88.wallet.api.type;

import lombok.Getter;
import tv.game88.common.utils.LocalDateTimeUtils;

import java.time.LocalDate;

@Getter
public enum EnumReqTime {

    today( 1, "今天" ),
    three_today( 2, "近三天" ),
    five_today( 3, "近五天" ),
    fifteen_today( 4, "近十五天" ),
    month( 5, "一个月内" ),
    ;

    private final int    type;
    private final String des;

    EnumReqTime( int type, String des ) {
        this.type = type;
        this.des  = des;
    }

    public String getBeginDayTime() {
        LocalDate now = LocalDate.now();
        LocalDate localDate = switch ( this ) {
            case today -> now;
            case three_today -> now.minusDays( 3 );
            case five_today -> now.minusDays( 5 );
            case fifteen_today -> now.minusDays( 15 );
            case month -> now.minusMonths( 1 );
        };
        return LocalDateTimeUtils.format( localDate.atStartOfDay() );
    }

    public String getEndDayTime() {
        LocalDate now = LocalDate.now();
        LocalDate localDate = switch ( this ) {
            case today, three_today, five_today, fifteen_today, month -> now;
        };
        return LocalDateTimeUtils.format( localDate.atTime( 23, 59, 59 ) );
    }

    public static void main( String[] args ) {
        EnumReqTime enumReqTime = EnumReqTime.three_today;
        System.out.println( enumReqTime.getBeginDayTime() );
        System.out.println( enumReqTime.getEndDayTime() );
    }
}
