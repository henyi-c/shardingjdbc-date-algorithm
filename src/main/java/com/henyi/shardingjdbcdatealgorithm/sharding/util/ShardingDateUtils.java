package com.henyi.shardingjdbcdatealgorithm.sharding.util;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>
 * sharding时间工具类
 *
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Slf4j
public class ShardingDateUtils {

    /**
     * 根据日期策略选择的范围获取SimpleDateFormat
     *
     * @param range
     * @return
     */
    public static SimpleDateFormat getDateFormat(String range) {
        switch (range) {
            case "year":
                return new SimpleDateFormat(DateFormatter.YEAR_FORMATTER.getValue());
            case "day":
                return new SimpleDateFormat(DateFormatter.DATE_FORMATTER_SHORT.getValue());
            case "month":
            default:
                return new SimpleDateFormat(DateFormatter.YEAR_MONTH_FORMATTER_SHORT.getValue());
        }
    }


    /**
     * 根据策略范围进行分析时间
     *
     * @param range
     * @param id
     * @return
     */
    public static Date analysisIdByRange(String range, String id) throws ParseException {
        SimpleDateFormat dateFormat = ShardingDateUtils.getDateFormat(range);
        switch (range) {
            case "year":
                return dateFormat.parse(id.substring(0, 4));
            case "day":
                return dateFormat.parse(id.substring(0, 8));
            case "month":
            default:
                return dateFormat.parse(id.substring(0, 6));
        }
    }


    /**
     * 根据日期策略选择的范围获取Calendar
     *
     * @param range
     * @return
     */
    public static Calendar getCalendar(String minDateStr, String range, int offset) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        switch (range) {
            case "year": {
                Date minDate = new SimpleDateFormat(DateFormatter.YEAR_FORMATTER.getValue()).parse(minDateStr);
                calendar.setTime(minDate);
                calendar.add(Calendar.YEAR, offset);
            }
            break;
            case "day": {
                Date minDate = new SimpleDateFormat(DateFormatter.DATE_FORMATTER_SHORT.getValue()).parse(minDateStr);
                calendar.setTime(minDate);
                calendar.add(Calendar.DATE, offset);
            }
            break;
            case "month":
            default: {
                Date minDate = new SimpleDateFormat(DateFormatter.YEAR_MONTH_FORMATTER_SHORT.getValue()).parse(minDateStr);
                calendar.setTime(minDate);
                calendar.add(Calendar.MONTH, offset);
            }
        }
        return calendar;
    }


    /**
     * 最小时间表到最大时间偏移量为多少
     *
     * @param range
     * @return
     */
    public static int getBetweenOffset(Date minDate, Date maxDate, String range) {

        switch (range) {
            case "year":
                return new Long(DateUtil.betweenYear(minDate, maxDate, false)).intValue();

            case "day":
                return new Long(DateUtil.betweenDay(minDate, maxDate, false)).intValue();

            case "month":
            default:
                return new Long(DateUtil.betweenMonth(minDate, maxDate, false)).intValue();

        }
    }


    public static int getCalendarDateRange(String range) {
        switch (range) {
            case "year":
                return Calendar.YEAR;

            case "day":
                return Calendar.DATE;

            case "month":
            default:
                return Calendar.MONTH;

        }
    }


    /**
     * 获取数据库该逻辑表日期
     *
     * @return
     */
    public static Date getTableDate(String date, String range, SimpleDateFormat dateFormat, boolean isMax) {
        try {
            Date resDate = dateFormat.parse(date);
            if (isMax) {
                resDate = getMaxTime(resDate, range);
            }
            return resDate;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 最大值转换默认为当天的最小值，那么需要根据策略配置成为真正的最大值
     *
     * @param range
     * @return
     */
    public static Date getMaxTime(Date resDate, String range) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(resDate);
        switch (range) {
            case "year":
                calendar.add(Calendar.YEAR, 1);
                return new Date(calendar.getTime().getTime() - 1);
            case "day":
                calendar.add(Calendar.DATE, 1);
                return new Date(calendar.getTime().getTime() - 1);
            case "month":
            default:
                calendar.add(Calendar.MONTH, 1);
                return new Date(calendar.getTime().getTime() - 1);
        }
    }

}
