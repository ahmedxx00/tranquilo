package com.vegas.tranquilo.models;

public class SimpleResponse {
    private boolean success;
    private String msg;

    public SimpleResponse(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

}
