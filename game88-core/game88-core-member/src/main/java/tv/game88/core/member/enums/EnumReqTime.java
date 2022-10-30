package tv.game88.core.member.enums;

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


    public  String getBeginDay(){
        LocalDate l = LocalDate.now();
        return switch ( this ) {
            case today -> l.toString();
            case yesterday -> l.plusDays( -1 ).toString();
            case month -> l.plusMonths( -1 ).toString();
        };
    }

    public  String getEndDay(){
        LocalDate l = LocalDate.now();
        return switch ( this ) {
            case today, month -> l.toString();
            case yesterday -> l
                    .plusDays( -1 )
                    .toString();
        };
    }


}
