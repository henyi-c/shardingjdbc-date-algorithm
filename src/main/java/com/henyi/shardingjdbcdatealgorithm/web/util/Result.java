package com.henyi.shardingjdbcdatealgorithm.web.util;

import lombok.Data;

import java.io.Serializable;


/**
 * <p>
 * 接口返回对象封装类
 * </p>
 *
 * @author henyi-c
 * @since 2021-07-01
 */
@Data
public class Result<T> implements Serializable {

    //返回信息常量
    private static final String SUCCESS_MESSAGE = "success";
    private static final String FAIL_MESSAGE = "fail";
    //返回状态码常量
    private static final int SUCCESS_CODE = 200;
    private static final int FAIL_CODE = 500;
    //返回默认数据
    private static final Object DEFAULT_DATA = null;
    //状态码
    private Integer code;
    //返回信息
    private String msg;
    //返回数据
    private T data;


    //全参构造
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }



    /**
     * 默认成功响应
     *
     * @return
     */
    public static Result success() {
        return new Result(SUCCESS_CODE, SUCCESS_MESSAGE, DEFAULT_DATA);
    }

    /**
     * 默认失败相应
     *
     * @return
     */
    public static Result fail() {
        return new Result(FAIL_CODE, FAIL_MESSAGE, DEFAULT_DATA);
    }

    /**
     * 传参作为成功/失败
     * 默认成功响应
     *
     * @return
     */
    public static Result isSuccess(Boolean res) {
        return res ? new Result(SUCCESS_CODE, SUCCESS_MESSAGE, DEFAULT_DATA) : new Result(FAIL_CODE, FAIL_MESSAGE, DEFAULT_DATA);
    }


    /**
     * 自定义相应信息，成功响应
     *
     * @return
     */
    public static Result success(Object data) {
        return new Result(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    /**
     * 自定义信息，失败响应
     *
     * @return
     */
    public static Result fail(String msg) {
        return new Result(FAIL_CODE, msg, DEFAULT_DATA);
    }

    /**
     * 自定义状态吗和信息，成功响应
     *
     * @return
     */
    public static Result success(Integer code, String msg) {
        return new Result(code, msg, DEFAULT_DATA);
    }

    /**
     * 自定义状态吗,信息，数据，成功响应
     *
     * @return
     */
    public static Result success(Integer code, String msg, Object data) {
        return new Result(code, msg, data);
    }

    /**
     * 自定义状态码和信息，失败响应
     *
     * @return
     */
    public static Result fail(Integer code, String msg) {
        return new Result(code, msg, DEFAULT_DATA);
    }

    /**
     * 自定义状态码和信息响应
     * 推荐使用
     *
     * @return
     */
    public static Result getInstance(Integer code, String msg) {
        return new Result(code, msg, DEFAULT_DATA);
    }
}
