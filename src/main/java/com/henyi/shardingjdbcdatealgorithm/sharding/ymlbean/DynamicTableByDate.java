package com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Data
public class DynamicTableByDate {
    /**
     * 表名称
     */
    private String name;
    /**
     * 策略时间范围
     */
    private String range;
    /**
     * range的偏移量
     */
    private Integer offset;
    /**
     * 数据库中表最小日期
     */
    private String minDate;

    /**
     * 数据库中表最大日期
     */
    private String maxDate;

    private List<DbSql> dbList;

}
