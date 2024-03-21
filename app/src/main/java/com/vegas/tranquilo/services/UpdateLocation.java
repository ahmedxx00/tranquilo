package com.vegas.tranquilo.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.vegas.tranquilo.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.vegas.tranquilo.App.UPDATE_LOCATION_CHANNEL_ID;


public class UpdateLocation extends Service {

    private FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback = null;
    public Notification notification_1;


    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    //    //#########################
    public class LocationBinder extends Binder {
        public UpdateLocation getLocation() {
            return UpdateLocation.this;
        }
    }

    private IBinder mBinder = new LocationBinder();
    //#########################

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {

            notification_1 = new NotificationCompat.Builder(this, UPDATE_LOCATION_CHANNEL_ID)
                    .setContentTitle("tranquilo Update")
                    .setContentText("This Is tranquilo Update")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            startForeground(1, notification_1);

        }


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {

                Log.d("Update Location", "got location result");

                Location location = locationResult.getLastLocation();

                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                }
                Log.i("lat", String.valueOf(lat));
                Log.i("lng", String.valueOf(lng));
            }
        };


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("Update Location", "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;

    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Update Location", "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d("Update Location", "getLocation: getting location information.");

        mFusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback,
                Objects.requireNonNull(Looper.myLooper())); // Looper.myLooper tells this to repeat forever until thread is destroyed


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        Log.i("ServiceDemo", "destroyed");


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("ServiceDemo", "Bind started");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("ServiceDemo", "UnBind started");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i("ServiceDemo", "ReBind started");

    }


//    protected LocationRequest createLocationRequest() {
//        LocationRequest mLocationRequest = LocationRequest.create();
//        mLocationRequest.setInterval(5000);
//        mLocationRequest.setFastestInterval(2000);
////        mLocationRequest.setSmallestDisplacement(5);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        return mLocationRequest;
//    }


    protected LocationRequest createLocationRequest() {
        return new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


}
