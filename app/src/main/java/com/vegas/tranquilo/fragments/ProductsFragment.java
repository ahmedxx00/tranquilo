package com.vegas.tranquilo.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.vegas.tranquilo.R;
import com.vegas.tranquilo.adapters.ProductGroupAdapter;
import com.vegas.tranquilo.api.RetrofitClient;
import com.vegas.tranquilo.models.Order;
import com.vegas.tranquilo.models.ProductGroup;
import com.vegas.tranquilo.models.SelectedRowModel;
import com.vegas.tranquilo.models.SimpleResponse;
import com.vegas.tranquilo.models.delivery_point;
import com.vegas.tranquilo.utils.CONSTANTS;
import com.vegas.tranquilo.utils.SharedPrefManager;

import org.joda.time.DateTimeComparator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ProductsFragment extends Fragment implements ProductGroupAdapter.OnMessageFromAllGroups {

    private RecyclerView groupsRV;
    private List<ProductGroup> productGroups = new ArrayList<>();
    private ProductGroupAdapter productGroupAdapter = null;

    private Button createOrder;
    private TextView prcNum;

//    private String locale;

    private Activity mActivity;

    private OnProductsFragmentInteractionWithParentActivityListener mParentActivityListener;

    private Dialog mDialog;

    private TextView nearest_endTime;
    private TextClock nowTime;
    private SimpleDateFormat onlyTimeFormat;


    private String PHONE, CODE, ADMIN_HANDLER, SIGN_WORD;
    private Date END_TIME;
    private LinkedHashMap<String, Object> SELECTED_HASH = new LinkedHashMap<>();
    private LinkedHashMap<String, LinkedHashMap<String, Integer>> ORDERED_PRODUCTS_TO_SEND = new LinkedHashMap<>();

    private delivery_point DELiVERY_POINT;

    // ======================================================================================
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=\\S+$)" +           //no white spaces
                    ".{11}" +               //at least 11 characters
                    "$");
    // ======================================================================================

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    // if single permission it will be [ ActivityResultLauncher<String> ] => with just one String
    // and the ActivityResultCallback will have two params 1- new ActivityResultContracts.RequestPermission(),
    // 2 - new ActivityResultCallback<Boolean>()
    // but if multiple permissions it will be [ ActivityResultLauncher<String[]> ] => with array of Strings
    // and the ActivityResultCallback will have two params 1- new ActivityResultContracts.RequestMultiplePermissions(),
    // 2 - new ActivityResultCallback<Map<String,Boolean>>()

//    private ActivityResultLauncher<String[]> mPermissionLauncher;
//    private ActivityResultLauncher<IntentSenderRequest> resolutionForResult;
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

//    private double LATITUDE, LONGITUDE;

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
//    private Intent serviceIntent;
//    private boolean isLocationBound;
//    private ServiceConnection serviceConnection;
//    private UpdateLocation updateLocation;
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$


    public ProductsFragment() {
        // Required empty public constructor
    }

    public static ProductsFragment newInstance(List<ProductGroup> allProducts, String admin_handler, delivery_point delivery_point, String code, Date end_time) {

        ProductsFragment f = new ProductsFragment();
        Bundle b = new Bundle();
        b.putSerializable("allProducts", (Serializable) allProducts);
        b.putString("admin_handler", admin_handler);
        b.putSerializable("delivery_point", delivery_point);
        b.putSerializable("end_time", end_time);
        b.putString("code", code);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mActivity = (Activity) context;

//        preparePermissionLauncher();// must be called only in [ onAttach or onCreate ]
//        prepareResolutionForResult();// must be called only in [ onAttach or onCreate ]

        // check if parent Activity implements listener
        if (mActivity instanceof OnProductsFragmentInteractionWithParentActivityListener) {
            mParentActivityListener = (OnProductsFragmentInteractionWithParentActivityListener) mActivity;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChildFragmentInteractionListener");
        }

        //--------------------- to prevent spinner item alignment change on arabic lang -----------
//        Locale locale = new Locale("us"); //US English Locale
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        mActivity.getBaseContext().getResources().updateConfiguration(config,
//                mActivity.getBaseContext().getResources().getDisplayMetrics());
        //---------------------------------------------------------------------------


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.products_fragment, container, false);

        mDialog = new Dialog(mActivity);

        onlyTimeFormat = new SimpleDateFormat(DateFormat.is24HourFormat(mActivity) ? "HH:mm" : "h:mm a", Locale.getDefault());

//        serviceIntent = new Intent(mActivity, UpdateLocation.class);

        nowTime = view.findViewById(R.id.nowTime);
        nearest_endTime = view.findViewById(R.id.nearest_endTime);
        groupsRV = view.findViewById(R.id.groupsRV);
        prcNum = view.findViewById(R.id.prcNum);
        createOrder = view.findViewById(R.id.createOrder);

        if (getArguments() != null) {
            @SuppressWarnings("unchecked")
            List<ProductGroup> pg = (List<ProductGroup>) getArguments().getSerializable("allProducts");
            productGroups = pg;

            ADMIN_HANDLER = getArguments().getString("admin_handler");
            DELiVERY_POINT = (delivery_point) getArguments().getSerializable("delivery_point");
            CODE = getArguments().getString("code");
            END_TIME = (Date) getArguments().getSerializable("end_time");

        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        nearest_endTime.setText(onlyTimeFormat.format(END_TIME));

        prcNum.setText("0");

//        locale = mActivity.getResources().getConfiguration().locale.getLanguage();

        SIGN_WORD = generateRandomSignWord();// generate random sign_word


        //--------------------- Groups recycler view -------------------------

        groupsRV.setLayoutManager(new LinearLayoutManager(mActivity));
        groupsRV.addItemDecoration(new ProductsFragment.ItemDecoration(20));

        //--------------------------------------------------------------------
        productGroupAdapter = new ProductGroupAdapter(mActivity, productGroups, ProductsFragment.this);
        groupsRV.setAdapter(productGroupAdapter);
        productGroupAdapter.notifyDataSetChanged();

        //--------------------- to prevent spinner item alignment change on arabic lang -----------
//        Locale locale = new Locale("us"); //US English Locale
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        mActivity.getBaseContext().getResources().updateConfiguration(config,
//                mActivity.getBaseContext().getResources().getDisplayMetrics());
        //---------------------------------------------------------------------------

        createOrder.setOnClickListener(v -> {
            if (productGroupAdapter != null) {

                SELECTED_HASH.clear();
                SELECTED_HASH = productGroupAdapter.gimmeTheSelectedList();
                if (SELECTED_HASH.size() > 0) {

                    int x = DateTimeComparator.getDateOnlyInstance().compare(Calendar.getInstance().getTime(), END_TIME); // day compare
                    int y = DateTimeComparator.getTimeOnlyInstance().compare(Calendar.getInstance().getTime(), END_TIME); // time compare

                    if (((x == 0) && (y > 0)) || (x > 0)) {// (same day && our now time exceeded) || (our day itself exceeded)
                        showTimeExceededPopup();
                    } else {
                        enterPhonePopup();
                    }

                } else {
                    Toast.makeText(mActivity, getResources().getString(R.string.choose_num_pls), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    //************************************************
    private static String generateRandomSignWord() {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(CONSTANTS.SIGN_WORD_SIZE);
        for (int i = 0; i < CONSTANTS.SIGN_WORD_SIZE; ++i)
            sb.append(CONSTANTS.SIGN_WORD_ALLOWED_CHARACTERS.charAt(random.nextInt(CONSTANTS.SIGN_WORD_ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    //----------------------------------------------------


    private void enterPhonePopup() {

        mDialog.dismiss();
        mDialog.setContentView(R.layout.enterphonepopup);
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 250;
            mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            mDialog.setCancelable(true);
            mDialog.show();
        }

        final TextInputLayout enterPh = mDialog.findViewById(R.id.enterPh);
        Button proceedOrder = mDialog.findViewById(R.id.proceedOrder);

        if (enterPh.getEditText() != null)
            enterPh.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    validatePopupPhone(enterPh);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.shake);

        proceedOrder.setOnClickListener(v -> {

            if (validatePopupPhone(enterPh)) {
                PHONE = enterPh.getEditText().getText().toString();
//                checkPermission();// do all stuff there
                doAllStuff();
            } else {
                enterPh.startAnimation(animation);
            }
        });

    }

//    private void doAllStuff() {
//
//        waitCheckPopup();
//        startUpdateLocation();
//        new Handler().postDelayed(() -> {
//            bindService();
//            new Handler().postDelayed(() -> {
//
//                unbindService();
//                mActivity.stopService(serviceIntent);
//
//                // use Latitude & Longitude to check
//                if (LATITUDE == 0.0 || LONGITUDE == 0.0) {
//                    showGpsNAInArea();
//                    return;
//                }
//
//                sendOrder(prepareOrder(LATITUDE, LONGITUDE));
//
//            }, 1000);
//        }, 8000);
//
//    }

    private void doAllStuff() {
        waitCheckPopup();
        new Handler().postDelayed(() -> sendOrder(prepareOrder()), 2000);
    }

    private Order prepareOrder() {

        ORDERED_PRODUCTS_TO_SEND.clear();

        for (HashMap.Entry me : SELECTED_HASH.entrySet()) {
            String groupName = (String) me.getKey();
            @SuppressWarnings("unchecked")
            List<SelectedRowModel> selectedRows = (List<SelectedRowModel>) me.getValue();

            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Integer> oneRow = new LinkedHashMap();

            for (SelectedRowModel srm : selectedRows) {

                oneRow.put(srm.getRowTitle(), srm.getRowSelectedNum());// fill the hash

            }

            ORDERED_PRODUCTS_TO_SEND.put(groupName, oneRow);

        }

        return new Order(SharedPrefManager.getInstance(mActivity).getLoginPhone(), PHONE, CODE, ORDERED_PRODUCTS_TO_SEND, Integer.parseInt(prcNum.getText().toString()), ADMIN_HANDLER, DELiVERY_POINT, SIGN_WORD, false, false, false);

    }

    @EverythingIsNonNull
    private void sendOrder(Order sentOrder) {

        final JsonObject jsonObject = new GsonBuilder().create().toJsonTree(sentOrder).getAsJsonObject();

        Call<SimpleResponse> call = RetrofitClient.getInstance().getApi().sendOrder(jsonObject);
        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()) {
                    SimpleResponse simpleResponse = response.body();
                    if (simpleResponse != null) {
                        String s = simpleResponse.getMsg();
                        if (simpleResponse.isSuccess()) {
                            SharedPrefManager.getInstance(mActivity).saveOrderPhone(PHONE);
                            showSuccessPopup();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(mActivity, s, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    mDialog.dismiss();
                    Toast.makeText(mActivity, !isNetworkAvailable() ? R.string.noInternet : R.string.serverError, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                mDialog.dismiss();
                Toast.makeText(mActivity, !isNetworkAvailable() ? R.string.noInternet : R.string.serverError, Toast.LENGTH_SHORT).show();
            }
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


    private void waitCheckPopup() {
        mDialog.dismiss();
        mDialog.setContentView(R.layout.waitcheck);

        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().getAttributes().gravity = Gravity.TOP;
            mDialog.getWindow().getAttributes().y = 500;
//            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setCancelable(false);
            mDialog.show();
        }
    }


    @Override
    public void totalFromAllGroups(int supremeTotal) {
        prcNum.setText(String.valueOf(supremeTotal));// setting total
    }

    //------------------------------------------------------------------------------
    private static class ItemDecoration extends RecyclerView.ItemDecoration {
        private int verticalSpaceHeight;

        ItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.bottom = verticalSpaceHeight;
        }
    }
    //------------------------------------------------------------------------------

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


    //------------- communication interface -------------
    public interface OnProductsFragmentInteractionWithParentActivityListener {
        void messageFromProductsFragmentToParentActivityToRestartTask();
    }


    //-----------------------------------------------

    //##################################### permissions ###########################################

//    private void checkPermission() {
//
//        if (ContextCompat.checkSelfPermission(mActivity,
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {// location permission
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//                //                    ActivityCompat.requestPermissions(mActivity,
////                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
////                            REQUEST_FINE_LOCATION_PERMISSION);
//
//                if (mPermissionLauncher != null)
//                    mPermissionLauncher.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION});
//
//            } else {
//                //                    ActivityCompat.requestPermissions(mActivity,
////                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
////                            REQUEST_FINE_LOCATION_PERMISSION);
//
//                if (mPermissionLauncher != null)
//                    mPermissionLauncher.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION});
//            }
//
//        } else {
//            checkLocationSetting();// there we do our stuff
//        }
//
//    }
//
//    private void checkLocationSetting() {
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                .addLocationRequest(createLocationRequest());
//        builder.setAlwaysShow(true);
//
//
//        SettingsClient client = LocationServices.getSettingsClient(mActivity);
//        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
//
//        task.addOnSuccessListener(mActivity, locationSettingsResponse -> {
//            // do our stuff
//            //----------------------------------------------------------------
//            doAllStuff();
//            //----------------------------------------------------------------
//        });
//
//
//        task.addOnFailureListener(mActivity, e -> {
//
//            if (e instanceof ResolvableApiException) {
//
//                ResolvableApiException resolvable = (ResolvableApiException) e;
////                    try {
////                        resolvable.startResolutionForResult(mActivity,
////                                REQUEST_CHECK_SETTINGS);
//
//                resolutionForResult.launch(new IntentSenderRequest.Builder(resolvable.getResolution()).build());
//
////                    } catch (IntentSender.SendIntentException e1) {
////                        e1.printStackTrace();
////                    }
//            }
//        });
//
//
//    }
//
//   /* -------------------  for requesting single permission -----------------
//    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
//            new ActivityResultContracts.RequestPermission(),
//            new ActivityResultCallback<Boolean>() {
//                @Override
//                public void onActivityResult(Boolean result) {
//                    if(result) {
//                        Log.e(TAG, "onActivityResult: PERMISSION GRANTED");
//                    } else {
//                        Log.e(TAG, "onActivityResult: PERMISSION DENIED");
//                    }
//                }
//            });
//*/
//
//    private void preparePermissionLauncher() {
//
//        mPermissionLauncher = registerForActivityResult(
//
//                new ActivityResultContracts.RequestMultiplePermissions(), resultMap -> {
//                    boolean granted = true;
//
//                    for (Map.Entry<String, Boolean> x : resultMap.entrySet()) {
//                        if (!x.getValue()) granted = false;
//
//                        if (granted) {
//                            checkLocationSetting();
//                        } else {
//                            if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
//                                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//                                showYouHavePermanentlyDeniedLocationPermission();
//                            }
//                        }
//                    }
//                });
//
//    }
//
//    private void prepareResolutionForResult() {
//
//        resolutionForResult = registerForActivityResult(
//
//                new ActivityResultContracts.StartIntentSenderForResult(), result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK)
//                        doAllStuff();
//                });
//
//    }
//
//    /*
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {// If request is cancelled, the result arrays are empty.
//            if (grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                checkLocationSetting();
//            } else {
//                //Permission denied permanently open Permission setting's page
//                if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
//                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    showYouHavePermanentlyDeniedLocationPermission();
//                }
//            }
//        }
//    }
//    */
//
//    private void showYouHavePermanentlyDeniedLocationPermission() {
//
//        mDialog.dismiss();// cause phone popup still displayed
//
//        AlertDialog.Builder builder1 = new AlertDialog.Builder(mActivity);
//        builder1.setMessage(locale.equals("ar") ? getResources().getString(R.string.ar_go_enable_permission) : getResources().getString(R.string.en_go_enable_permission));
//        builder1.setCancelable(false);
//
//        builder1.setPositiveButton("ok", (dialog, which) -> {
//            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//            Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
//            intent.setData(uri);
//            startActivity(intent);
//        });
//
//        AlertDialog alert11 = builder1.create();
//        alert11.show();
//
//    }
//
//    private LocationRequest createLocationRequest() {
//
//        LocationRequest mLocationRequest = LocationRequest.create();
//        mLocationRequest.setInterval(5000);
//        mLocationRequest.setFastestInterval(2000);
////        mLocationRequest.setSmallestDisplacement(5);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        return mLocationRequest;
//    }
//
//    //-------------------------------------
//    private void startUpdateLocation() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mActivity.startForegroundService(serviceIntent);
//        } else {
//            mActivity.startService(serviceIntent);
//        }
//    }
//
//    private void bindService() {
//
//        if (serviceConnection == null) {
//            serviceConnection = new ServiceConnection() {
//                @Override
//                public void onServiceConnected(ComponentName name, IBinder service) {
//                    UpdateLocation.LocationBinder locationBinder = (UpdateLocation.LocationBinder) service;
//                    updateLocation = locationBinder.getLocation();
//                    LATITUDE = updateLocation.getLat();
//                    LONGITUDE = updateLocation.getLng();
//                    isLocationBound = true;
//                }
//
//                @Override
//                public void onServiceDisconnected(ComponentName name) {
//                    isLocationBound = false;
//                }
//            };
//        }
//
//
//        mActivity.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    private void unbindService() {
//        if (isLocationBound) {
//            mActivity.unbindService(serviceConnection);
//            isLocationBound = false;
//        }
//    }
//    //-------------------------------------
//
//    private void showGpsNAInArea() {
//
//        mDialog.dismiss();
//        mDialog.setContentView(CONSTANTS.prepareDialogContentView(
//                mActivity,
//                R.drawable.round_corners,
//                R.drawable.area,
//                true,
//                R.string.gpsNA,
//                R.color.purple_white,
//                true,
//                R.string.moveForGps,
//                R.color.white,
//                R.string.gotIt,
//                R.color.purple
//        ));
//        prepareDialogAndSetOnClickListener(mDialog, () -> mDialog.dismiss());
//    }


    //################################################################################################

    private void showSuccessPopup() {

        mDialog.dismiss();
        mDialog.setContentView(CONSTANTS.prepareDialogContentView(
                mActivity,
                R.drawable.round_corners,
                R.drawable.smile,
                true,
                R.string.success_order,
                R.color.green,
                true,
                R.string.suc_note,
                R.color.white,
                R.string.ok,
                R.color.purple
        ));
        prepareDialogAndSetOnClickListener(mDialog, () -> {
            mDialog.dismiss();
            mParentActivityListener.messageFromProductsFragmentToParentActivityToRestartTask();
        });

    }

    private void showTimeExceededPopup() {

        mDialog.dismiss();
        mDialog.setContentView(CONSTANTS.prepareDialogContentView(
                mActivity,
                R.drawable.btn_background,
                R.drawable.timeout,
                true,
                R.string.time_out,
                R.color.red,
                true,
                R.string.time_out_note,
                R.color.white,
                R.string.ok,
                R.color.purple
        ));
        prepareDialogAndSetOnClickListener(mDialog, () -> {
            mDialog.dismiss();
            mParentActivityListener.messageFromProductsFragmentToParentActivityToRestartTask();
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mParentActivityListener != null) {
            mParentActivityListener = null;
        }
    }


}



