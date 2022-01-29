package com.henyi.shardingjdbcdatealgorithm.sharding.algorithm;

import com.henyi.shardingjdbcdatealgorithm.sharding.util.HashMapConst;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingConstant;
import com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean.DynamicTableByDate;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import com.google.common.collect.Range;

import java.util.*;

/**
 * @author henyi
 */
public class ComplexKeysShardingAlgorithmForTb implements ComplexKeysShardingAlgorithm {


    @Override
    public Collection<String> doSharding(Collection databaseNames, ComplexKeysShardingValue complexKeysShardingValue) {
        //HashMapConst中查询该表名
        DynamicTableByDate dynamicTableByDate = HashMapConst.tableAlgorithmContain.get(ShardingConstant.ALGORITHM_DATE_TABLE + complexKeysShardingValue.getLogicTableName().trim());

        List<String> shardingSuffix = new ArrayList<>();

        getPrecise(complexKeysShardingValue, dynamicTableByDate, shardingSuffix);
        if (!shardingSuffix.isEmpty()) {
            return shardingSuffix;
        }
        getRange(complexKeysShardingValue, dynamicTableByDate, shardingSuffix);
        if (!shardingSuffix.isEmpty()) {
            return shardingSuffix;
        }
        shardingSuffix.add(complexKeysShardingValue.getLogicTableName());
        return shardingSuffix;
    }


    /**
     * 分片拿取值
     *
     * @param columnNameAndShardingValuesMap
     * @param key
     * @return
     */
    private Collection getShardingValue(Map<String, Collection> columnNameAndShardingValuesMap, String key) {
        Collection valueSet = new ArrayList<>();

        if (columnNameAndShardingValuesMap.containsKey(key.toLowerCase())) {
            key = key.toLowerCase();
        } else if (columnNameAndShardingValuesMap.containsKey(key.toUpperCase())) {
            key = key.toUpperCase();
        } else {
            return valueSet;
        }
        if (columnNameAndShardingValuesMap.get(key) instanceof Collection) {
            valueSet.addAll(columnNameAndShardingValuesMap.get(key));
        } else {
            valueSet.add(columnNameAndShardingValuesMap.get(key));
        }
        return valueSet;
    }


    /**
     * 精确分片
     * @param complexKeysShardingValue
     * @param dynamicTableByDate
     * @param shardingSuffix
     */
    @SneakyThrows
    private void getPrecise(ComplexKeysShardingValue complexKeysShardingValue, DynamicTableByDate dynamicTableByDate, List<String> shardingSuffix) {
        // 得到每个分片健精确的对应的值
        Collection<String> idValues = this.getShardingValue(complexKeysShardingValue.getColumnNameAndShardingValuesMap(), dynamicTableByDate.getShardingColumnsId());

        //选择哪张表
        //优先级 id>record_date

        //id不为空，进行返回对应表
        if (!idValues.isEmpty()) {
            for (String id : idValues) {
                shardingSuffix.add(ShardingAlgorithmOfPreciseForTb.doSharding(dynamicTableByDate, id));
            }
            return;
        }

    }


    /**
     * 拿取范围分片
     *
     * @param complexKeysShardingValue
     * @param dynamicTableByDate
     * @param shardingSuffix
     */
    @SneakyThrows
    private void getRange(ComplexKeysShardingValue complexKeysShardingValue, DynamicTableByDate dynamicTableByDate, List<String> shardingSuffix) {

        Collection<Range<Date>> recordDateValues = this.getShardingValue(complexKeysShardingValue.getColumnNameAndRangeValuesMap(), dynamicTableByDate.getShardingColumnsDate());

        //id为空，时间不为空，返回时间对应的表
        if (!recordDateValues.isEmpty()) {
            for (Range<Date> range : recordDateValues) {
                shardingSuffix.addAll(ShardingAlgorithmOfRangeForTb.doSharding(dynamicTableByDate, range));
            }
        }
    }

}

