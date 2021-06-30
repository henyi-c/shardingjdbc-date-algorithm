package com.henyi.shardingjdbcdatealgorithm.sharding.algorithm;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.HashMapConst;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingConstant;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingDateUtils;
import com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean.DynamicTableByDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 自定义分片算法类，用于当SQL语句中包含了分片键
 * sharding-jdbc会调用该类的doSharding方法，得到要查询的实际数据表名
 * 自定义standard的范围分片（between）
 *
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Slf4j
@Component
public class ShardingAlgorithmOfRangeForTb implements RangeShardingAlgorithm<Date> {


    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Date> shardingValue) {
        Collection<String> result = new LinkedHashSet<>();

        Range<Date> shardingKey = shardingValue.getValueRange();
        //逻辑表名
        String logicTableName = shardingValue.getLogicTableName().trim();

        //默认将初始表放入落点
        Collection<String> tables = new HashSet<>();
        tables.add(logicTableName);
        result.addAll(tables);

        //HashMapConst查询该表名，拿取range
        DynamicTableByDate dynamicTableByDate = (DynamicTableByDate) HashMapConst.contain.get(ShardingConstant.ALGORITHM_DATE_TABLE + logicTableName);
        String range = dynamicTableByDate.getRange();
        SimpleDateFormat dateFormat = ShardingDateUtils.getDateFormat(dynamicTableByDate.getRange());

        //该表名中的最小时间
        Date minTableDate = ShardingDateUtils.getTableDate(dynamicTableByDate.getMinDate(), dateFormat);
        //该表名中的最大时间
        Date maxTableDate = ShardingDateUtils.getTableDate(dynamicTableByDate.getMaxDate(), dateFormat);


        Date endTime;
        try {
            endTime = shardingKey.upperEndpoint();
        } catch (IllegalStateException e) {
            endTime = maxTableDate;
        }
        Date startTime;

        try {
            startTime = shardingKey.lowerEndpoint();
        } catch (IllegalStateException e) {
            startTime = minTableDate;
        }

        //开始时间和结束时间都小于最小日期表，返回默认表数据
        if (startTime.before(minTableDate) && endTime.before(minTableDate)) {
            return result;
        }

        //开始时间比最小时间还早,把最小值赋予开始时间
        if (startTime.before(minTableDate)) {
            startTime = minTableDate;
        }
        //开始时间比最大时间还晚,把最大值赋予开始时间
        if (startTime.after(maxTableDate)) {
            startTime = maxTableDate;
        }
        //结束时间比最小时间还早,把最小值赋予结束时间
        if (endTime.before(maxTableDate)) {
            endTime = minTableDate;
        }
        //结束时间比最大时间还晚,把最大值赋予结束时间
        if (endTime.after(maxTableDate)) {
            endTime = maxTableDate;
        }
        //开始时间大于结束时间，这种情况返回默认表数据
        if (startTime.after(endTime)) {
            return result;
        }

        tables.addAll(getRoutTable(dateFormat, range, shardingValue.getLogicTableName(), startTime, endTime));

        if (tables != null && tables.size() > 0) {
            result.addAll(tables);
        }
        return result;
    }


    /**
     * 拼接路由表
     *
     * @param dateFormat
     * @param logicTableName
     * @param startTime
     * @param endTime
     * @return
     */
    private Collection<String> getRoutTable(SimpleDateFormat dateFormat, String range, String logicTableName, Date startTime, Date endTime) {

        Set<String> rouTables = new HashSet<>();

        if (startTime != null && endTime != null) {
            List<String> rangeNameList = getRangeNameList(dateFormat, range, startTime, endTime);
            for (String yearMonth : rangeNameList) {
                rouTables.add(logicTableName + "_" + yearMonth);
            }
        }
        return rouTables;
    }

    /**
     * 根据日期获取数据表名的范围
     *
     * @param dateFormat
     * @param startTime
     * @param endTime
     * @return
     */
    private List<String> getRangeNameList(SimpleDateFormat dateFormat, String range, Date startTime, Date endTime) {

        List<String> result = Lists.newArrayList();

        // 定义日期实例
        Calendar dd = Calendar.getInstance();

        dd.setTime(startTime);

        while (dd.getTime().before(endTime)) {
            result.add(dateFormat.format(dd.getTime()));
            // 根据策略进行 + 1
            dd.add(ShardingDateUtils.getCalendarDateRange(range), 1);
        }
        return result;
    }





}