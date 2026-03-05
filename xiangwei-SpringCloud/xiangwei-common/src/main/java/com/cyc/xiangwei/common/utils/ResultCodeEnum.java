package com.cyc.xiangwei.common.utils;

public enum ResultCodeEnum {
    SUCCESS("200", "操作成功"),
    ERROR("500", "系统内部异常"),

    PARAM_ERROR("400", "参数校验失败或不完整"),
    UNAUTHORIZED("401", "未登录或Token已失效，请重新登录"),
    FORBIDDEN("403", "权限不足，拒绝访问"),
    NOT_FOUND("404", "请求的资源不存在");
    private final String code;
    private final String message;

    ResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
