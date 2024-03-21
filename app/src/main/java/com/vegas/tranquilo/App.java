package com.vegas.tranquilo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.zeugmasolutions.localehelper.LocaleAwareApplication;


public class App extends LocaleAwareApplication {

    public static final String UPDATE_LOCATION_CHANNEL_ID = "tranquilo";

    @Override
    public void onCreate() {
        super.onCreate();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel_1 = new NotificationChannel(
                    UPDATE_LOCATION_CHANNEL_ID,
                    "tranquilo",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel_1);

        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }


}
