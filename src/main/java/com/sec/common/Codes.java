package com.sec.common;

public enum Codes {
    SUCCESS(200, "成功"),
    ERROR(500, "失败"),

    FLOW_LIMIT(4400, "请求过于频繁, 请稍后再试"),

    NO_LOGIN(4401, "未登录"),

    SECKIIL_ERROR(4422, "秒杀错误"),

    ORDER_ERROR(4433, "订单出错"),
    PAY_ERROR(4444, "支付出错"),
    REDIS_ERROR(4455, "redis错误"),
    ILLEGAL_ARGUMENT(4480, "非法参数"),

    IO_ERROR(5480, "IO异常"),
    SERVER_ERR(5500, "服务器异常"),
    ;

    private int code;
    private String msg;

    Codes(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }
}
