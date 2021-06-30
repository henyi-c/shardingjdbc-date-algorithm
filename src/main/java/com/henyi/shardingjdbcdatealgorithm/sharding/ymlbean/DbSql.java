package com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean;

import lombok.Data;

/**
 * <p>
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Data
public class DbSql {
    private String dbType;

    /**
     * 创建该表sql语句
     */
    private String createTableSql;
}
