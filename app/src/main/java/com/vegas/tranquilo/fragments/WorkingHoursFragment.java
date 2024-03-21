package com.vegas.tranquilo.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vegas.tranquilo.R;

import org.joda.time.DateTimeComparator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WorkingHoursFragment extends Fragment {

    private Activity mActivity;

    private TextView day, st, en, dtNote;

    private SimpleDateFormat tf, df;
    private SwipeRefreshLayout swipeRefresh;

    private Date START_TIME, END_TIME;
    private OnWorkingHoursFragmentInteractionWithParentActivityListener mParentActivityListener;

    public WorkingHoursFragment() {
        // Required empty public constructor
    }

    public static WorkingHoursFragment newInstance(Date start_time, Date end_time) {

        WorkingHoursFragment f = new WorkingHoursFragment();
        Bundle b = new Bundle();
        b.putSerializable("start_time", start_time);
        b.putSerializable("end_time", end_time);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mActivity = (Activity) context;
        // check if parent Activity implements listener
        if (mActivity instanceof OnWorkingHoursFragmentInteractionWithParentActivityListener) {
            mParentActivityListener = (OnWorkingHoursFragmentInteractionWithParentActivityListener) mActivity;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChildFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.working_hours_fragment, container, false);


        day = view.findViewById(R.id.day);
        st = view.findViewById(R.id.st);
        en = view.findViewById(R.id.en);
        dtNote = view.findViewById(R.id.dtNote);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);


        tf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        df = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());


        if (getArguments() != null) {
            START_TIME = (Date) getArguments().getSerializable("start_time");
            END_TIME = (Date) getArguments().getSerializable("end_time");
        }

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        if (DateTimeComparator.getDateOnlyInstance().compare(Calendar.getInstance().getTime(), START_TIME) == 0) {
            day.setText(getResources().getString(R.string.today));
            st.setText(tf.format(START_TIME));
            en.setText(tf.format(END_TIME));
            if ((DateTimeComparator.getTimeOnlyInstance().compare(Calendar.getInstance().getTime(), END_TIME) > 0)
                    && (DateTimeComparator.getDateOnlyInstance().compare(Calendar.getInstance().getTime(), END_TIME) >= 0)) {
                dtNote.setText(getResources().getString(R.string.finishedNote));
            }
        } else if (DateTimeComparator.getDateOnlyInstance().compare(Calendar.getInstance().getTime(), START_TIME) < 0) {
            day.setText(df.format(START_TIME));
            st.setText(tf.format(START_TIME));
            en.setText(tf.format(END_TIME));
        } else {
            day.setText("");
            st.setText("");
            en.setText("");
            dtNote.setText(getResources().getString(R.string.laterNote));
        }


        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(false);
            mParentActivityListener.messageFromWorkingHoursFragmentToParentActivityToRestartTask();
        });

    }

    public interface OnWorkingHoursFragmentInteractionWithParentActivityListener {
        void messageFromWorkingHoursFragmentToParentActivityToRestartTask();
    }


}
