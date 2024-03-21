package com.vegas.tranquilo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.vegas.tranquilo.R;
import com.vegas.tranquilo.api.RetrofitClient;
import com.vegas.tranquilo.fragments.OrderFragment;
import com.vegas.tranquilo.fragments.ProductsFragment;
import com.vegas.tranquilo.fragments.WorkingHoursFragment;
import com.vegas.tranquilo.models.Gift;
import com.vegas.tranquilo.models.GiftResponse;
import com.vegas.tranquilo.models.MainResponse;
import com.vegas.tranquilo.models.Order;
import com.vegas.tranquilo.models.SimpleResponse;
import com.vegas.tranquilo.services.UpdateLocation;
import com.vegas.tranquilo.utils.CONSTANTS;
import com.vegas.tranquilo.utils.SharedPrefManager;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class Main extends LocaleAwareCompatActivity implements
        OrderFragment.OnOrderFragmentInteractionWithParentActivityListener,
        ProductsFragment.OnProductsFragmentInteractionWithParentActivityListener,
        WorkingHoursFragment.OnWorkingHoursFragmentInteractionWithParentActivityListener {

    private Fragment selectedFragment;
    private FloatingActionButton logoutFab, feedbackFab, giftFab;

    ImageView g_image;

    Dialog mDialog;
    private Handler mHandler1, mHandler2, mHandler3;
    private Runnable mRunnable1, mRunnable2, mRunnable3;
    private TextView noInternet, ct;
    private ImageButton refresh;

    private String APP_VERSION, locale;

    CountDownTimer mCountDownTimer;
    private static final long countTime = 9000;

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    // if single permission it will be [ ActivityResultLauncher<String> ] => with just one String
    // and the ActivityResultCallback will have two params 1- new ActivityResultContracts.RequestPermission(),
    // 2 - new ActivityResultCallback<Boolean>()
    // but if multiple permissions it will be [ ActivityResultLauncher<String[]> ] => with array of Strings
    // and the ActivityResultCallback will have two params 1- new ActivityResultContracts.RequestMultiplePermissions(),
    // 2 - new ActivityResultCallback<Map<String,Boolean>>()

    private ActivityResultLauncher<String[]> mPermissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> resolutionForResult;
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    private double LATITUDE, LONGITUDE;

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    private Intent serviceIntent;
    private boolean isLocationBound;
    private ServiceConnection serviceConnection;
    private UpdateLocation updateLocation;
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    String HINT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        serviceIntent = new Intent(this, UpdateLocation.class);

        mDialog = new Dialog(this);

        preparePermissionLauncher();// must be called only in [ onAttach or onCreate ]
        prepareResolutionForResult();// must be called only in [ onAttach or onCreate ]

        APP_VERSION = getCurrentVersion();

        locale = this.getResources().getConfiguration().locale.getLanguage();

        noInternet = findViewById(R.id.noInternet);
        refresh = findViewById(R.id.refresh);

        logoutFab = findViewById(R.id.logoutFab);
        logoutFab.setOnClickListener(v -> logoutPopup());

        feedbackFab = findViewById(R.id.feedbackFab);
        feedbackFab.setOnClickListener(v -> feedbackPopup());

        giftFab = findViewById(R.id.giftFab);
        g_image = findViewById(R.id.g_image);


        startTask();

//        selectedFragment = OrderFragment.newInstance(myPhone);
//        getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, selectedFragment).commitAllowingStateLoss();

        if (refresh != null) {

            refresh.setOnClickListener(v -> {
                if (HINT != null)
                    switch (HINT) {
                        case "startTask":
                            startTask();
                        case "restartTask":
                            restartTask();
                    }
            });
        }

    }

    private void logoutPopup() {

        mDialog.dismiss();

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(getResources().getString(R.string.want_logout));
        builder1.setCancelable(true);

        builder1.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> logout());


        builder1.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss());

        AlertDialog alert11 = builder1.create();

        alert11.show();

    }

    private void logout() {
        SharedPrefManager.getInstance(this).clearLoginPhone();
        Intent intent = new Intent(getApplicationContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startTask() {
        stopTask();
        removeAllFragments();

        if (!isTimeAutomatic()) {
            enableAutoTimePopup();
        } else if (!isNetworkAvailable()) {
            if (noInternet != null && refresh != null) {
                noInternet.setVisibility(View.VISIBLE);
                noInternet.setText(R.string.noInternet);
                refresh.setVisibility(View.VISIBLE);
                HINT = "startTask";
            }
        } else {

            refresh.setVisibility(View.GONE);
            noInternet.setVisibility(View.GONE);

            checkPermission();

        }

    }

    private void restartTask() {
        stopTask();
        removeAllFragments();

        if (!isTimeAutomatic()) {
            enableAutoTimePopup();
        } else if (!isNetworkAvailable()) {
            if (noInternet != null && refresh != null) {
                noInternet.setVisibility(View.VISIBLE);
                noInternet.setText(R.string.noInternet);
                refresh.setVisibility(View.VISIBLE);
                HINT = "startTask";
            }
        } else {

            refresh.setVisibility(View.GONE);
            noInternet.setVisibility(View.GONE);

            doAllStuff();

        }

    }

    private void stopTask() {
        try {
            if (mCountDownTimer != null)
                mCountDownTimer.cancel();

            if (mHandler1 != null && mRunnable1 != null) {
                mHandler1.removeCallbacks(mRunnable1);
                mHandler1.removeCallbacksAndMessages(null);
                mHandler1 = null;
                mRunnable1 = null;
            }
            if (mHandler2 != null && mRunnable2 != null) {
                mHandler2.removeCallbacks(mRunnable2);
                mHandler2.removeCallbacksAndMessages(null);
                mHandler2 = null;
                mRunnable2 = null;
            }
            if (mHandler3 != null && mRunnable3 != null) {
                mHandler3.removeCallbacks(mRunnable3);
                mHandler3.removeCallbacksAndMessages(null);
                mHandler3 = null;
                mRunnable3 = null;
            }
        } catch (Exception e) {
            Log.e("ThreadUtil:", "Error:" + e);
        }

    }

    private void removeAllFragments() {

        List<Fragment> childFragments = getSupportFragmentManager().getFragments();
        if (!childFragments.isEmpty()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            for (Fragment childFragment : childFragments) {
                if (childFragment != null) {
                    fragmentTransaction.remove(childFragment);
                }
            }
            fragmentTransaction.commitAllowingStateLoss();
        }

    }

    private void startTimer() {
        countDownPopup();
        mCountDownTimer = new CountDownTimer(countTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ct.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }


    private String getCurrentVersion() {
        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;
        try {
            pInfo = pm.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "can't get packageName", Toast.LENGTH_SHORT).show();
        }
        if (pInfo != null) {
            return pInfo.versionName;
        } else {
            Toast.makeText(this, "packageName is null", Toast.LENGTH_SHORT).show();
            return "1000";
        }
    }

    private void feedbackPopup() {

        mDialog.dismiss();
        mDialog.setContentView(R.layout.feedback_popup);
        TextInputEditText fb_ph = mDialog.findViewById(R.id.feedback_phone);
        TextInputEditText fb_txt = mDialog.findViewById(R.id.feedback_text);
        Button send_fb = mDialog.findViewById(R.id.send_feedback);

        //------------
        String fb_ds = SharedPrefManager.getInstance(getApplicationContext()).getFeedbackBeforeDismiss();
        if (fb_ds != null && !"-1".equals(fb_ds))
            fb_txt.setText(fb_ds);
        //------------

        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 100;
            mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            mDialog.setCancelable(true);
            mDialog.show();
        }

        mDialog.setOnDismissListener(dialog -> {
            if (fb_txt.getText() != null && !fb_txt.getText().toString().isEmpty()) {
                String ft = fb_txt.getText().toString().trim().replaceAll("\\s+", " ");
                SharedPrefManager.getInstance(getApplicationContext()).saveFeedbackBeforeDismiss(ft);
            }
        });

        send_fb.setOnClickListener(v -> {
            String fp = Objects.requireNonNull(fb_ph.getText()).toString().trim().replaceAll("\\s+", " ");
            String ft = Objects.requireNonNull(fb_txt.getText()).toString().trim().replaceAll("\\s+", " ");
            if (fp.isEmpty()) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.ph_em), Toast.LENGTH_LONG).show();
            } else {
                if (ft.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_message), Toast.LENGTH_LONG).show();
                } else {
                    waitCheckPopup();
                    new Handler().postDelayed(() -> sendFeedback(fp, ft), 1000);
                }
            }
        });

    }

//    @EverythingIsNonNull
//    private void talkToServer(String phone, Date nowTime, String APP_VERSION) {
//
//        Call<MainResponse> call = RetrofitClient.getInstance().getApi().talkToServer(phone, nowTime, APP_VERSION);
//        call.enqueue(new Callback<MainResponse>() {
//            @Override
//            public void onResponse(Call<MainResponse> call, Response<MainResponse> response) {
//                if (response.isSuccessful()) {
//                    MainResponse mainResponse = response.body();
//                    if (mainResponse != null) {
//                        String s = mainResponse.getMsg();
//                        if (mainResponse.isSuccess()) {
//
//                            stopTask();
//                            mDialog.dismiss();
//                            if (noInternet != null && refresh != null) {
//                                noInternet.setVisibility(View.GONE);
//                                refresh.setVisibility(View.GONE);
//                            }
//
//
//                            if (s.equals("has pending order")) {
//                                Order lastOrder = mainResponse.getLastOrder();
//
//                                selectedFragment = OrderFragment.newInstance(lastOrder);
//                                getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, selectedFragment).commitAllowingStateLoss();
//
//
//                            } else if (s.equals("products loaded")) {
//
//                                selectedFragment = ProductsFragment.newInstance(mainResponse.getAllProducts());
//                                getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, selectedFragment).commitAllowingStateLoss();
//
//
//                            }
//
//                        } else {
//
//                            stopTask();
//                            mDialog.dismiss();
//
//                            if (s.equals("not working hour")) {
//
//                                if (noInternet != null && refresh != null) {
//                                    noInternet.setVisibility(View.GONE);
//                                    refresh.setVisibility(View.GONE);
//                                }
//
//                                selectedFragment = WorkingHoursFragment.newInstance(mainResponse.getStart_time(), mainResponse.getEnd_time());
//                                getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, selectedFragment).commitAllowingStateLoss();
//
//                            } else if (s.equals("outDatedVersion")) {
//
//                                if (noInternet != null && refresh != null) {
//                                    noInternet.setVisibility(View.GONE);
//                                    refresh.setVisibility(View.GONE);
//                                }
//
//                                showUpdatePop(mainResponse.getAnotherMsg());
//                            } else {
//                                if (noInternet != null && refresh != null) {
//                                    noInternet.setVisibility(View.VISIBLE);
//                                    noInternet.setText(s);
//                                    refresh.setVisibility(View.VISIBLE);
//                                }
//                            }
//
//                        }
//                    }
//                } else {
//                    stopTask();
//                    mDialog.dismiss();
//                    if (noInternet != null && refresh != null) {
//                        noInternet.setVisibility(View.VISIBLE);
//                        noInternet.setText(R.string.serverError);
//                        refresh.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MainResponse> call, Throwable th) {
//                stopTask();
//                mDialog.dismiss();
//                if (noInternet != null && refresh != null) {
//                    noInternet.setVisibility(View.VISIBLE);
//                    noInternet.setText(R.string.serverError);
//                    refresh.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//
//
//    }

    @EverythingIsNonNull
    private void checkLastOrder(String order_phone, String login_phone, String APP_VERSION) {

        Call<MainResponse> call = RetrofitClient.getInstance().getApi().checkLastOrder(order_phone, login_phone, getResources().getString(R.string.app_name), APP_VERSION);
        call.enqueue(new Callback<MainResponse>() {
            @Override
            public void onResponse(Call<MainResponse> call, Response<MainResponse> response) {
                if (response.isSuccessful()) {
                    MainResponse mainResponse = response.body();
                    if (mainResponse != null) {
                        String s = mainResponse.getMsg();
                        if (mainResponse.isSuccess()) {

                            stopTask();
                            mDialog.dismiss();
                            if (noInternet != null && refresh != null) {
                                noInternet.setVisibility(View.GONE);
                                refresh.setVisibility(View.GONE);
                            }

                            if (s.equals("has_pending_order")) {
                                Order lastOrder = mainResponse.getLastOrder();

                                selectedFragment = OrderFragment.newInstance(lastOrder);
                                getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, selectedFragment).commitAllowingStateLoss();

                                gentlyCheckMyGift();

                            }

                        } else {

                            stopTask();
                            mDialog.dismiss();

                            switch (s) {
                                case "outDatedVersion":

                                    if (noInternet != null && refresh != null) {
                                        noInternet.setVisibility(View.GONE);
                                        refresh.setVisibility(View.GONE);
                                    }
                                    showUpdatePop(mainResponse.getAnotherMsg());

                                    break;
                                case "has_no_order":
                                    checkPoint();
                                    break;
                                case "blocked":
                                    showBlockPopup();
                                    break;
                                default:
                                    if (noInternet != null && refresh != null) {
                                        noInternet.setVisibility(View.VISIBLE);
                                        noInternet.setText(s);
                                        refresh.setVisibility(View.VISIBLE);
                                        HINT = "restartTask";
                                    }
                                    break;
                            }

                        }
                    }
                } else {
                    stopTask();
                    mDialog.dismiss();
                    if (noInternet != null && refresh != null) {
                        noInternet.setVisibility(View.VISIBLE);
                        noInternet.setText(!isNetworkAvailable() ? R.string.noInternet : R.string.serverError);
                        refresh.setVisibility(View.VISIBLE);
                        HINT = "restartTask";
                    }
                }
            }

            @Override
            public void onFailure(Call<MainResponse> call, Throwable th) {
                stopTask();
                mDialog.dismiss();
                if (noInternet != null && refresh != null) {
                    noInternet.setVisibility(View.VISIBLE);
                    noInternet.setText(!isNetworkAvailable() ? R.string.noInternet : R.string.serverError);
                    refresh.setVisibility(View.VISIBLE);
                    HINT = "restartTask";
                }
            }
        });


    }

    private void checkPoint() {
        startTimer();
        startUpdateLocation();

        mHandler2 = new Handler();
        mRunnable2 = () -> {
            bindService();
            mHandler3 = new Handler();
            mRunnable3 = () -> {
                unbindService();
                getApplicationContext().stopService(serviceIntent);

                // use Latitude & Longitude to check
                if (LATITUDE == 0.0 || LONGITUDE == 0.0) {
                    showGpsNAInArea();
                    return;
                }

                checkPointLiesInArea(LATITUDE, LONGITUDE);
            };

            mHandler3.postDelayed(mRunnable3, 1000);
        };

        mHandler2.postDelayed(mRunnable2, 8000);

    }

    @EverythingIsNonNull
    private void checkPointLiesInArea(double LAT, double LNG) {

//        Call<MainResponse> call = RetrofitClient.getInstance().getApi().checkPointLiesInArea(30.210576, 31.470638, Calendar.getInstance().getTime());
        Call<MainResponse> call = RetrofitClient.getInstance().getApi().checkPointLiesInArea(LAT, LNG, Calendar.getInstance().getTime());
        call.enqueue(new Callback<MainResponse>() {
            @Override
            public void onResponse(Call<MainResponse> call, Response<MainResponse> response) {
                if (response.isSuccessful()) {
                    MainResponse mainResponse = response.body();
                    if (mainResponse != null) {
                        String s = mainResponse.getMsg();
                        if (mainResponse.isSuccess()) {

                            stopTask();
                            mDialog.dismiss();
                            if (noInternet != null && refresh != null) {
                                noInternet.setVisibility(View.GONE);
                                refresh.setVisibility(View.GONE);
                            }

                            if (s.equals("lies_and_now_working")) {
                                //allProducts
                                selectedFragment = ProductsFragment.newInstance(mainResponse.getAllProducts(), mainResponse.getAdmin_handler(), mainResponse.getDelivery_point(), mainResponse.getCode(), mainResponse.getEnd_time());
                                getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, selectedFragment).commitAllowingStateLoss();

                                gentlyCheckMyGift();
                            }

                        } else {

                            stopTask();
                            mDialog.dismiss();

                            if (s.equals("lies_but_not_now_working")) {

                                if (noInternet != null && refresh != null) {
                                    noInternet.setVisibility(View.GONE);
                                    refresh.setVisibility(View.GONE);
                                }

                                selectedFragment = WorkingHoursFragment.newInstance(mainResponse.getStart_time(), mainResponse.getEnd_time());
                                getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, selectedFragment).commitAllowingStateLoss();

                                gentlyCheckMyGift();

                            } else if (s.equals("NoArea")) {
                                if (noInternet != null && refresh != null) {
                                    noInternet.setVisibility(View.GONE);
                                    refresh.setVisibility(View.GONE);
                                }

                                showAreaPopup();

                            } else {
                                if (noInternet != null && refresh != null) {
                                    noInternet.setVisibility(View.VISIBLE);
                                    noInternet.setText(s);
                                    refresh.setVisibility(View.VISIBLE);
                                    HINT = "restartTask";
                                }
                            }

                        }
                    }
                } else {
                    stopTask();
                    mDialog.dismiss();
                    if (noInternet != null && refresh != null) {
                        noInternet.setVisibility(View.VISIBLE);
                        noInternet.setText(!isNetworkAvailable() ? R.string.noInternet : R.string.serverError);
                        refresh.setVisibility(View.VISIBLE);
                        HINT = "restartTask";
                    }
                }
            }

            @Override
            public void onFailure(Call<MainResponse> call, Throwable th) {
                stopTask();
                mDialog.dismiss();
                if (noInternet != null && refresh != null) {
                    noInternet.setVisibility(View.VISIBLE);
                    noInternet.setText(!isNetworkAvailable() ? R.string.noInternet : R.string.serverError);
                    refresh.setVisibility(View.VISIBLE);
                    HINT = "restartTask";
                }
            }
        });

    }

    @EverythingIsNonNull
    private void gentlyCheckMyGift() {

        Call<GiftResponse> call = RetrofitClient.getInstance().getApi().getUserGift(SharedPrefManager.getInstance(getApplicationContext()).getLoginPhone());
        call.enqueue(new Callback<GiftResponse>() {
            @Override
            public void onResponse(Call<GiftResponse> call, Response<GiftResponse> response) {
                if (response.isSuccessful()) {
                    GiftResponse giftResponse = response.body();
                    if (giftResponse != null) {
                        String s = giftResponse.getMsg();
                        if (giftResponse.isSuccess()) {

                            if (s.equals("user_has_gift_waiting_for_wallet_number")) {

                                giftFab.setVisibility(View.VISIBLE);
                                g_image.setScaleX(locale.equals("ar") ? -1 : 1);
                                g_image.setVisibility(View.VISIBLE);

                                giftFab.setOnClickListener(v -> showUserNotAddedWalletNumberPopup(giftResponse.getGift()));

                                showUserNotAddedWalletNumberPopup(giftResponse.getGift());

                            }

                        } else {

                            if (s.equals("user_has_gift_waiting_for_money_transfer")) {

                                giftFab.setVisibility(View.VISIBLE);
                                g_image.setVisibility(View.GONE);

                                giftFab.setOnClickListener(v -> showUserHasAddedWalletNumberPopup(giftResponse.getGift()));


                            } else if (s.equals("user_has_no_gift")) {

                                giftFab.setVisibility(View.VISIBLE);
                                g_image.setVisibility(View.GONE);

                                giftFab.setOnClickListener(v -> Toast.makeText(getApplicationContext(), getResources().getString(R.string.noGift), Toast.LENGTH_LONG).show());


                            } else {
                                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? R.string.noInternet : R.string.serverError, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GiftResponse> call, Throwable th) {
                Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? R.string.noInternet : R.string.serverError, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showUserNotAddedWalletNumberPopup(Gift gift) {

        mDialog.dismiss();
        mDialog.setContentView(R.layout.gift_wallet_number_not_added_popup);

        TextView g_value = mDialog.findViewById(R.id.g_value);
        EditText wallet_number = mDialog.findViewById(R.id.wallet_number);
        ImageButton send_w_num = mDialog.findViewById(R.id.send_w_num);

        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 200;
            mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            mDialog.setCancelable(true);
            mDialog.show();
        }

        g_value.setText(String.valueOf(gift.getAmount()));

        send_w_num.setOnClickListener(v1 -> {

            String w_num = wallet_number.getText().toString().trim().replaceAll("\\s", "");

            if (w_num.isEmpty()) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.ph_em), Toast.LENGTH_LONG).show();
            } else {
                if (w_num.length() < 11) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.ph_ln), Toast.LENGTH_LONG).show();
                } else {
                    if (w_num.startsWith("010") || w_num.startsWith("011") || w_num.startsWith("012") || w_num.startsWith("015")) {

                        mDialog.dismiss();
                        sendWalletNumber(gift.get_id(), w_num);
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.ph_wr), Toast.LENGTH_LONG).show();
                    }
                }
            }

        });

    }

    @EverythingIsNonNull
    private void sendWalletNumber(String giftId, String wallet_number) {

        Call<SimpleResponse> call = RetrofitClient.getInstance().getApi().userGiftAddWalletNumber(giftId, wallet_number);
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()) {
                    SimpleResponse simpleResponse = response.body();
                    if (simpleResponse != null) {
                        String s = simpleResponse.getMsg();
                        if (simpleResponse.isSuccess()) {
                            gentlyCheckMyGift();
                        } else {
                            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? R.string.noInternet : R.string.serverError, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable th) {
                Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? R.string.noInternet : R.string.serverError, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @EverythingIsNonNull
    private void sendFeedback(String phone, String feedback) {

        Call<SimpleResponse> call = RetrofitClient.getInstance().getApi().sendFeedback(phone, feedback);
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()) {
                    SimpleResponse simpleResponse = response.body();
                    if (simpleResponse != null) {
                        String s = simpleResponse.getMsg();
                        if (simpleResponse.isSuccess()) {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                            SharedPrefManager.getInstance(getApplicationContext()).saveFeedbackBeforeDismiss("");
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                            SharedPrefManager.getInstance(getApplicationContext()).saveFeedbackBeforeDismiss(feedback);
                        }
                    }
                } else {
                    mDialog.dismiss();
                    Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? R.string.noInternet : R.string.serverError, Toast.LENGTH_SHORT).show();
                    SharedPrefManager.getInstance(getApplicationContext()).saveFeedbackBeforeDismiss(feedback);

                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable th) {
                mDialog.dismiss();
                Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? R.string.noInternet : R.string.serverError, Toast.LENGTH_SHORT).show();
                SharedPrefManager.getInstance(getApplicationContext()).saveFeedbackBeforeDismiss(feedback);
            }
        });

    }

    private void showUserHasAddedWalletNumberPopup(Gift gift) {

        mDialog.dismiss();
        mDialog.setContentView(R.layout.gift_wallet_number_added_popup);

        TextView g_value = mDialog.findViewById(R.id.g_value);
        EditText wallet_number = mDialog.findViewById(R.id.wallet_number);
        ImageButton send_w_num = mDialog.findViewById(R.id.send_w_num);

        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 200;
            mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            mDialog.setCancelable(true);
            mDialog.show();
        }

        g_value.setText(String.valueOf(gift.getAmount()));
        wallet_number.setText(gift.getWallet_number());
        wallet_number.setEnabled(false);
        wallet_number.setClickable(false);
        send_w_num.setEnabled(false);
        send_w_num.setClickable(false);

    }

    private void showUpdatePop(final String newVersionUrl) {
        mDialog.dismiss();
        mDialog.setContentView(R.layout.update_popup);
        Button doBtn = mDialog.findViewById(R.id.doBtn);
        TextView url = mDialog.findViewById(R.id.url);
        ImageButton copy = mDialog.findViewById(R.id.copy);

        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 250;
            mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            mDialog.setCancelable(false);
            mDialog.show();
        }

        url.setText(newVersionUrl);

        copy.setOnClickListener(v1 -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("url", newVersionUrl);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(getApplicationContext(), "تم نسخ الرابط", Toast.LENGTH_SHORT).show();
        });

        doBtn.setOnClickListener(v2 -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(newVersionUrl)));
            finish();
        });


    }

    private boolean isTimeAutomatic() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
    }

    private void enableAutoTimePopup() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("قم بتشغيل الوقت التلقائى");
        builder1.setCancelable(false);

        builder1.setPositiveButton("ok", (dialog, which) -> {
            startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
            finish();
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void waitCheckPopup() {
        mDialog.dismiss();
        mDialog.setContentView(R.layout.waitcheck);
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 500;
            mDialog.setCancelable(false);
            mDialog.show();
        }

    }

    private void countDownPopup() {
        mDialog.dismiss();
        mDialog.setContentView(R.layout.countdown);
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 500;
            mDialog.setCancelable(false);
            mDialog.show();
        }

        ct = mDialog.findViewById(R.id.ct);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void doAllStuff() {

        waitCheckPopup();
        mHandler1 = new Handler();
        mRunnable1 = () -> {
            String order_phone = SharedPrefManager.getInstance(getApplicationContext()).getOrderPhone();
            String login_phone = SharedPrefManager.getInstance(getApplicationContext()).getLoginPhone();
            if (order_phone == null) {
                checkLastOrder("", login_phone, APP_VERSION);
            } else {
                checkLastOrder(order_phone, login_phone, APP_VERSION);
            }
        };
        mHandler1.postDelayed(mRunnable1, 1000);

    }


    private void showGpsNAInArea() {
        stopTask();
        mDialog.dismiss();
        mDialog.setContentView(CONSTANTS.prepareDialogContentView(
                this,
                R.drawable.round_corners,
                R.drawable.area,
                true,
                R.string.gpsNA,
                R.color.purple_white,
                true,
                R.string.moveForGps,
                R.color.white,
                R.string.gotIt,
                R.color.purple
        ));
        prepareDialogAndSetOnClickListener(mDialog, this::startTask);
    }

    private void showAreaPopup() {

        mDialog.dismiss();
        mDialog.setContentView(CONSTANTS.prepareDialogContentView(
                this,
                R.drawable.round_corners,
                R.drawable.area,
                true,
                R.string.Ar_failed,
                R.color.red,
                true,
                R.string.aNote,
                R.color.white,
                R.string.gotIt,
                R.color.purple
        ));
        prepareDialogAndSetOnClickListener(mDialog, () -> {
            mDialog.dismiss();
            stopTask();
            gentlyCheckMyGift();
        });

    }

    private void showBlockPopup() {
        mDialog.dismiss();
        mDialog.setContentView(CONSTANTS.prepareDialogContentView(
                this,
                R.drawable.round_corners,
                R.drawable.block,
                true,
                R.string.blocked,
                R.color.red,
                true,
                R.string.blocked_note,
                R.color.white,
                R.string.gotIt,
                R.color.black

        ));
        prepareDialogAndSetOnClickListener(mDialog, () -> {
            mDialog.dismiss();
            logout();
        });

    }

    private void prepareDialogAndSetOnClickListener(Dialog dialog, final CONSTANTS.DialogBtnCallback dialogBtnCallback) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().gravity = Gravity.TOP;
            dialog.getWindow().getAttributes().y = 250;
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.setCancelable(false);
            dialog.show();
        }

        Button dialog_btn = dialog.findViewById(R.id.dialog_btn);

        dialog_btn.setOnClickListener(v -> dialogBtnCallback.doTheCallback());

    }

    //##################################### permissions ###########################################

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {// location permission

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //                    ActivityCompat.requestPermissions(this
                //                   ,
//                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                            REQUEST_FINE_LOCATION_PERMISSION);

                if (mPermissionLauncher != null)
                    mPermissionLauncher.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION});

            } else {
                //                    ActivityCompat.requestPermissions(this
                //                   ,
//                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                            REQUEST_FINE_LOCATION_PERMISSION);

                if (mPermissionLauncher != null)
                    mPermissionLauncher.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION});
            }

        } else {
            checkLocationSetting();// there we do our stuff
        }

    }

    private void checkLocationSetting() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());
        builder.setAlwaysShow(true);


        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // do our stuff
            //----------------------------------------------------------------
            doAllStuff();
            //----------------------------------------------------------------
        });


        task.addOnFailureListener(this, e -> {

            if (e instanceof ResolvableApiException) {

                ResolvableApiException resolvable = (ResolvableApiException) e;
//                    try {
//                        resolvable.startResolutionForResult(this
//                       ,
//                                REQUEST_CHECK_SETTINGS);

                resolutionForResult.launch(new IntentSenderRequest.Builder(resolvable.getResolution()).build());

//                    } catch (IntentSender.SendIntentException e1) {
//                        e1.printStackTrace();
//                    }
            }
        });


    }

   /* -------------------  for requesting single permission -----------------
    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result) {
                        Log.e(TAG, "onActivityResult: PERMISSION GRANTED");
                    } else {
                        Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                    }
                }
            });
*/

    private void preparePermissionLauncher() {

        mPermissionLauncher = registerForActivityResult(

                new ActivityResultContracts.RequestMultiplePermissions(), resultMap -> {
                    boolean granted = true;

                    for (Map.Entry<String, Boolean> x : resultMap.entrySet()) {
                        if (!x.getValue()) granted = false;

                        if (granted) {
                            checkLocationSetting();
                        } else {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                                showYouHavePermanentlyDeniedLocationPermission();
                            } else {
                                refresh.setVisibility(View.VISIBLE);
                                HINT = "startTask";
                                noInternet.setVisibility(View.VISIBLE);
                                noInternet.setText(R.string.reacceptPerm);
                                noInternet.setTextColor(getApplicationContext().getResources().getColor(R.color.red, null));
                            }
                        }
                    }
                });

    }

    private void prepareResolutionForResult() {

        resolutionForResult = registerForActivityResult(

                new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        doAllStuff();
                    } else {
                        refresh.setVisibility(View.VISIBLE);
                        HINT = "startTask";
                        noInternet.setVisibility(View.VISIBLE);
                        noInternet.setText(R.string.reopenLoc);
                        noInternet.setTextColor(getApplicationContext().getResources().getColor(R.color.red, null));
                    }
                });

    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSetting();
            } else {
                //Permission denied permanently open Permission setting's page
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this
               ,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showYouHavePermanentlyDeniedLocationPermission();
                }
            }
        }
    }
    */

    private void showYouHavePermanentlyDeniedLocationPermission() {

        mDialog.dismiss();// cause phone popup still displayed

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(locale.equals("ar") ? R.string.ar_go_enable_permission : R.string.en_go_enable_permission);
        builder1.setCancelable(false);

        builder1.setPositiveButton("ok", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

//    private LocationRequest createLocationRequest() {
//
//        LocationRequest mLocationRequest = LocationRequest.create();
//        mLocationRequest.setInterval(5000);
//        mLocationRequest.setFastestInterval(2000);
////        mLocationRequest.setSmallestDisplacement(5);
//        mLocationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
//        return mLocationRequest;
//
//    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
    }

    private void startUpdateLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
        }
    }

    private void bindService() {

        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    UpdateLocation.LocationBinder locationBinder = (UpdateLocation.LocationBinder) service;
                    updateLocation = locationBinder.getLocation();
                    LATITUDE = updateLocation.getLat();
                    LONGITUDE = updateLocation.getLng();
                    isLocationBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    isLocationBound = false;
                }
            };
        }


        this.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (isLocationBound) {
            this.unbindService(serviceConnection);
            isLocationBound = false;
        }
    }

    //################################################################################################

    @Override
    protected void onStart() {
        super.onStart();
        if (!isTimeAutomatic()) {
            enableAutoTimePopup();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isTimeAutomatic()) {
            enableAutoTimePopup();
        }
    }

    @Override
    public void messageFromOrderFragmentToParentActivityToRestartTask() {
        restartTask();
    }

    @Override
    public void messageFromProductsFragmentToParentActivityToRestartTask() {
        restartTask();
    }

    @Override
    public void messageFromWorkingHoursFragmentToParentActivityToRestartTask() {
        restartTask();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternet.setVisibility(View.GONE);
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        stopTask();
    }

}
