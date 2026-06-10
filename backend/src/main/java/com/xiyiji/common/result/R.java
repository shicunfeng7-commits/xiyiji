package com.xiyiji.common.result;

import lombok.Data;

@Data
public class R<T> {
    private int code;
    private String msg;
    private T data;

    private R() {}

    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> error(int code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public static <T> R<T> error(String msg) {
        return error(500, msg);
    }
}