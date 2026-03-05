package com.cyc.xiangwei.common.utils;

public class Result<T> {
    private String code;
    private String msg;
    private T data;
    public Result() {}
    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public Result(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCodeEnum.SUCCESS.getCode());
        result.setMsg(ResultCodeEnum.SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCodeEnum.SUCCESS.getCode());
        result.setMsg(ResultCodeEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    // 🚀 新增：直接通过枚举返回错误
    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        result.setCode(resultCodeEnum.getCode());
        result.setMsg(resultCodeEnum.getMessage());
        return result;
    }

    // 🚀 新增：通过枚举返回错误，但允许覆盖默认的错误信息（用于参数校验把具体字段错报出来）
    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum, String customMessage) {
        Result<T> result = new Result<>();
        result.setCode(resultCodeEnum.getCode());
        result.setMsg(customMessage);
        return result;
    }

    // 保留原有的方法，兼容旧代码，避免一次性报错太多
    public static <T> Result<T> error(String code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}