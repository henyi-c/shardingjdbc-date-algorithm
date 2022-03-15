package com.henyi.shardingjdbcdatealgorithm.web.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.DateAlgorithmRange;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingDateAlgorithmSnowFlake;
import com.henyi.shardingjdbcdatealgorithm.web.entity.Record;
import com.henyi.shardingjdbcdatealgorithm.web.mapper.RecordMapper;
import com.henyi.shardingjdbcdatealgorithm.web.service.RecordService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 业务实现类
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Service
@ShardingTransactionType(TransactionType.XA)
@Transactional(rollbackFor = Exception.class)
public class RecordRecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    @Override
    public List getList(Map<String, Object> params) {
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        String id = MapUtil.getStr(params, "id");
        Date startTime = MapUtil.getDate(params, "startTime");
        Date endTime = MapUtil.getDate(params, "endTime");
        if (!StringUtils.isBlank(id)) {
            queryWrapper.eq("ID", id);
        }
        if (startTime != null) {
            queryWrapper.ge("RECORD_DATE", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("RECORD_DATE", endTime);
        }
        return list(queryWrapper);
    }

    @Override
    public Boolean saveRecord(Record record) {
        record.setId(ShardingDateAlgorithmSnowFlake.getId(DateAlgorithmRange.DATE_ALGORITHM_RANGE_MONTH, record.getRecordDate()));
        return save(record);
    }

    @Override
    public Boolean updateRecord(Record record) {
        LambdaUpdateWrapper<Record> updateWrapper = Wrappers.<Record>lambdaUpdate()
                .set(Record::getRecordContent, record.getRecordContent())
                .eq(Record::getId, record.getId());
        return update(updateWrapper);
    }

    @Override
    public Boolean removeRecord(String id) {
        LambdaUpdateWrapper<Record> updateWrapper = Wrappers.<Record>lambdaUpdate()
                .eq(Record::getId, id);
        return remove(updateWrapper);
    }
}