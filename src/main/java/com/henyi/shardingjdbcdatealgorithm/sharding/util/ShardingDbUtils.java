package com.henyi.shardingjdbcdatealgorithm.sharding.util;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * sharding对多种库的兼容处理
 *
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
public class ShardingDbUtils {


    /**
     * 查询表是否存在该数据库中
     *
     * @param tableName
     * @param dataSource
     * @return
     */
    public static boolean isTableExist(String tableName, DataSource dataSource) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            rs = null;
            DatabaseMetaData data = conn.getMetaData();

            String[] types = {"TABLE"};
            rs = data.getTables(getDatabaseName(data), null, tableName, types);
            if (rs.next()) {
                String a = rs.getString("TABLE_NAME");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * 查询最小时间表
     *
     * @param tableName
     * @param dataSource
     * @return
     */
    public static String getTableLikeNames(String tableName, ShardingDataSource dataSource, SimpleDateFormat dateFormat) {

        //默认数据源
        String defaultDataSourceName = dataSource.getRuntimeContext().getRule().getRuleConfiguration().getDefaultDataSourceName();
        Map<String, DataSource> dataSourceMap = dataSource.getDataSourceMap();
        DataSource defaultDataSource = dataSourceMap.get(defaultDataSourceName);

        List<String> tableNames = new ArrayList<>();

        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = defaultDataSource.getConnection();
            rs = null;
            DatabaseMetaData data = conn.getMetaData();
            String[] types = {"TABLE"};
            rs = data.getTables(getDatabaseName(data), null, tableName + "%", types);
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(tableNames, (item1, item2) -> getYmd(item1, tableName) - getYmd(item2, tableName));

        //列表为空，代表无生成日期表，默认当前日期对应的日期策略作为最小时间表
        return tableNames.isEmpty() ? tableName + dateFormat.format(new Date()) : tableNames.get(0);
    }

    /**
     * 把年月日转成Long进行对比
     *
     * @param tableLikeName
     * @param tableName
     * @return
     */
    private static Integer getYmd(String tableLikeName, String tableName) {
        String ymd = tableLikeName.replace(tableName.trim(), "");
        return Integer.parseInt(ymd);
    }


    /**
     * 根据不同数据库类别获取数据库名称
     *
     * @param data
     * @return
     * @throws SQLException
     */
    private static String getDatabaseName(DatabaseMetaData data) throws SQLException {
        //数据库URL
        String url = data.getURL();
        //数据库名称
        String dbName;
        //数据库类别
        String databaseProductName = data.getDatabaseProductName();

        switch (databaseProductName) {
            case "ORACLE": {
                dbName = url.substring(url.lastIndexOf(":") + 1);
            }
            break;
            case "DB2": {
                String databaseUrl = url.substring(0, url.lastIndexOf(":"));
                dbName = databaseUrl.substring(databaseUrl.lastIndexOf("/") + 1);
            }
            break;
            case "SQLServer": {
                String s = url.toUpperCase();
                dbName = url.substring(s.lastIndexOf("DATABASENAME=") + 1);
            }
            break;
            case "MYSQL":
            default: {
                String substring = url.substring(0, url.indexOf("?"));
                dbName = substring.substring(substring.lastIndexOf("/") + 1);
            }
            break;
        }
        return dbName;

    }


}
