package tv.game88.common.utils;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class LocalDateTimeUtils {
    public static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );

    public static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern( "yyyyMMdd" );

    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );

    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_SSS_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss.SSS" );

    public static final DateTimeFormatter YYYY_MM_DDTHH_MM_SS_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss" );

    public static final DateTimeFormatter YYYY_MM_DDTHH_MM_SS_SSS_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss.SSS" );


    public static final DateTimeFormatter YYYYMMDDHHMMSS_FORMATTER = DateTimeFormatter.ofPattern( "yyyyMMddHHmmss" );

    public static final DateTimeFormatter YYYYMMDDHHMMSSSSS_FORMATTER = DateTimeFormatter.ofPattern( "yyyyMMddHHmmssSSS" );

    public static final DateTimeFormatter DDMMYYYYHHMMSS_FORMATTER = DateTimeFormatter.ofPattern( "dd-MM-yyyy HH:mm:ss" );

    public static final DateTimeFormatter DDMMYYYYHHMM00_FORMATTER = DateTimeFormatter.ofPattern( "dd-MM-yyyy HH:mm:00" );

    public static final DateTimeFormatter MMDDYYYYHHMMSSSSS_FORMATTER = DateTimeFormatter.ofPattern( "MM/dd/yyyy HH:mm:ss.SSS" );

    public static final DateTimeFormatter YYYY_MM_DD_T_HH_MM_SSS_XXXFORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ssXXX" );

    public static final DateTimeFormatter RFC3339_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" );

    public static final DateTimeFormatter HH_MM_SS_FORMATTER = DateTimeFormatter.ofPattern( "HH:mm:ss" );

    public static final DateTimeFormatter HHMMSS_FORMATTER = DateTimeFormatter.ofPattern( "HHmmss" );

    /**
     * 格式化LocalDate
     *
     * @param date
     */
    public static String format( LocalDate date, DateTimeFormatter formatter ) {
        return date.format( formatter );
    }

    /**
     * 默认格式化LocalDate
     *
     * @param date
     */
    public static String format( LocalDate date ) {
        return date.format( LocalDateTimeUtils.YYYY_MM_DD_FORMATTER );
    }

    /**
     * 格式化LocalDateTime
     *
     * @param time
     */
    public static String format( LocalDateTime time, DateTimeFormatter formatter ) {
        return time.format( formatter );
    }

    /**
     * 默认格式化LocalDateTime
     *
     * @param time
     */
    public static String format( LocalDateTime time ) {
        return time.format( LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS_FORMATTER );
    }

    /**
     * 将字符串转化为LocalDate
     *
     * @param dateStr
     */
    public static LocalDate parseLocalDate( String dateStr, DateTimeFormatter formatter ) {
        return LocalDate.parse( dateStr, formatter );
    }

    /**
     * 将字符串转化为LocalDate 默认
     *
     * @param dateStr
     */
    public static LocalDate parseLocalDate( String dateStr ) {
        return LocalDate.parse( dateStr, YYYY_MM_DD_FORMATTER );
    }

    /**
     * 将字符串转化为LocalDateTime
     *
     * @param dateStr
     */
    public static LocalDateTime parseLocalDateTime( String dateStr, DateTimeFormatter formatter ) {
        return LocalDateTime.parse( dateStr, formatter );
    }

    /**
     * 将字符串转化为LocalDateTime
     *
     * @param dateStr
     */
    public static LocalDateTime parseLocalDateTime( String dateStr ) {
        return LocalDateTime.parse( dateStr, YYYY_MM_DD_HH_MM_SS_FORMATTER );
    }

    /**
     * 将时间戳转成LocalDateTime
     *
     * @param timestamp
     */
    public static LocalDateTime getDateTimeFromTimestamp( long timestamp ) {
        if ( timestamp == 0 ) {
            return null;
        }
        return LocalDateTime.ofInstant( Instant.ofEpochMilli( timestamp ), TimeZone.getDefault().toZoneId() );
    }

    /**
     * 将时间戳转化成日期
     *
     * @param timestamp
     */
    public static LocalDate getDateFromTimestamp( long timestamp ) {
        LocalDateTime date = getDateTimeFromTimestamp( timestamp );
        return date == null ? null : date.toLocalDate();
    }

    /**
     * 将LocalDateTime转成时间戳
     *
     * @param date
     */
    public static long localDateToTimestamp( LocalDateTime date ) {
        return Timestamp.valueOf( date ).getTime();
    }

    /**
     * 获得今天的 00:00:00
     */
    public static LocalDateTime getStartOfToday() {
        LocalDate localDate = LocalDate.now();
        return localDate.atStartOfDay();
    }

    /**
     * 获得今天的 23:59:59
     */
    public static LocalDateTime getEndOfToday() {
        LocalDate localDate = LocalDate.now();
        return localDate.atTime( 23, 59, 59 );
    }

    /**
     * 判断是否同一周
     *
     * @param one
     * @param two
     */
    public static boolean isSameWeek( LocalDateTime one, LocalDateTime two ) {
        LocalDate oneLocalDate = one.with( DayOfWeek.MONDAY ).toLocalDate();
        LocalDate twoLocalDate = two.with( DayOfWeek.MONDAY ).toLocalDate();
        return oneLocalDate.isEqual( twoLocalDate );
    }

    /**
     * 判断是否同一个月
     *
     * @param one
     * @param two
     */
    public static boolean isSameMonth( LocalDateTime one, LocalDateTime two ) {
        LocalDate oneLocalDate = LocalDate.of( one.getYear(), one.getMonth(), 1 );
        LocalDate twoLocalDate = LocalDate.of( two.getYear(), two.getMonth(), 1 );
        return oneLocalDate.isEqual( twoLocalDate );
    }

    /**
     * 判断是否同一天
     *
     * @param one
     * @param two
     */
    public static boolean isSameDay( LocalDateTime one, LocalDateTime two ) {
        return one.toLocalDate().isEqual( two.toLocalDate() );
    }

    /**
     * 获取两个时间间隔(毫秒)
     *
     * @return long 毫秒
     */
    public static long getIntervalTime( LocalDateTime end, LocalDateTime now ) {
        long time2 = Timestamp.valueOf( now ).getTime();
        long time1 = Timestamp.valueOf( end ).getTime();
        return time2 - time1;
    }

    /**
     * 当地时区时间转换成美东时间
     *
     * @param localDateTime
     *
     * @return LocalDateTime
     */
    public static LocalDateTime convertToMeiDong( LocalDateTime localDateTime ) {
        ZonedDateTime zonedTime = localDateTime.atZone( ZoneId.systemDefault() );
        ZonedDateTime converted = zonedTime.withZoneSameInstant( ZoneId.of( "America/Caracas" ) );
        return converted.toLocalDateTime();
    }

    /**
     * 时间戳转换成美东时间
     *
     * @return LocalDateTime
     */
    public static LocalDateTime convertTimestampToMeiDong( long timestamp ) {
        return LocalDateTime.ofInstant( Instant.ofEpochMilli( timestamp ), ZoneId.of( "America/Caracas" ) );
    }

    /**
     * 美东时间转换成当地时区时间
     *
     * @param time
     *
     * @return LocalDateTime
     */
    public static LocalDateTime convertMeiDongToDefault( String time ) {
        LocalDateTime localDateTime = LocalDateTime.parse( time, YYYY_MM_DD_HH_MM_SS_FORMATTER );
        ZonedDateTime zonedTime     = localDateTime.atZone( ZoneId.of( "America/Caracas" ) );
        ZonedDateTime converted     = zonedTime.withZoneSameInstant( ZoneId.systemDefault() );
        return converted.toLocalDateTime();
    }

    public static LocalDateTime convertMeiDongToDefault( String time, DateTimeFormatter formatter ) {
        LocalDateTime localDateTime = LocalDateTime.parse( time, formatter );
        ZonedDateTime zonedTime     = localDateTime.atZone( ZoneId.of( "America/Caracas" ) );
        ZonedDateTime converted     = zonedTime.withZoneSameInstant( ZoneId.systemDefault() );
        return converted.toLocalDateTime();
    }

    /**
     * 当地时区时间转换成UTC+0时间
     *
     * @param localDateTime
     *
     * @return LocalDateTime
     */
    public static LocalDateTime convertToUTC0( LocalDateTime localDateTime ) {
        ZonedDateTime zonedTime = localDateTime.atZone( ZoneId.systemDefault() );
        ZonedDateTime converted = zonedTime.withZoneSameInstant( ZoneId.of( "UTC+0" ) );
        return converted.toLocalDateTime();
    }

    public static LocalDateTime convertToUTC7( LocalDateTime localDateTime ) {
        ZonedDateTime zonedTime = localDateTime.atZone( ZoneId.systemDefault() );
        ZonedDateTime converted = zonedTime.withZoneSameInstant( ZoneId.of( "UTC+7" ) );
        return converted.toLocalDateTime();
    }

    public static LocalDateTime convertToUTC8( LocalDateTime localDateTime ) {
        ZonedDateTime zonedTime = localDateTime.atZone( ZoneId.systemDefault() );
        ZonedDateTime converted = zonedTime.withZoneSameInstant( ZoneId.of( "UTC+8" ) );
        return converted.toLocalDateTime();
    }

    /**
     * UTC+0时间转换成当地时区时间
     *
     * @param time
     *
     * @return LocalDateTime
     */
    public static LocalDateTime convertUTC0ToDefault( String time, DateTimeFormatter dateTimeFormatter ) {
        LocalDateTime localDateTime = LocalDateTime.parse( time, dateTimeFormatter );
        ZonedDateTime zonedTime     = localDateTime.atZone( ZoneId.of( "UTC+0" ) );
        ZonedDateTime converted     = zonedTime.withZoneSameInstant( ZoneId.systemDefault() );
        return converted.toLocalDateTime();
    }

    public static LocalDateTime convertUTC7ToDefault( String time, DateTimeFormatter dateTimeFormatter ) {
        LocalDateTime localDateTime = LocalDateTime.parse( time, dateTimeFormatter );
        ZonedDateTime zonedTime     = localDateTime.atZone( ZoneId.of( "UTC+7" ) );
        ZonedDateTime converted     = zonedTime.withZoneSameInstant( ZoneId.systemDefault() );
        return converted.toLocalDateTime();
    }

    /**
     * 当地时区时间转换成UTC-4时间
     *
     * @param localDateTime
     *
     * @return LocalDateTime
     */
    public static LocalDateTime convertToUTC_4( LocalDateTime localDateTime ) {
        ZonedDateTime zonedTime = localDateTime.atZone( ZoneId.systemDefault() );
        ZonedDateTime converted = zonedTime.withZoneSameInstant( ZoneId.of( "UTC-4" ) );
        return converted.toLocalDateTime();
    }

    /**
     * UTC-4时间转换成当地时区时间
     *
     * @param time
     *
     * @return LocalDateTime
     */
    public static LocalDateTime convertUTC_4ToDefault( String time, DateTimeFormatter dateTimeFormatter ) {
        LocalDateTime localDateTime = LocalDateTime.parse( time, dateTimeFormatter );
        ZonedDateTime zonedTime     = localDateTime.atZone( ZoneId.of( "UTC-4" ) );
        ZonedDateTime converted     = zonedTime.withZoneSameInstant( ZoneId.systemDefault() );
        return converted.toLocalDateTime();
    }

    public static String secondsToTime( long seconds ) {
        long h = seconds / 3600;            //小时
        long m = ( seconds % 3600 ) / 60;        //分钟
        long s = ( seconds % 3600 ) % 60;        //秒
        if ( h > 0 ) {
            return h + "小时" + m + "分钟" + s + "秒";
        }
        if ( m > 0 ) {
            return m + "分钟" + s + "秒";
        }
        return s + "秒";
    }
}