package tv.game88.common.utils;

import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class LocalDateTimeUtils {
	public static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter YYYYMMDDHHMMSS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	public static final DateTimeFormatter HH_MM_SS_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static final DateTimeFormatter HHMMSS_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

	/**
	 * LocalDateTime转为Date
	 *
	 * @param localDateTime
	 * @return Date
	 */
	public static Date localDateTimeToDate(LocalDateTime localDateTime) {
		Assert.notNull(localDateTime, "日期不能为空!");
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * LocalDate转为Date
	 *
	 * @param localDate
	 * @return
	 */
	public static Date localDateToDate(LocalDate localDate) {
		Assert.notNull(localDate, "日期不能为空!");
		ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(zonedDateTime.toInstant());
	}

	/**
	 * 格式化LocalDate
	 *
	 * @param date
	 * @return
	 */
	public static String format(LocalDate date, DateTimeFormatter formatter) {
		return date.format(formatter);
	}

	/**
	 * 默认格式化LocalDate
	 *
	 * @param date
	 * @return
	 */
	public static String format(LocalDate date) {
		return date.format(LocalDateTimeUtils.YYYY_MM_DD_FORMATTER);
	}

	/**
	 * 格式化LocalDateTime
	 *
	 * @param time
	 * @return
	 */
	public static String format(LocalDateTime time, DateTimeFormatter formatter) {
		return time.format(formatter);
	}

	/**
	 * 默认格式化LocalDateTime
	 *
	 * @param time
	 * @return
	 */
	public static String format(LocalDateTime time) {
		return time.format(LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS_FORMATTER);
	}

	/**
	 * 将字符串转化为LocalDate
	 *
	 * @param dateStr
	 * @return
	 */
	public static LocalDate parseLocalDate(String dateStr, DateTimeFormatter formatter) {
		return LocalDate.parse(dateStr, formatter);
	}

	/**
	 * 将字符串转化为LocalDate 默认
	 *
	 * @param dateStr
	 * @return
	 */
	public static LocalDate parseLocalDate(String dateStr) {
		return LocalDate.parse(dateStr, YYYY_MM_DD_FORMATTER);
	}

	/**
	 * 将字符串转化为LocalDateTime
	 *
	 * @param dateStr
	 * @return
	 */
	public static LocalDateTime parseLocalDateTime(String dateStr, DateTimeFormatter formatter) {
		return LocalDateTime.parse(dateStr, formatter);
	}

	/**
	 * 将字符串转化为LocalDateTime
	 *
	 * @param dateStr
	 * @return
	 */
	public static LocalDateTime parseLocalDateTime(String dateStr) {
		return LocalDateTime.parse(dateStr, YYYY_MM_DD_HH_MM_SS_FORMATTER);
	}

	/**
	 * 将时间戳转成LocalDateTime
	 *
	 * @param timestamp
	 * @return
	 */
	public static LocalDateTime getDateTimeFromTimestamp(long timestamp) {
		if (timestamp == 0) {
			return null;
		}
		return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), TimeZone.getDefault().toZoneId());
	}

	/**
	 * 将时间戳转化成日期
	 *
	 * @param timestamp
	 * @return
	 */
	public static LocalDate getDateFromTimestamp(long timestamp) {
		LocalDateTime date = getDateTimeFromTimestamp(timestamp);
		return date == null ? null : date.toLocalDate();
	}

	/**
	 * 将LocalDateTime转成时间戳
	 *
	 * @param date
	 * @return
	 */
	public static long localDateToTimestamp(LocalDateTime date) {
		return Timestamp.valueOf(date).getTime();
	}

	/**
	 * 获得今天的 00:00:00
	 *
	 * @return
	 */
	public static LocalDateTime getStartOfToday() {
		LocalDate localDate = LocalDate.now();
		return localDate.atStartOfDay();
	}

	/**
	 * 获得今天的 23:59:59
	 *
	 * @return
	 */
	public static LocalDateTime getEndOfToday() {
		LocalDate localDate = LocalDate.now();
		return localDate.atTime(23, 59, 59);
	}
}
