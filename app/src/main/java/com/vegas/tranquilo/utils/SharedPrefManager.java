package com.vegas.tranquilo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.vegas.tranquilo.R;


public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = R.string.app_name + "_shared_pref";
    @SuppressLint("StaticFieldLeak")
    private static SharedPrefManager mInstance;
    private Context mContext;

    private SharedPrefManager(Context mContext) {
        this.mContext = mContext;
    }

    public static synchronized SharedPrefManager getInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(mContext);
        }
        return mInstance;
    }

    // save phone in shared
    public void saveOrderPhone(String phone) {

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("order_phone", phone);
        editor.apply();

    }

    public void saveLoginPhone(String phone) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("login_phone", phone);
        editor.apply();
    }

    // getPhone from shared
    public String getLoginPhone() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains("login_phone")) {
            return null;
        }
        return sharedPreferences.getString("login_phone", "-1");
    }

    public String getOrderPhone() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains("order_phone")) {
            return null;
        }
        return sharedPreferences.getString("order_phone", "-1");
    }


    public void saveFeedbackBeforeDismiss(String fb) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("fb", fb);
        editor.apply();
    }

    // getPhone from shared
    public String getFeedbackBeforeDismiss() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains("fb")) {
            return null;
        }
        return sharedPreferences.getString("fb", "-1");
    }



    // check if the user is loggedIn or not
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return !"-1".equals(sharedPreferences.getString("login_phone", "-1"));
    }

    // for logout
    public void clearLoginPhone() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove("login_phone");
        editor.apply();
    }


}
