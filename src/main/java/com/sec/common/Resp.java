package com.sec.common;

import java.io.Serializable;

public class Resp<T> implements Serializable {
    private int code;
    private boolean success;
    private String msg;
    private T data;

    private Resp(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = this.code == 200;
    }

    public static <T> Resp<T> success() {
        return new Resp<T>(Codes.SUCCESS.getCode(), Codes.SUCCESS.getMsg(), null);
    }

    public static <T> Resp<T> successMsg(String msg) {
        return new Resp<T>(Codes.SUCCESS.getCode(), msg, null);
    }

    public static <T> Resp<T> success(T data) {
        return new Resp<T>(Codes.SUCCESS.getCode(), Codes.SUCCESS.getMsg(), data);
    }

    public static <T> Resp<T> success(String msg, T data) {
        return new Resp<T>(Codes.SUCCESS.getCode(), msg, data);
    }

    public static <T> Resp<T> error() {
        return new Resp<T>(Codes.ERROR.getCode(), Codes.ERROR.getMsg(), null);
    }

    public static <T> Resp<T> error(int code, String msg) {
        return new Resp<T>(code, msg, null);
    }

    public static <T> Resp<T> error(Codes codes) {
        return new Resp<T>(codes.getCode(), codes.getMsg(), null);
    }

    public static <T> Resp<T> error(int code, String msg, T eData) {
        return new Resp<T>(code, msg, eData);
    }


    public static <T> Resp<T> error(Codes codes, T eData) {
        return new Resp<T>(codes.getCode(), codes.getMsg(), eData);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
