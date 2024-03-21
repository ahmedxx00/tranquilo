package com.vegas.tranquilo.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.vegas.tranquilo.R;
import com.vegas.tranquilo.api.RetrofitClient;
import com.vegas.tranquilo.models.SimpleResponse;
import com.vegas.tranquilo.utils.CONSTANTS;
import com.vegas.tranquilo.utils.SharedPrefManager;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;
import com.zeugmasolutions.localehelper.Locales;

import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class Login extends LocaleAwareCompatActivity {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=\\S+$)" +           //no white spaces
                    ".{11}" +               //at least 11 characters
                    "$");
    // ======================================================================================
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=\\S+$)" +               //no white spaces
                    ".{6,15}" +               // from 6-15 characters
                    "$");
    // +++++++++++++++++++#######################################################++++++++++++

    private TextInputLayout u_login_phone, u_login_password;
    Button login_button;
    Animation uptodown, downtoup;
    Dialog mDialog;

    LinearLayout lk;
    EditText checkCodeET;
    ImageButton checkCode;

    RadioGroup country;
    RadioButton ar, en, es, fr;

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    // if single permission it will be [ ActivityResultLauncher<String> ] => with just one String
    // and the ActivityResultCallback will have two params 1- new ActivityResultContracts.RequestPermission(),
    // 2 - new ActivityResultCallback<Boolean>()
    // but if multiple permissions it will be [ ActivityResultLauncher<String[]> ] => with array of Strings
    // and the ActivityResultCallback will have two params 1- new ActivityResultContracts.RequestMultiplePermissions(),
    // 2 - new ActivityResultCallback<Map<String,Boolean>>()

    private ActivityResultLauncher<String[]> notificationPermissionLauncher;
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    boolean hasNotificationPermissionGranted = false;

    String locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        mDialog = new Dialog(this);
        u_login_phone = findViewById(R.id.u_login_phone);
        u_login_password = findViewById(R.id.u_login_password);
        login_button = findViewById(R.id.login_button);
        lk = findViewById(R.id.lk);
        checkCodeET = findViewById(R.id.checkCodeET);
        checkCode = findViewById(R.id.checkCode);
        country = findViewById(R.id.country);
        ar = findViewById(R.id.ar);
        en = findViewById(R.id.en);
        es = findViewById(R.id.es);
        fr = findViewById(R.id.fr);
        uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
        downtoup = AnimationUtils.loadAnimation(this, R.anim.downtoup);
        u_login_phone.startAnimation(uptodown);
        u_login_password.startAnimation(downtoup);


        if (u_login_phone.getEditText() != null)
            u_login_phone.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    validatePhone();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        if (u_login_password.getEditText() != null)
            u_login_password.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    validatePassword();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.shake);

        login_button.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermissionGranted) {
                notificationPermissionLauncher.launch(new String[]{android.Manifest.permission.POST_NOTIFICATIONS});
            } else {
                if (validatePhone() && validatePassword()) {
                    waitPopup();
                    new Handler().postDelayed(this::loginUser, 1000);
                } else if (!validatePhone() && validatePassword()) {
                    u_login_phone.startAnimation(animation);
                } else if (validatePhone() && !validatePassword()) {
                    u_login_password.startAnimation(animation);
                } else {
                    u_login_phone.startAnimation(animation);
                    u_login_password.startAnimation(animation);
                }
            }
        });

        //------- for checkCodeET position move up a little bit when focused ------
        checkCodeET.setOnFocusChangeListener((v, hasFocus) -> lk.setTranslationY(hasFocus ? -50f : 0f));
        //-------------------------------------------------------------------------

        checkCode.setOnClickListener(v -> {
            String str = checkCodeET.getText().toString().replaceAll("\\s", "").trim();
            if (str.length() < 14) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.RegCodeErr), Toast.LENGTH_SHORT).show();
            } else {
                waitPopup();
                new Handler().postDelayed(() -> checkRegistrationValidCode(str), 1000);
            }
        });


        setCountry();
        country.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.ar) {
                updateLocale(Locales.INSTANCE.getArabic());
            } else if (checkedId == R.id.en) {
                updateLocale(Locales.INSTANCE.getEnglish());
            } else if (checkedId == R.id.es) {
                updateLocale(Locales.INSTANCE.getSpanish());
            } else if (checkedId == R.id.fr) {
                updateLocale(Locales.INSTANCE.getFrench());
            } else {
                updateLocale(Locales.INSTANCE.getArabic());
            }

        });


        //================ POST NOTIFICATION PERMISSION =================
        preparePermissionLauncher();// must be called only in [ onAttach or onCreate ]
        locale = this.getResources().getConfiguration().locale.getLanguage();
        //---------------------------------------------------------------
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(new String[]{android.Manifest.permission.POST_NOTIFICATIONS});
        } else {
            hasNotificationPermissionGranted = true;
        }
        //===============================================================

    }

    private void setCountry() {
        String lang = getResources().getConfiguration().locale.getLanguage();
        switch (lang) {
            case "en":
                country.check(R.id.en);
                break;
            case "es":
                country.check(R.id.es);
                break;
            case "fr":
                country.check(R.id.fr);
                break;
            default:
                country.check(R.id.ar);// includes arabic "ar"
        }
    }

    @EverythingIsNonNull
    private void checkRegistrationValidCode(String code) {
        Call<SimpleResponse> call = RetrofitClient.getInstance().getApi().checkIfRegistrationCodeValid(code);
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {

                if (response.isSuccessful()) {

                    SimpleResponse simpleResponse = response.body();
                    if (simpleResponse != null) {
                        String s = simpleResponse.getMsg();
                        if (simpleResponse.isSuccess()) {

                            mDialog.dismiss();
                            showRegistrationPopup(code);

                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "invalid_code".equals(s) ? getResources().getString(R.string.invalid_code) : s, Toast.LENGTH_LONG).show();
                        }
                    }

                } else {
                    mDialog.dismiss();
                    Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? getResources().getString(R.string.noInternet) : getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                }


            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                mDialog.dismiss();
                Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? getResources().getString(R.string.noInternet) : getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showRegistrationPopup(String code) {

        mDialog.dismiss();
        mDialog.setContentView(R.layout.registrationpopup);
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 100;
            mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            mDialog.setCancelable(true);
            mDialog.show();

        }

        final TextInputLayout regPhone = mDialog.findViewById(R.id.regPhone);
        final TextInputLayout regPass = mDialog.findViewById(R.id.regPass);
        Button regBtn = mDialog.findViewById(R.id.regBtn);


        if (regPhone.getEditText() != null)
            regPhone.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    validatePopupPhone(regPhone);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

        if (regPass.getEditText() != null)
            regPass.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    validatePopupPass(regPass);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.shake);

        regBtn.setOnClickListener(v -> {

            if (!validatePopupPhone(regPhone) && !validatePopupPass(regPass)) {
                regPhone.startAnimation(animation);
                regPass.startAnimation(animation);
            } else if (validatePopupPhone(regPhone) && !validatePopupPass(regPass)) {
                regPass.startAnimation(animation);
            } else if (!validatePopupPhone(regPhone) && validatePopupPass(regPass)) {
                regPhone.startAnimation(animation);
            } else {
                String ph = regPhone.getEditText().getText().toString();
                String ps = regPass.getEditText().getText().toString();

                waitPopup();
                new Handler().postDelayed(() -> registerUser(ph, ps, code), 1000);
            }

        });


    }

    @EverythingIsNonNull
    private void registerUser(String ph, String ps, String code) {
        Call<SimpleResponse> call = RetrofitClient.getInstance().getApi().registerUser(ph, ps, code);
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {

                if (response.isSuccessful()) {

                    SimpleResponse simpleResponse = response.body();
                    if (simpleResponse != null) {
                        String s = simpleResponse.getMsg();
                        if (simpleResponse.isSuccess()) {
                            mDialog.dismiss();
                            showOkPopup();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "already_registered".equals(s) ? getResources().getString(R.string.already_registered) : s, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    mDialog.dismiss();
                    Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? getResources().getString(R.string.noInternet) : getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                }


            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                mDialog.dismiss();
                Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? getResources().getString(R.string.noInternet) : getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showOkPopup() {
        mDialog.dismiss();

        mDialog.setContentView(R.layout.reg_ok);

        Button kd = mDialog.findViewById(R.id.kd);

        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 500;
            mDialog.setCancelable(false);
            mDialog.show();
        }

        kd.setOnClickListener(v -> {
            mDialog.dismiss();
            checkCodeET.getText().clear();
            checkCodeET.setText("");
        });

    }

    private boolean validatePopupPhone(TextInputLayout textInputLayout) {
        if (textInputLayout.getEditText() != null) {
            String user_phone = textInputLayout.getEditText().getText().toString().trim();

            if (user_phone.isEmpty()) {
                textInputLayout.setError(getResources().getString(R.string.ph_em));
                return false;
            } else if (!PHONE_PATTERN.matcher(user_phone).matches()) {
                textInputLayout.setError(getResources().getString(R.string.ph_ln));
                return false;

            }

            if ((user_phone.startsWith("010")) || (user_phone.startsWith("011")) || (user_phone.startsWith("012")) || (user_phone.startsWith("015"))) {
                textInputLayout.setError(null);
                return true;

            } else {
                textInputLayout.setError(getResources().getString(R.string.ph_wr));
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean validatePopupPass(TextInputLayout textInputLayout) {

        if (textInputLayout.getEditText() != null) {
            String user_pass = textInputLayout.getEditText().getText().toString().trim();

            if (user_pass.isEmpty()) {
                textInputLayout.setError(getResources().getString(R.string.ps_em));
                return false;
            } else if (!PASSWORD_PATTERN.matcher(user_pass).matches()) {
                textInputLayout.setError(getResources().getString(R.string.ps_ln));
                return false;
            } else {
                textInputLayout.setError(null);
                return true;
            }
        } else {
            return false;
        }

    }


    @EverythingIsNonNull
    private void loginUser() {
        if (u_login_phone.getEditText() != null && u_login_password.getEditText() != null) {
            final String phone = u_login_phone.getEditText().getText().toString().trim();
            String password = u_login_password.getEditText().getText().toString().trim();

            Call<SimpleResponse> call = RetrofitClient.getInstance().getApi().loginUser(phone, password);
            call.enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {

                    if (response.isSuccessful()) {

                        SimpleResponse simpleResponse = response.body();
                        if (simpleResponse != null) {
                            String s = simpleResponse.getMsg();
                            if (simpleResponse.isSuccess()) {

                                mDialog.dismiss();

                                SharedPrefManager.getInstance(Login.this).saveLoginPhone(simpleResponse.getMsg());
                                Intent intent = new Intent(Login.this, Main.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            } else {
                                mDialog.dismiss();
                                switch (s) {
                                    case "wrong_password":
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                                        break;
                                    case "not_exists":
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.notExists), Toast.LENGTH_LONG).show();
                                        break;
                                    case "blocked":
                                        showBlockPopup();
                                        break;
                                    default:
                                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? getResources().getString(R.string.noInternet) : getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                    }


                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    mDialog.dismiss();
                    Toast.makeText(getApplicationContext(), !isNetworkAvailable() ? getResources().getString(R.string.noInternet) : getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // +++++++++++++++++++++++++#####   validate phone and password   ####+++++++++++++++++++
    private boolean validatePhone() {
        if (u_login_phone.getEditText() != null) {
            String user_phone = u_login_phone.getEditText().getText().toString().trim();

            if (user_phone.isEmpty()) {
                u_login_phone.setError(getResources().getString(R.string.ph_em));
                u_login_phone.getEditText().setTextColor(getResources().getColor(R.color.trans_grey, null));


                return false;
            } else if (!PHONE_PATTERN.matcher(user_phone).matches()) {
                u_login_phone.setError(getResources().getString(R.string.ph_ln));
                u_login_phone.getEditText().setTextColor(getResources().getColor(R.color.trans_grey, null));

                return false;

            }

            if ((user_phone.startsWith("010")) || (user_phone.startsWith("011")) || (user_phone.startsWith("012")) || (user_phone.startsWith("015"))) {
                u_login_phone.setError(null);
                u_login_phone.getEditText().setTextColor(getResources().getColor(R.color.grey, null));
                return true;

            } else {
                u_login_phone.setError(getResources().getString(R.string.ph_wr));
                u_login_phone.getEditText().setTextColor(getResources().getColor(R.color.trans_grey, null));

                return false;
            }
        } else {
            return false;
        }
    }

    private boolean validatePassword() {
        if (u_login_password.getEditText() != null) {
            String user_password = u_login_password.getEditText().getText().toString().trim();
            if (user_password.isEmpty()) {
                u_login_password.setError(getResources().getString(R.string.ps_em));
                u_login_password.getEditText().setTextColor(getResources().getColor(R.color.trans_grey, null));
                return false;
            } else if (!PASSWORD_PATTERN.matcher(user_password).matches()) {
                u_login_password.setError(getResources().getString(R.string.ps_ln));
                u_login_password.getEditText().setTextColor(getResources().getColor(R.color.trans_grey, null));
                return false;

            } else {
                u_login_password.setError(null);
                u_login_password.getEditText().setTextColor(getResources().getColor(R.color.grey, null));

                return true;
            }
        } else {
            return false;
        }
    }
    //=========================================================================================

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
        prepareDialogAndSetOnClickListener(mDialog, () -> mDialog.dismiss());

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
        dialog_btn.setOnClickListener(view -> dialogBtnCallback.doTheCallback());
    }

    private void waitPopup() {
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

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    //================ POST NOTIFICATION PERMISSION =================
    private void preparePermissionLauncher() {

        notificationPermissionLauncher = registerForActivityResult(

                new ActivityResultContracts.RequestMultiplePermissions(), resultMap -> {
                    boolean granted = true;

                    for (Map.Entry<String, Boolean> x : resultMap.entrySet()) {
                        if (!x.getValue()) granted = false;

                        hasNotificationPermissionGranted = granted;

                        if (!granted) {

                            if (Build.VERSION.SDK_INT >= 33) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                                    showNotificationPermissionRationale();
                                } else {
                                    showSettingDialog();
                                }
                            }

                        }
                    }
                });

    }

    private void showSettingDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(locale.equals("ar") ? "عزيزى العميل من فضلك قم بالذهاب الى الأذونات أو الترخيص وتفعيل اذن الإشعارات لتطبيق tranquilo" : "Dear user you should enable Notifications permission for tranquilo");
        builder1.setCancelable(false);

        builder1.setPositiveButton("ok", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        builder1.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void showNotificationPermissionRationale() {


        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(locale.equals("ar") ? "من فضلك قم بقبول اذن الإشعارات , التطبيق يحتاج اليه .." : "Please Accept Notifications Permission , the App needs it ...");
        builder1.setCancelable(false);

        builder1.setPositiveButton("ok", (dialog, which) -> {
            if (Build.VERSION.SDK_INT >= 33)
                notificationPermissionLauncher.launch(new String[]{Manifest.permission.POST_NOTIFICATIONS});
        });

        builder1.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }
    //====================================================================

    @Override
    protected void onResume() {
        super.onResume();
        setCountry();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

    }


}
