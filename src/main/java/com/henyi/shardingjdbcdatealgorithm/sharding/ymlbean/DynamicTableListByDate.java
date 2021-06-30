package com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * <p>
 * 需要根据年月设置动态加载的表名
 *
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@ConfigurationProperties(prefix = "dynamic.table.date-algorithm")
@Data
public class DynamicTableListByDate {
    private List<DynamicTableByDate> tableList;

}
