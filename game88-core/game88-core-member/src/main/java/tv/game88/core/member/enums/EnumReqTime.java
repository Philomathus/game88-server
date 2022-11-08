package tv.game88.core.member.enums;

import tv.game88.common.utils.LocalDateTimeUtils;

import java.time.LocalDate;

public enum EnumReqTime {

    today(1,"今天"),
    yesterday(2,"昨天"),
    month(3,"一个月内"),
    ;

    private int type ;
    private String des;


    EnumReqTime( int type, String des ) {
        this.type = type;
        this.des = des;
    }

    public int getType() {
        return type;
    }

    public String getDes() {
        return des;
    }


    public  String getBeginDayTime(){
        LocalDate localDate = LocalDate.now();
        localDate =  switch ( this ) {
            case today -> localDate;
            case yesterday -> localDate.plusDays( -1 );
            case month -> localDate.plusMonths( -1 );
        };
        return LocalDateTimeUtils.format( localDate.atStartOfDay() );
    }

    public  String getEndDayTime(){
        LocalDate localDate = LocalDate.now();
        localDate = switch ( this ) {
            case today, month -> localDate;
            case yesterday -> localDate
                    .plusDays( -1 );
        };
        return LocalDateTimeUtils.format( localDate.atTime( 23, 59, 59 ) );
    }


}
