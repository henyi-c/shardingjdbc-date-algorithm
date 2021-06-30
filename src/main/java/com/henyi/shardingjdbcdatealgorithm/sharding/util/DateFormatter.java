package com.henyi.shardingjdbcdatealgorithm.sharding.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DateFormatter {

    /**
     * <p>
     * 日期类型格式化
     *
     * </p>
     *
     * @author henyi-c
     * @since 2021-07-01
     */
    YEAR_FORMATTER("yyyy"),
    YEAR_MONTH_FORMATTER("yyyy-MM"),
    DATE_FORMATTER("yyyy-MM-dd"),
    DATETIME_FORMATTER("yyyy-MM-dd HH:mm:ss"),
    TIME_FORMATTER("HH:mm:ss"),
    YEAR_MONTH_FORMATTER_SHORT("yyyyMM"),
    DATE_FORMATTER_SHORT("yyyyMMdd");

    private final String value;
}
