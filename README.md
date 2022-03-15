
# shardingsphere-jdbc-date-algorithm

<p  align="center">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg">
    <img alt="springboot-2.5.2" src="https://img.shields.io/badge/spring--boot-2.5.2-release.svg">
    <img alt="mybatis-plus-3.5.1" src="https://img.shields.io/badge/mybatis--plus-3.5.1-blue.svg">
</p>

## 项目介绍

基于springboot+shardingsphere-jdbc+mybatisplus实现按日期 年/月/日 策略进行动态分表兼容配置多数据源，读写分离，主从等


## 使用说明

根目录有postman文件测试案例

具体可参照案例进行实现即可


 ### 新增操作
    插入的数据中请不要乱设置ID的值，主键必须人为代码中设置ID为ShardingDateAlgorithmSnowFlake.getId(dateAlgorithmType,date)
    插入的数据中该时间字段的值若非为时间区间的表所存储对应的时间数据，就会落在原始表(record)中,符合规则的落入对应表中


 ### 查询操作
    如果不包含查询条件，就会去查询所有的库中对应该业务表的真实表
    如果只带了主键条件，因为主键的值ShardingDateAlgorithmSnowFlake.getId(dateAlgorithmType,date)是带有时间意义的，那么可以直接定位到具体的库和具体的表
    如果只带了时间，根据时间范围去多个库中查，拿取对应范围的表。
    如果两个都带，就直接定位主键条件即可


 ### 更新操作
    不能更新复合分片的值（主键的值和时间值），这样会打乱分片策略，定位不到数据位置



## 注意事项
    1. 各库的数据表结构、数据初始化时需保持一致(数据保持一致是对于不进行分片的表而言，分片的表数据是不同的，因为按照分片策略来进行存入)
    2. 规定采用该规则分片的表生成的ID必须为varchar类型且采用ShardingDateAlgorithmSnowFlake.getId(dateAlgorithmType,date)进行生成
    3. 必须采用@ShardingTransactionType(TransactionType.XA)和@Transactional(rollbackFor = Exception.class)进行搭配
    4. 本项目只演示了基本的demo,实际业务上会存在很多sharding不支持的sql问题，具体可访问查看具体sql注意点 https://blog.51cto.com/u_14355948/2708929