package com.vegas.tranquilo.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.vegas.tranquilo.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class CONSTANTS {


    public static final String BASE_URL = "http://84.32.188.211/";
//    public static final String BASE_URL = "http://192.168.148.123:3000/";

    public static final String BASIC_AUTH_USERNAME = "vegasnerva";
    public static final String BASIC_AUTH_PASS = "@l#i$f%e";


    public static final String SIGN_WORD_ALLOWED_CHARACTERS = "123456789abchwmxz";
    public static final int SIGN_WORD_SIZE = 3;

    public interface DialogBtnCallback {
        void doTheCallback();
    }

    public static View prepareDialogContentView(Context context,
                                                int dialogBackground,
                                                int photo,
                                                boolean isPhotoVisible,
                                                int mainMsg,
                                                int mainMsgTextColor,
                                                boolean isMainMsgVisible,
                                                int secMsg,
                                                int secMsgTextColor,
                                                int btnText,
                                                int btnTextColor

    ) {

        View rootView = LayoutInflater.from(context).inflate(R.layout.popupdialog, null);

        ConstraintLayout wrp = rootView.findViewById(R.id.wrapper);
        ImageView pht = rootView.findViewById(R.id.photo);
        TextView mnMsg = rootView.findViewById(R.id.mainMsg);
        TextView scMsg = rootView.findViewById(R.id.secMsg);
        Button dialog_btn = rootView.findViewById(R.id.dialog_btn);

        wrp.setBackgroundResource(dialogBackground);

        pht.setVisibility(isPhotoVisible ? View.VISIBLE : View.GONE);
        pht.setImageResource(photo);

        mnMsg.setVisibility(isMainMsgVisible ? View.VISIBLE : View.GONE);
        mnMsg.setText(mainMsg);
        mnMsg.setTextColor(context.getResources().getColor(mainMsgTextColor));

        scMsg.setText(secMsg);
        scMsg.setTextColor(context.getResources().getColor(secMsgTextColor));

        dialog_btn.setText(btnText);
        dialog_btn.setTextColor(context.getResources().getColor(btnTextColor));

        return rootView;

    }

    public static Date getUTCDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        return calendar.getTime();
    }


}
