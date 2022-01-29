package com.henyi.shardingjdbcdatealgorithm.sharding.algorithm;

import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingDateUtils;
import com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean.DynamicTableByDate;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * 自定义分片算法类，用于当SQL语句中包含了分片键
 * sharding-jdbc会调用该类的doSharding方法，得到要查询的实际数据表名
 * 自定义standard的精确分片(in / eq)
 *
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Slf4j
public class ShardingAlgorithmOfPreciseForTb {


    public static String doSharding(DynamicTableByDate dynamicTableByDate, String id) throws ParseException {

        //拆分id
        //根据时间范围拿取日期
        Date shardingValue = ShardingDateUtils.analysisIdByRange(dynamicTableByDate.getRange(), id);
        return doSharding(dynamicTableByDate, shardingValue);
    }


    public static String doSharding(DynamicTableByDate dynamicTableByDate, Date shardingValue) {

        StringBuilder tableName = new StringBuilder();

        SimpleDateFormat dateFormat = ShardingDateUtils.getDateFormat(dynamicTableByDate.getRange());

        String logicTableName = dynamicTableByDate.getName();
        //拿取最大最小时间
        String minDate = dynamicTableByDate.getMinDate();
        String maxDate = dynamicTableByDate.getMaxDate();
        try {
            //小于最小时间或大于最大时间
            if (dateFormat.parse(minDate).getTime() > shardingValue.getTime() || shardingValue.getTime() > dateFormat.parse(maxDate).getTime()) {
                return logicTableName;
            }
        } catch (ParseException e) {
            log.error(e.getMessage());
        }

        log.info("执行操作的表名为  {}", logicTableName + "_" + dateFormat.format(shardingValue));

        tableName.append(logicTableName).append("_").append(dateFormat.format(shardingValue));

        return tableName.toString();
    }

}
