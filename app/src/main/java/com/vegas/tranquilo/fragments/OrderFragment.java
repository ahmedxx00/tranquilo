package com.vegas.tranquilo.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vegas.tranquilo.R;
import com.vegas.tranquilo.api.RetrofitClient;
import com.vegas.tranquilo.models.Order;
import com.vegas.tranquilo.models.SimpleResponse;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class OrderFragment extends Fragment {

    private Activity mActivity;
    private TextView oD, oC, RRC, sn_word, admin_handler;
    private Button pressToGo, cancelOrd;
    private LinearLayout pWrapper, meeting_point_changed;

    private SimpleDateFormat tf, df;

    private SwipeRefreshLayout swipeRefresh;


    private String _id, SIGN_WORD, ADMIN_HANDLER;
    private int TOTAL;
    private double POINT_LAT, POINT_LNG;
    private boolean IS_MEETING_POINT_CHANGED;

    private Order LAST_ORDER;

    private OnOrderFragmentInteractionWithParentActivityListener mParentActivityListener;

    private Dialog mDialog;

    public OrderFragment() {
        // Required empty public constructor
    }


    public static OrderFragment newInstance(Order lastOrder) {

        OrderFragment f = new OrderFragment();
        Bundle b = new Bundle();
        b.putSerializable("lastOrder", lastOrder);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        // check if parent Activity implements listener
        if (mActivity instanceof OnOrderFragmentInteractionWithParentActivityListener) {
            mParentActivityListener = (OnOrderFragmentInteractionWithParentActivityListener) mActivity;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChildFragmentInteractionListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.order_fragment, container, false);

        oD = view.findViewById(R.id.oD);
        oC = view.findViewById(R.id.oC);
        pWrapper = view.findViewById(R.id.pWrapper);
        sn_word = view.findViewById(R.id.sn_word);
        RRC = view.findViewById(R.id.RRC);
        pressToGo = view.findViewById(R.id.pressToGo);
        meeting_point_changed = view.findViewById(R.id.meeting_point_changed);
        admin_handler = view.findViewById(R.id.admin_handler);
        cancelOrd = view.findViewById(R.id.cancelOrd);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        tf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        df = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());


        if (getArguments() != null) {
            LAST_ORDER = (Order) getArguments().getSerializable("lastOrder");
            if (LAST_ORDER != null) {
                _id = LAST_ORDER.get_id();
                TOTAL = LAST_ORDER.getTotal();
                POINT_LAT = LAST_ORDER.getDelivery_point().getLat();
                POINT_LNG = LAST_ORDER.getDelivery_point().getLng();
                SIGN_WORD = LAST_ORDER.getSign_word();
                ADMIN_HANDLER = LAST_ORDER.getAdmin_handler();
                IS_MEETING_POINT_CHANGED = LAST_ORDER.isMeeting_point_changed();

            }

        }

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            sn_word.setText(SIGN_WORD);
            RRC.setText(String.valueOf(TOTAL));
            admin_handler.setText(ADMIN_HANDLER);
            meeting_point_changed.setVisibility(IS_MEETING_POINT_CHANGED ? View.VISIBLE : View.GONE);
        }

        mDialog = new Dialog(mActivity);

        cancelOrd.setOnClickListener(v -> {
            cancelOrderPopup();
        });

        pressToGo.setOnClickListener(view1 -> gotoPoint(POINT_LAT, POINT_LNG));
        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(false);
            mParentActivityListener.messageFromOrderFragmentToParentActivityToRestartTask();
        });

        oC.setText(tf.format(LAST_ORDER.getCreated_at()));
        oD.setText(df.format(LAST_ORDER.getCreated_at()));

        inflate(LAST_ORDER.getOrdered_products());

    }


    private void cancelOrderPopup() {

        mDialog.dismiss();

        AlertDialog.Builder builder1 = new AlertDialog.Builder(mActivity);
        builder1.setMessage(getResources().getString(R.string.want_cancel_ord));
        builder1.setCancelable(true);

        builder1.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
            waitCheckPopup();
            new Handler().postDelayed(() -> canceledByUser(_id), 1000);
        });


        builder1.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss());

        AlertDialog alert11 = builder1.create();

        alert11.show();

    }

    private void gotoPoint(double lat, double lng) {
        Uri navigationIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);//creating intent with latlng
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");// the maps app

        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException ex) {// if maps app is not installed
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
                startActivity(unrestrictedIntent);// open with any available app or even the browser
            } catch (ActivityNotFoundException innerEx) {
                Toast.makeText(mActivity, "Please install a maps application", Toast.LENGTH_LONG).show();
            }
        } finally {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
                startActivity(unrestrictedIntent);// open with any available app or even the browser
            } catch (ActivityNotFoundException innerEx) {
                Toast.makeText(mActivity, "Please install a maps application", Toast.LENGTH_LONG).show();
            }
        }

    }

    @EverythingIsNonNull
    private void canceledByUser(String _id) {

        Call<SimpleResponse> call = RetrofitClient.getInstance().getApi().canceledByUser(_id);

        call.enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()) {
                    SimpleResponse simpleResponse = response.body();
                    if (simpleResponse != null) {
                        String s = simpleResponse.getMsg();
                        if (simpleResponse.isSuccess()) {
                            mDialog.dismiss();
                            mParentActivityListener.messageFromOrderFragmentToParentActivityToRestartTask();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(mActivity, s, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    mDialog.dismiss();
                    Toast.makeText(mActivity, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                mDialog.dismiss();
                Toast.makeText(mActivity, getResources().getString(R.string.serverError), Toast.LENGTH_SHORT).show();
            }
        });
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

//----------------------------------------------------
//    public interface OnGoToPointFragmentInteractionWithParentListener {
//        void messageFromGoToPointFragmentToParentToInflateDefault();
//    }

//    private void inf(HashMap<String, Integer> ordered_products) {
//
//        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        if (inflater != null) {
//            for (HashMap.Entry me : ordered_products.entrySet()) {
//
//                View childView = inflater.inflate(R.layout.simple_inflated, pWrapper, false);
//                ((TextView) childView.findViewById(R.id.Name)).setText((String) me.getKey());
//                ((TextView) childView.findViewById(R.id.nM)).setText(String.valueOf(me.getValue()));
//
//                pWrapper.addView(childView);
//            }
//        }
//
//    }

    private void inflate(LinkedHashMap<String, LinkedHashMap<String, Integer>> ordered_products) {
        for (LinkedHashMap.Entry me : ordered_products.entrySet()) {

            String groupName = (String) me.getKey();
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Integer> groupProducts = (LinkedHashMap<String, Integer>) me.getValue();
            inf(groupName, groupProducts);

        }
    }

    private void inf(String groupName, LinkedHashMap<String, Integer> groupProducts) {

        TextView tv = new TextView(mActivity);
        tv.setText(groupName);
        tv.setTextColor(mActivity.getResources().getColor(R.color.white,null));
        tv.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.btn_background, null));
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
        tv.setPadding(120, 0, 120, 0);
        pWrapper.addView(tv);

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            for (LinkedHashMap.Entry ent : groupProducts.entrySet()) {

                View childView = inflater.inflate(R.layout.inflated2, pWrapper, false);
                ((TextView) childView.findViewById(R.id.pDty)).setText((String) ent.getKey());
                ((TextView) childView.findViewById(R.id.pNmy)).setText(String.valueOf(ent.getValue()));

                pWrapper.addView(childView);
            }
        }

    }

    public interface OnOrderFragmentInteractionWithParentActivityListener {
        void messageFromOrderFragmentToParentActivityToRestartTask();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // nulling mParentListener in onDetach because it is instantiated in onAttach
        if (mParentActivityListener != null) {
            mParentActivityListener = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // nulling mDialog in onDestroyView because it is instantiated in onViewCreated
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }


}
