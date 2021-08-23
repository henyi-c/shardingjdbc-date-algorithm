package com.henyi.shardingjdbcdatealgorithm.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.DateAlgorithmRange;
import com.henyi.shardingjdbcdatealgorithm.sharding.util.ShardingDateAlgorithmSnowFlake;
import com.henyi.shardingjdbcdatealgorithm.web.entity.Record;
import com.henyi.shardingjdbcdatealgorithm.web.service.RecordService;
import com.henyi.shardingjdbcdatealgorithm.web.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;


/**
 * <p>
 * sharding测试
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@RestController
@RequestMapping("/record")
@Slf4j
public class RecordController {

    @Autowired
    private RecordService recordService;



    @PostMapping("/list")
    public Result list(@RequestBody Map<String, Object> params) {
        return Result.success(recordService.getList(params));
    }



    @PostMapping("/insert")
    public Result insert(@ModelAttribute Record record) {
        record.setId(ShardingDateAlgorithmSnowFlake.getId(DateAlgorithmRange.DATE_ALGORITHM_RANGE_MONTH,record.getRecordDate()));
        return Result.isSuccess(recordService.save(record));
    }




    @PostMapping("/update")
    public Result update(@ModelAttribute Record record) {
        return Result.isSuccess(recordService.update(Wrappers.<Record>lambdaUpdate()
                .set(Record::getRecordContent, record.getRecordContent())
                .eq(Record::getId, record.getId())));
    }



    @GetMapping("/delete")
    public Result delete(@RequestParam BigDecimal id) {
        return Result.isSuccess(recordService.remove(Wrappers.<Record>lambdaUpdate()
                .eq(Record::getId, id)));
    }
}
