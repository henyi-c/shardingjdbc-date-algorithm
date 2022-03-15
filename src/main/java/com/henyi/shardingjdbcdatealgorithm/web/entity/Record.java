package com.henyi.shardingjdbcdatealgorithm.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 实体类
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Record implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private String id;

    /**
     * 记录内容
     */
    @TableField("RECORD_CONTENT")
    private String recordContent;

    /**
     * 记录日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("RECORD_DATE")
    private Date recordDate;
}
