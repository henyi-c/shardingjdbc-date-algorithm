package com.henyi.shardingjdbcdatealgorithm.sharding.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 适用于日期策略使用的雪花算法
 *
 * @author henyi
 */
public class ShardingDateAlgorithmSnowFlake {

    private static final ShardingDateAlgorithmSnowFlake WORKER = new ShardingDateAlgorithmSnowFlake(1, 1);

    /**
     * 起始的时间戳
     */
    private final static long START_STAMP = 1607155485176L;


    //每一部分占用的位数

    /**
     * 序列号占用的位数
     */
    private final static long SEQUENCE_BIT = 12;

    /**
     * 机器标识占用的位数
     */
    private final static long MACHINE_BIT = 5;

    /**
     * 数据中心占用的位数
     */
    private final static long DATACENTER_BIT = 5;

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    /**
     * 数据中心
     */
    private final long datacenterId;

    /**
     * 机器标识
     */
    private final long machineId;

    /**
     * 控制序列号
     */
    private long sequence = 0L;

    /**
     * 实际使用的序列号
     */
    private long actualSequence = 0L;

    /**
     * 上一次时间戳
     */
    private long lastStamp = -1L;

    public ShardingDateAlgorithmSnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return ID
     */
    public synchronized long nextId() {
        long currStamp = getNewStamp();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        //时间不连续出来全是偶数
        if (currStamp == lastStamp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }
        // 上面那个控制序列号sequence，控制了一毫秒内不会超过MAX_SEQUENCE(4096)个，超过会等待，直到下一毫秒才会继续
        // 上面那个序列号sequence如果一毫秒只生成一个id，那么它永远都是0，那么取模永远都是0，插入的表也就可以理解为都是0表（或规律的那几张表），达不到均匀分布在各表的目的
        // 所以用下面这个序列号actualSequence来生成均匀的取模id，达到均匀分布在各表的目的
        actualSequence = (actualSequence + 1) & MAX_SEQUENCE;

        lastStamp = currStamp;
        //时间戳部分
        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                //数据中心部分
                | datacenterId << DATACENTER_LEFT
                //机器标识部分
                | machineId << MACHINE_LEFT
                //序列号部分
                | actualSequence;
    }

    private long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    private long getNewStamp() {
        return System.currentTimeMillis();
    }

    public static String getId(String dateAlgorithmType, Date date) {
        SimpleDateFormat dateFormat = ShardingDateUtils.getDateFormat(dateAlgorithmType);
        return dateFormat.format(date) + WORKER.nextId();
    }

    public static String getId(String dateAlgorithmType, String date) throws ParseException {
        SimpleDateFormat dateFormat = ShardingDateUtils.getDateFormat(dateAlgorithmType);
        return dateFormat.format(dateFormat.parse(date)) + WORKER.nextId();
    }

    /**
     * 默认采用月份策略生成ID
     * @return
     */
    public static String getId() {
        SimpleDateFormat dateFormat = ShardingDateUtils.getDateFormat("month");
        return dateFormat.format( new Date()) + WORKER.nextId();
    }
}
