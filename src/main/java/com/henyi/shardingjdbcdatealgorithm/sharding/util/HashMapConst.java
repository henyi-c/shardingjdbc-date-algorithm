package com.henyi.shardingjdbcdatealgorithm.sharding.util;

import com.henyi.shardingjdbcdatealgorithm.sharding.ymlbean.DynamicTableByDate;

import java.util.HashMap;

/**
 * <p>
 * 作为容器常量存储分表的最大最小日期值
 *
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
public class HashMapConst {

    public static final HashMap<String, DynamicTableByDate> tableAlgorithmContain = new HashMap<>();
}
