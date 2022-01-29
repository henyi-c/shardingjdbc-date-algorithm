package com.henyi.shardingjdbcdatealgorithm.sharding.schedule;

import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.HashMapConst;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingConstant;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingDateUtils;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingDbUtils;
import com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean.DbSql;
import com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean.DynamicTableByDate;
import com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean.DynamicTableListByDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.underlying.common.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.underlying.common.rule.DataNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: leo我男神
 * @Date: 2021/5/13 9:45
 * 定时任务进行更新和执行分表
 */
@Component
@EnableScheduling
@EnableConfigurationProperties(DynamicTableListByDate.class)
@ConfigurationProperties(prefix = "spring.shardingsphere")
@Slf4j
public class ShardingTableRuleActualTablesRefreshSchedule implements InitializingBean {

    private boolean enabled;

    @Resource
    private DynamicTableListByDate list;

    @Resource
    private DataSource dataSource;

    public ShardingTableRuleActualTablesRefreshSchedule() {
    }

    /**
     * 每天执行一次
     * 跟随策略每日重设最小物理表到规则中，且根据规则生成当前日表
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void actualTablesRefreshForDay() throws NoSuchFieldException, IllegalAccessException, ParseException {
        setDynamicTableRule("day");
    }


    /**
     * 每月执行一次
     * 跟随策略每月重设最小物理表到规则中，且根据规则生成当前月表
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void actualTablesRefreshForMonth() throws NoSuchFieldException, IllegalAccessException, ParseException {
        setDynamicTableRule("month");
        setDynamicTableRule("year");
    }


    /**
     * 根据传递的策略范围来筛选对应的表进行真实表映射到sharding中
     *
     * @param strategyRange
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void setDynamicTableRule(String strategyRange) throws NoSuchFieldException, IllegalAccessException, ParseException {
        //未开启sharding
        if (!enabled) {
            return;
        }
        List<DynamicTableByDate> list = this.list.getTableList();

        //未配置基于平台的策略,直接返回
        if (Objects.isNull(list)) {
            return;
        }
        if (!StringUtils.isBlank(strategyRange)) {
            list = this.list.getTableList().stream().filter(e -> strategyRange.equals(e.getRange())).collect(Collectors.toList());
        }

        ShardingDataSource dataSource = (ShardingDataSource) this.dataSource;
        ShardingRule rule = dataSource.getRuntimeContext().getRule();
        rule.getBroadcastTables();
        //获取默认源中所有的表名
        List<String> defaultSourceAllTable = ShardingDbUtils.getDefaultSourceAllTable(dataSource);
        //筛选掉需要分片的表
        List<String> unCutTables = filterTables(defaultSourceAllTable, list);
        //设置所有未分片的库变为广播表
        rule.getRuleConfiguration().setBroadcastTables(unCutTables);
        rule.getBroadcastTables().addAll(unCutTables);

        for (DynamicTableByDate dynamicTable : list) {
            String range = dynamicTable.getRange();
            Integer offset = dynamicTable.getOffset();

            if (StringUtils.isBlank(range)) {
                throw new RuntimeException("sharding: table range is wrong value ! ");
            }
            SimpleDateFormat dateFormat = ShardingDateUtils.getDateFormat(range);

            String tableName = dynamicTable.getName();

            TableRule tableRule = null;
            try {
                tableRule = rule.getTableRule(tableName);
                log.info(tableRule.toString());
            } catch (ShardingSphereConfigurationException e) {
                log.error("逻辑表：{},不存在配置！", tableName);
                continue;
            }

            //设置最大最小表并进行存储
            setMinAndMaxTable(tableName, dynamicTable, dataSource, dateFormat, range, offset);
            String minYmd = dynamicTable.getMinDate();
            //检测数据库表是否更新，并且检查各个数据库是否表名都一致
            checkDB(tableName, dateFormat, dataSource, dynamicTable, range);


            //默认水平分表开始
            LocalDateTime localDateTime;
            LocalDateTime maxLocalDateTime = LocalDateTime.now();
            switch (range) {
                case "year":
                    localDateTime = LocalDateTime.of(Integer.valueOf(minYmd), 1, 1, 0, 0, 0);
                    maxLocalDateTime = maxLocalDateTime.plusYears(offset);
                    break;
                case "day":
                    localDateTime = LocalDateTime.of(Integer.valueOf(minYmd.substring(0, 4)), Integer.valueOf(minYmd.substring(4, 6)), Integer.valueOf(minYmd.substring(6, 8)), 0, 0, 0);
                    maxLocalDateTime = maxLocalDateTime.plusDays(offset);
                    break;
                case "month":
                default:
                    localDateTime = LocalDateTime.of(Integer.valueOf(minYmd.substring(0, 4)), Integer.valueOf(minYmd.substring(4)), 1, 0, 0, 0);
                    maxLocalDateTime = maxLocalDateTime.plusMonths(offset);
            }
            setActualTable(range, tableRule, localDateTime, maxLocalDateTime, dateFormat);
        }
    }


    /**
     * 过滤掉需要分片的表，剩下的指定默认数据源
     *
     * @param defaultSourceAllTable
     * @param list
     * @return
     */
    private List<String> filterTables(List<String> defaultSourceAllTable, List<DynamicTableByDate> list) {
        List<String> cutTables = new ArrayList<>();

        for (DynamicTableByDate dynamicTable : list) {
            String tableName = dynamicTable.getName();
            for (String defaultTableName : defaultSourceAllTable) {
                if (defaultTableName.equals(tableName)) {
                    cutTables.add(defaultTableName);
                } else if (defaultTableName.contains(tableName) && defaultTableName.contains("_")) {
                    String number = defaultTableName.substring(defaultTableName.lastIndexOf("_") + 1);
                    if (number.length() >= 4 && Pattern.matches("^[-\\+]?[\\d]*$", number)) {
                        cutTables.add(defaultTableName);
                    }
                }
            }
        }
        defaultSourceAllTable.removeAll(cutTables);
        return defaultSourceAllTable;
    }


    /**
     * 设置最大最小表并进行存储
     *
     * @param tableName
     * @param dynamicTable
     * @param dataSource
     * @return
     */
    private void setMinAndMaxTable(String tableName, DynamicTableByDate dynamicTable, ShardingDataSource dataSource, SimpleDateFormat dateFormat, String range, int offset) throws ParseException {
        String tableLikeName = ShardingDbUtils.getTableLikeNames(tableName.trim() + "_", dataSource, dateFormat);
        String minTableYmd = tableLikeName.replace(tableName.trim() + "_", "");
        //设置最小时间
        dynamicTable.setMinDate(minTableYmd);
        //设置最大时间
        Date minDate = DateUtil.parse(minTableYmd, dateFormat.toPattern());
        //先取得最小时间表到当前时间的offset,然后再加上配置的offset即为最大值表
        Calendar maxCalendar = ShardingDateUtils.getCalendar(minTableYmd, range, ShardingDateUtils.getBetweenOffset(minDate, new Date(), range) + offset);
        dynamicTable.setMaxDate(dateFormat.format(maxCalendar.getTime()));
        //存入容器常量中
        HashMapConst.tableAlgorithmContain.put(ShardingConstant.ALGORITHM_DATE_TABLE + tableName.trim(), dynamicTable);
    }


    /**
     * 检测数据库表是否更新，并且检查各个数据库是否表名都一致
     *
     * @param tableName
     * @param dateFormat
     * @param dataSource
     * @param dynamicTable
     */
    private void checkDB(String tableName, SimpleDateFormat dateFormat, ShardingDataSource dataSource, DynamicTableByDate dynamicTable, String range) throws ParseException {


        //拿取多个数据源
        Map<String, DataSource> dataSourceMap = dataSource.getDataSourceMap();

        Set<String> keySet = dataSourceMap.keySet();

        Iterator<String> dataSourceIterator = keySet.iterator();


        //遍历数据源
        while (dataSourceIterator.hasNext()) {

            //数据源对应的数据库类型名称
            String dbName = dataSourceIterator.next();

            //如果是从库，不执行新增表（主从一致会自动同步）
            if (dbName.contains("slave")) {
                continue;
            }
            //数据源
            DataSource dataSourceOne = dataSourceMap.get(dbName);

            //判断原表是否存在，不存在则创建
            createTable(dynamicTable, dataSourceOne, dbName, tableName, tableName);

            //最小时间表到最大时间偏移量为多少
            Date minDate = DateUtil.parse(dynamicTable.getMinDate(), dateFormat.toPattern());
            Date maxDate = DateUtil.parse(dynamicTable.getMaxDate(), dateFormat.toPattern());
            int minBetweenMaxOffset = ShardingDateUtils.getBetweenOffset(minDate, maxDate, range);

            //遍历偏移量,由最小时间到最大时间进行查询
            for (int i = 0; i <= minBetweenMaxOffset; i++) {

                Calendar calendar = ShardingDateUtils.getCalendar(dynamicTable.getMinDate(), range, i);
                //查询当前时间表是否存在
                String tableNameByDate = tableName.trim() + "_" + dateFormat.format(calendar.getTime());
                //不存在则创建表
                createTable(dynamicTable, dataSourceOne, dbName, tableName, tableNameByDate);
            }
        }

    }


    /**
     * 不存在则创建表
     *
     * @param dynamicTable
     * @param dataSourceOne
     * @param dbName
     * @param tableName
     * @param tableNameByDate
     */
    private void createTable(DynamicTableByDate dynamicTable, DataSource dataSourceOne, String dbName, String tableName, String tableNameByDate) {
        //如果不存在表
        if (!ShardingDbUtils.isTableExist(tableNameByDate, dataSourceOne)) {
            //筛选拿取对于该数据库类型的创建表sql
            List<DbSql> dbSqlList = dynamicTable.getDbList().stream()
                    .filter((DbSql db) -> dbName.contains(db.getDbType()))
                    .collect(Collectors.toList());

            if (dbSqlList.isEmpty()) {
                return;
            }
            DbSql dbSql = dbSqlList.get(0);
            //创建数据表
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceOne);
            jdbcTemplate.update(dbSql.getCreateTableSql().replace(tableName.trim(), tableNameByDate));
        }
    }


    /**
     * 动态设置真实表到sharding中
     *
     * @param range
     * @param tableRule
     * @param localDateTime
     * @param dateFormat
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void setActualTable(String range, TableRule tableRule, LocalDateTime localDateTime, LocalDateTime maxLocalDateTime, SimpleDateFormat dateFormat) throws NoSuchFieldException, IllegalAccessException {
        //筛选拿到的最小时间到最大时间，根据所选range进行迭代动态设置进actualDataNodes
        List<DataNode> dataNodes = tableRule.getActualDataNodes();
        List<DataNode> newDataNodes = new ArrayList<>();
        Set<String> actualTables = Sets.newHashSet();
        Map<DataNode, Integer> dataNodeIntegerMap = Maps.newHashMap();
        AtomicInteger a = new AtomicInteger(0);
        Map<String, Collection<String>> datasourceToTablesMap = Maps.newHashMap();
        LocalDateTime localDateTimeTemp = LocalDateTime.from(localDateTime);
        for (DataNode dataNode : dataNodes) {
            localDateTime = LocalDateTime.from(localDateTimeTemp);
            String dataSourceName = dataNode.getDataSourceName();
            String logicTableName = tableRule.getLogicTable();
            StringBuilder stringBuilder = new StringBuilder(10).append(dataSourceName).append(".").append(logicTableName + "_");
            final int length = stringBuilder.length();
            newDataNodes.add(new DataNode(new StringBuilder(10).append(dataSourceName).append(".").append(logicTableName).toString()));
            actualTables.add(logicTableName);
            while (true) {
                stringBuilder.setLength(length);
                stringBuilder.append(localDateTime.format(DateTimeFormatter.ofPattern(dateFormat.toPattern())));
                DataNode dataNodeTemp = new DataNode(stringBuilder.toString());
                newDataNodes.add(dataNodeTemp);
                actualTables.add(dataNodeTemp.getTableName());
                switch (range) {
                    case "year":
                        localDateTime = localDateTime.plusYears(1L);
                        break;
                    case "day":
                        localDateTime = localDateTime.plusDays(1L);
                        break;
                    case "month":
                    default:
                        localDateTime = localDateTime.plusMonths(1L);
                }
                if (localDateTime.isAfter(maxLocalDateTime)) {
                    break;
                }
            }

            newDataNodes.forEach((dataNodeTemp -> {
                if (a.intValue() == 0) {
                    a.incrementAndGet();
                    dataNodeIntegerMap.put(dataNodeTemp, 0);
                } else {
                    dataNodeIntegerMap.put(dataNodeTemp, a.intValue());
                    a.incrementAndGet();
                }
            }));
            datasourceToTablesMap.put(dataSourceName, actualTables);
        }

        //动态刷新actualDataNodesField
        Field actualDataNodesField = TableRule.class.getDeclaredField("actualDataNodes");
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(actualDataNodesField, actualDataNodesField.getModifiers() & ~Modifier.FINAL);
        actualDataNodesField.setAccessible(true);
        actualDataNodesField.set(tableRule, newDataNodes);
        //动态刷新actualTables
        Field actualTablesField = TableRule.class.getDeclaredField("actualTables");
        actualTablesField.setAccessible(true);
        actualTablesField.set(tableRule, actualTables);
        //动态刷新：dataNodeIndexMap
        Field dataNodeIndexMapField = TableRule.class.getDeclaredField("dataNodeIndexMap");
        dataNodeIndexMapField.setAccessible(true);
        dataNodeIndexMapField.set(tableRule, dataNodeIntegerMap);
        //动态刷新：datasourceToTablesMap
        Field datasourceToTablesMapField = TableRule.class.getDeclaredField("datasourceToTablesMap");
        datasourceToTablesMapField.setAccessible(true);
        datasourceToTablesMapField.set(tableRule, datasourceToTablesMap);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //检查全部关于日期分表策略的所有表
        setDynamicTableRule(null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}