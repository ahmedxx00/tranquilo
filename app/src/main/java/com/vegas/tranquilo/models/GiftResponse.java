package com.vegas.tranquilo.models;

import java.io.Serializable;

public class GiftResponse implements Serializable {

    private boolean success;
    private String msg;
    private Gift gift;

    public GiftResponse(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public GiftResponse(boolean success, String msg, Gift gift) {
        this.success = success;
        this.msg = msg;
        this.gift = gift;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public Gift getGift() {
        return gift;
    }
}
