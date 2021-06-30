package com.henyi.shardingjdbcdatealgorithm.sharding.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;


/**
 * <p>
 * 自定义实现 精准分片算法（PreciseShardingAlgorithm）接口
 * 数据库DB的精准分片 (in / eq)
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Slf4j
public class ShardingAlgorithmOfPreciseForDb implements PreciseShardingAlgorithm<Long> {

    /**
     * @param databaseNames 有效的数据源
     * @param shardingValue 分片键的值
     * @return
     */
    @Override
    public String doSharding(Collection<String> databaseNames,
                             PreciseShardingValue<Long> shardingValue) {

        String[] arrays = new String[databaseNames.size()];
        databaseNames.toArray(arrays);
        Long index = shardingValue.getValue() % databaseNames.size();
        for (Integer i = 0; i < arrays.length; i++) {
            //如果数据源有多个，根据分片键取模数据源个数，如果得到的数和循环相等，那么就落点该数据源
            String databaseName = arrays[i];
            if (i == index.intValue()) {
                //返回相应的数据库
                log.info("databaseName" + databaseName);
                return databaseName;
            }
        }
        throw new UnsupportedOperationException();
    }
}
