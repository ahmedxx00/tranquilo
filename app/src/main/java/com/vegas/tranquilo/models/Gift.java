package com.vegas.tranquilo.models;

import java.io.Serializable;

public class Gift implements Serializable {

    private String _id,phone,wallet_number;
    private int amount;
    private boolean user_added_wallet_number,user_got_his_money;

    public Gift(String _id, String phone, String wallet_number, int amount, boolean user_added_wallet_number, boolean user_got_his_money) {
        this._id = _id;
        this.phone = phone;
        this.wallet_number = wallet_number;
        this.amount = amount;
        this.user_added_wallet_number = user_added_wallet_number;
        this.user_got_his_money = user_got_his_money;
    }

    public String get_id() {
        return _id;
    }

    public String getPhone() {
        return phone;
    }

    public String getWallet_number() {
        return wallet_number;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isUser_added_wallet_number() {
        return user_added_wallet_number;
    }

    public boolean isUser_got_his_money() {
        return user_got_his_money;
    }

}
