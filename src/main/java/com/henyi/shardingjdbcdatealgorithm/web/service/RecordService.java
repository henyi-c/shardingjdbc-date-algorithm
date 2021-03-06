package com.henyi.shardingjdbcdatealgorithm.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.henyi.shardingjdbcdatealgorithm.web.entity.Record;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 业务层
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
public interface RecordService extends IService<Record> {
    List getList(Map<String, Object> params);

    Boolean saveRecord(Record record);

    Boolean updateRecord(Record record);

    Boolean removeRecord(String id);
}
