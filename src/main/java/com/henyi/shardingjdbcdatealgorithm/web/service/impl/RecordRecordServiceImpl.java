package com.henyi.shardingjdbcdatealgorithm.web.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.henyi.shardingjdbcdatealgorithm.web.entity.Record;
import com.henyi.shardingjdbcdatealgorithm.web.mapper.RecordMapper;
import com.henyi.shardingjdbcdatealgorithm.web.service.RecordService;
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
@Transactional
public class RecordRecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    @Override
    public List getList(Map<String, Object> params) {
        Date startTime = MapUtil.getDate(params, "startTime");
        Date endTime = MapUtil.getDate(params, "endTime");
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        if (startTime != null) {
            queryWrapper.ge("RECORD_DATE", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("RECORD_DATE", endTime);
        }
        return list(queryWrapper);
    }
}