package com.henyi.shardingjdbcdatealgorithm.web.controller;

import com.henyi.shardingjdbcdatealgorithm.web.entity.Record;
import com.henyi.shardingjdbcdatealgorithm.web.service.RecordService;
import com.henyi.shardingjdbcdatealgorithm.web.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @Resource
    private RecordService recordService;


    @PostMapping(value = "list", produces = "application/json")
    public Result<?> list(@RequestBody Map<String, Object> params) {
        return Result.success(recordService.getList(params));
    }

    @PostMapping(value = "insert", produces = "application/json")
    public Result<?> insert(@RequestBody Record record) {
        return Result.isSuccess(recordService.saveRecord(record));
    }


    @PostMapping(value = "update", produces = "application/json")
    public Result<?> update(@RequestBody Record record) {
        return Result.isSuccess(recordService.updateRecord(record));
    }


    @GetMapping(value = "delete/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        return Result.isSuccess(recordService.removeRecord(id));
    }
}
