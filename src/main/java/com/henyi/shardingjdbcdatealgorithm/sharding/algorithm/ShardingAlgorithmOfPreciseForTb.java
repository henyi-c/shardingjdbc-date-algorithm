package com.henyi.shardingjdbcdatealgorithm.sharding.algorithm;

import com.henyi.shardingjdbcdatealgorithm.sharding.util.HashMapConst;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingConstant;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingDateUtils;
import com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean.DynamicTableByDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
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
public class ShardingAlgorithmOfPreciseForTb implements PreciseShardingAlgorithm<Date> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {


        StringBuffer tableName = new StringBuffer();

        //HashMapConst中查询该表名，拿取最大最小时间
        DynamicTableByDate dynamicTableByDate = (DynamicTableByDate) HashMapConst.contain.get( ShardingConstant.ALGORITHM_DATE_TABLE + shardingValue.getLogicTableName().trim());
        SimpleDateFormat dateFormat = ShardingDateUtils.getDateFormat(dynamicTableByDate.getRange());

        String minDate = dynamicTableByDate.getMinDate();
        String maxDate = dynamicTableByDate.getMaxDate();
        try {
            //小于最小时间或大于最大时间
            if (dateFormat.parse(minDate).getTime() > shardingValue.getValue().getTime() || shardingValue.getValue().getTime() > dateFormat.parse(maxDate).getTime()) {
                return shardingValue.getLogicTableName();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        log.info("执行操作的表名{}", shardingValue.getLogicTableName() + "_" + dateFormat.format(shardingValue.getValue()));

        tableName.append(shardingValue.getLogicTableName()).append("_").append(dateFormat.format(shardingValue.getValue()));

        return tableName.toString();
    }
}
