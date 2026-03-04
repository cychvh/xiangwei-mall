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
    public static Result success(){
        Result result = new Result();
        result.setCode("200");
        result.setMsg("success");
        return result;
    }
    //如果想要static方法使用泛型，需要在static<T>
    public static<T> Result<T> success(T data){
        return new Result("200","success",data);

    }
    //因为失败的可能有很多种，比如404，500 所以需要外部传入
    public static Result error(String code, String msg){
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
    //如果想要static方法使用泛型，需要在static<T>
    public static<T> Result<T> error(String code,String msg,T data){
        return new Result(code,msg,data);

    }

}