package com.vegas.tranquilo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vegas.tranquilo.R;
import com.vegas.tranquilo.models.ProductMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductMemberAdapter extends RecyclerView.Adapter {

    private List<ProductMember> productMembers;
    private Context context;
    private OnMessageFromSingleGroup onMessageFromSingleGroup;
    private String myGroupName;


    ProductMemberAdapter(Context context, List<ProductMember> productMembers, OnMessageFromSingleGroup onMessageFromSingleGroup, String myGroupName) {
        this.context = context;


        if (productMembers != null && productMembers.size() > 0) {
            Collections.sort(productMembers, ProductMember.getComparator(ProductMember.SortParameter.PRICE_ASCENDING));// sort price ascending
        }

        this.productMembers = productMembers;
        this.onMessageFromSingleGroup = onMessageFromSingleGroup;
        this.myGroupName = myGroupName;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layout = -1;
        View v;
        if (viewType == 0) {
            layout = R.layout.product_member;
            v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(layout, parent, false);
            return new MemberViewHolder(v, onMessageFromSingleGroup, myGroupName);
        } else {
            v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(layout, parent, false);
            return new MemberViewHolder(v, onMessageFromSingleGroup, myGroupName);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MemberViewHolder) holder).populate(context, productMembers, productMembers.get(position));
    }

    //---------------------------------------------------------------

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private TextView memberTitle;
        private Spinner spinner;
        private OnMessageFromSingleGroup onMessageFromSingleGroup;
        private String myGroupName;

        MemberViewHolder(View itemView, OnMessageFromSingleGroup onMessageFromSingleGroup, String myGroupName) {
            super(itemView);

            memberTitle = itemView.findViewById(R.id.memberTitle);
            spinner = itemView.findViewById(R.id.spinner);

            this.onMessageFromSingleGroup = onMessageFromSingleGroup;
            this.myGroupName = myGroupName;

        }

        void populate(Context context, final List<ProductMember> memberList, final ProductMember productMember) { // all text view is set here
            memberTitle.setText(productMember.getTitle());

            final ArrayList<String> str = new ArrayList<>();

            for (int i = 0; i <= productMember.getMax_num(); i++) {
                str.add(String.valueOf(i));
            }

            //################################[  MAGIC STARTS  ]##################################

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                    productMember.setCurrentNum(Integer.parseInt(parent.getSelectedItem().toString()));//save selection

                    //>>[ set the Tot for each row on selection ]>>>>[ start ]>>>>>>>
                    int rowTot = str.indexOf(String.valueOf(productMember.getCurrentNum())) * productMember.getPrice();
                    productMember.setTot(rowTot);
                    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>[ end ]>>>>>>>>

                    //>>>>>>>>>>>>>>>>[ get All Tots and sum it ]>>>>[ start ]>>>>>>>
                    int myGroupFinalTotal = 0;
                    for (int i = 0; i < memberList.size(); i++) {
                        myGroupFinalTotal = myGroupFinalTotal + memberList.get(i).getTot();// total of members of this group only
                    }
                    onMessageFromSingleGroup.totalFromSingleGroup(myGroupName, myGroupFinalTotal);

                    /*
                     * on first show of the listView and also when scrolling up or down
                     * the getView(); function is called [ it is called when any row appears ]
                     * it is called again and again while scrolling up down and because the
                     * onItemSelectedListener is implemented inside it and this is the perfect
                     * place  for the listener .. so when getView(); called the onItemSelected
                     * is executed even if you did not manually select any item the listView
                     * selects the default "0" for you every time row appears
                     * so we the row Tot when selected
                     *
                     * */
                    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

                    Log.i("called", "onSelected");

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            //################################[  MAGIC ENDS  ]##################################

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, str);
            spinner.setAdapter(adapter);
            spinner.setSelection(str.indexOf(String.valueOf(productMember.getCurrentNum())));// important

            //################################[ disable row ]#####################################
//            if (!productMember.isAvailable()) {
////                    row.setBackgroundColor(getResources().getColor(R.color.light_grey,null));
//                itemView.setBackgroundResource(R.drawable.btn_off);
//                spinner.setEnabled(false);
//            } else {// enable row
//                itemView.setBackgroundResource(R.drawable.white_bg);
//                spinner.setEnabled(true);
//            }
            //#####################################################################################


        }


    }

    //----------------------------------------------------------------

    public interface OnMessageFromSingleGroup {
        void totalFromSingleGroup(String groupName, int singleGroupTotal);
    }

    @Override
    public int getItemCount() {
        return productMembers.size();
    }

}
