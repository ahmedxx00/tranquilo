package com.vegas.tranquilo.models;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainResponse implements Serializable {

    private boolean success;
    private String msg, anotherMsg, admin_handler, code;
    private Order lastOrder;
    private List<ProductGroup> allProducts;
    private String start_time, end_time;
    private delivery_point delivery_point;

    public MainResponse(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public MainResponse(boolean success, String msg, String anotherMsg) {
        this.success = success;
        this.msg = msg;
        this.anotherMsg = anotherMsg;
    }

    public MainResponse(boolean success, String msg, Order lastOrder) {
        this.success = success;
        this.msg = msg;
        this.lastOrder = lastOrder;
    }

    public MainResponse(boolean success, String msg, List<ProductGroup> allProducts, String admin_handler, delivery_point delivery_point, String code, String end_time) {
        this.success = success;
        this.msg = msg;
        this.allProducts = allProducts;
        this.admin_handler = admin_handler;
        this.delivery_point = delivery_point;
        this.code = code;
        this.end_time = end_time;
    }

    public MainResponse(boolean success, String msg, String start_time, String end_time) {
        this.success = success;
        this.msg = msg;
        this.start_time = start_time;
        this.end_time = end_time;
    }


    public MainResponse(boolean success, String msg, Order lastOrder, List<ProductGroup> allProducts, String start_time, String end_time) {
        this.success = success;
        this.msg = msg;
        this.lastOrder = lastOrder;
        this.allProducts = allProducts;
        this.start_time = start_time;
        this.end_time = end_time;
    }


    public String getAdmin_handler() {
        return admin_handler;
    }

    public delivery_point getDelivery_point() {
        return delivery_point;
    }

    public String getCode() {
        return code;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public String getAnotherMsg() {
        return anotherMsg;
    }

    public Order getLastOrder() {
        return lastOrder;
    }

    public List<ProductGroup> getAllProducts() {
        return allProducts;
    }

    public Date getEnd_time() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return df.parse(end_time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Date getStart_time() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return df.parse(start_time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


}
