package tv.game88.lottery.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

/**
 * 获取输入公历日期的生肖
 * DATE 2020.08.13
 */
public class LunarAnimalUtils {

    final static long[] lunarInfo = new long[] { 0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0,
            0x09ad0, 0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970
            , 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566, 0x0d4a0, 0x0ea50,
            0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0
            , 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8,
            0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0
            , 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6, 0x095b0, 0x049b0, 0x0a974,
            0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58
            , 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0,
            0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954
            , 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0,
            0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0
            , 0x0aa50, 0x1b255, 0x06d20, 0x0ada0 };

    //生肖
    final static String[] animals = new String[] { "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪" };
    final static String[] animal_6hecai = new String[] { "虎", "牛", "鼠", "猪", "狗", "鸡", "猴", "羊", "马", "蛇", "龙", "兔" };

    /**
     * 返回农历y年的总天数
     *
     * @param y
     */
    private static int lunarYearDays( int y ) {
        int i, sum = 348;
        for ( i = 0x8000; i > 0x8; i >>= 1 ) {
            sum += ( ( lunarInfo[ y - 1900 ] & i ) != 0 ? 1 : 0 );
        }
        return ( sum + leapDays( y ) );
    }

    /**
     * 返回农历y年闰月的天数
     */
    private static int leapDays( int y ) {
        if ( leapMonth( y ) != 0 ) {
            return ( ( lunarInfo[ y - 1900 ] & 0x10000 ) != 0 ? 30 : 29 );
        } else {
            return 0;
        }
    }

    /**
     * 判断y年的农历中那个月是闰月,不是闰月返回0
     *
     * @param y
     */
    private static int leapMonth( int y ) {
        return ( int ) ( lunarInfo[ y - 1900 ] & 0xf );
    }


    /**
     * 输入公历日期返回生肖
     * 输入日期的格式为(YYYY-MM-DD)
     *
     * @param currentDate
     */
    public static String getAnimal( String currentDate ) {
        SimpleDateFormat solarDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        //基准日期
        Date baseDate = null;
        //当前日期
        Date nowaday = null;
        try {
            baseDate = solarDateFormat.parse( "1900-01-31" );
            nowaday = solarDateFormat.parse( currentDate );
        } catch ( ParseException e ) {
            throw new RuntimeException( e );
        }
        // 获取当前日期与1900年1月31日相差的天数
        int offset = ( int ) ( ( nowaday.getTime() - baseDate.getTime() ) / 86400000L );

        //用offset减去每农历年的天数，计算当天是农历第几天 iYear最终结果是农历的年份
        int iYear, daysOfYear = 0;
        for ( iYear = 1900; iYear < 10000 && offset > 0; iYear++ ) {
            daysOfYear = lunarYearDays( iYear );
            offset -= daysOfYear;
        }
        if ( offset < 0 ) {
            iYear--;
        }
        return animals[ ( iYear - 4 ) % 12 ];
    }

    /**
     * 根据输入生肖,获取剩下的顺序生肖
     *
     * @param animal
     */
    public static String[] getLeftOverAnimals( String animal ) {
        String[] result   = new String[ 12 ];
        int      position = 0;
        for ( int i = 0; i < animal_6hecai.length; i++ ) {
            if ( animal_6hecai[ i ].equals( animal ) ) {
                position = i;
            }
        }
        for ( int i = position; i < animal_6hecai.length; i++ ) {
            result[ i - position ] = animal_6hecai[ i ];
        }
        for ( int i = 0; i < position; i++ ) {
            result[ animal_6hecai.length - position + i ] = animal_6hecai[ i ];
        }
        return result;
    }

    /**
     * 测试方法
     *
     * @param args
     */
    public static void main( String[] args ) throws Exception {
        String localDate = LocalDate.now().format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );

        String animal = LunarAnimalUtils.getAnimal( localDate );
        System.out.println( animal + "年" );
        System.out.println( Arrays.toString( animals ) );
        System.out.println( Arrays.toString( LunarAnimalUtils.getLeftOverAnimals( animal ) ) );
    }
}