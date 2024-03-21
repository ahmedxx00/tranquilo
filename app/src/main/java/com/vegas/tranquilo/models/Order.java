package com.vegas.tranquilo.models;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.TimeZone;


public class Order implements Serializable {

    private String _id,user_phone, code, admin_handler, sign_word, created_at;
    //    private LinkedHashMap<String, Integer> ordered_products;
    private LinkedHashMap<String, LinkedHashMap<String, Integer>> ordered_products;
    private int total;
    private delivery_point delivery_point;
    private boolean canceled_by_user, canceled_by_owner, delivered, meeting_point_changed;

    String login_phone;

    public Order(String user_phone, String code, LinkedHashMap<String, LinkedHashMap<String, Integer>> ordered_products, int total, String admin_handler, delivery_point delivery_point, String sign_word, boolean canceled_by_user, boolean canceled_by_owner, boolean delivered) {
        this.user_phone = user_phone;
        this.code = code;
        this.ordered_products = ordered_products;
        this.total = total;
        this.admin_handler = admin_handler;
        this.delivery_point = delivery_point;
        this.sign_word = sign_word;
        this.canceled_by_user = canceled_by_user;
        this.canceled_by_owner = canceled_by_owner;
        this.delivered = delivered;
    }

    public Order(String login_phone,String user_phone, String code, LinkedHashMap<String, LinkedHashMap<String, Integer>> ordered_products, int total, String admin_handler, delivery_point delivery_point, String sign_word, boolean canceled_by_user, boolean canceled_by_owner, boolean delivered) {
        this.login_phone = login_phone;
        this.user_phone = user_phone;
        this.code = code;
        this.ordered_products = ordered_products;
        this.total = total;
        this.admin_handler = admin_handler;
        this.delivery_point = delivery_point;
        this.sign_word = sign_word;
        this.canceled_by_user = canceled_by_user;
        this.canceled_by_owner = canceled_by_owner;
        this.delivered = delivered;
    }


    public Order(String _id, String user_phone, String code, LinkedHashMap<String, LinkedHashMap<String, Integer>> ordered_products, int total, String admin_handler, delivery_point delivery_point, String sign_word, boolean canceled_by_user, boolean canceled_by_owner, boolean delivered, String created_at) {
        this._id = _id;
        this.user_phone = user_phone;
        this.code = code;
        this.ordered_products = ordered_products;
        this.total = total;
        this.admin_handler = admin_handler;
        this.delivery_point = delivery_point;
        this.sign_word = sign_word;
        this.canceled_by_user = canceled_by_user;
        this.canceled_by_owner = canceled_by_owner;
        this.delivered = delivered;
        this.created_at = created_at;
    }

    public Order(String _id, String user_phone, String code, String admin_handler, String sign_word, String created_at, LinkedHashMap<String, LinkedHashMap<String, Integer>> ordered_products, int total, delivery_point delivery_point, boolean canceled_by_user, boolean canceled_by_owner, boolean delivered, boolean meeting_point_changed) {
        this._id = _id;
        this.user_phone = user_phone;
        this.code = code;
        this.admin_handler = admin_handler;
        this.sign_word = sign_word;
        this.created_at = created_at;
        this.ordered_products = ordered_products;
        this.total = total;
        this.delivery_point = delivery_point;
        this.canceled_by_user = canceled_by_user;
        this.canceled_by_owner = canceled_by_owner;
        this.delivered = delivered;
        this.meeting_point_changed = meeting_point_changed;
    }

    public String get_id() {
        return _id;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public String getCode() {
        return code;
    }

    public LinkedHashMap<String, LinkedHashMap<String, Integer>> getOrdered_products() {
        return ordered_products;
    }

    public int getTotal() {
        return total;
    }

    public String getAdmin_handler() {
        return admin_handler;
    }

    public delivery_point getDelivery_point() {
        return delivery_point;
    }

    public String getSign_word() {
        return sign_word;
    }

    public boolean isCanceled_by_user() {
        return canceled_by_user;
    }

    public boolean isCanceled_by_owner() {
        return canceled_by_owner;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public boolean isMeeting_point_changed() {
        return meeting_point_changed;
    }

    public Date getCreated_at() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return df.parse(created_at);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


}
