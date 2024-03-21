package com.vegas.tranquilo.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vegas.tranquilo.R;
import com.vegas.tranquilo.models.ProductGroup;
import com.vegas.tranquilo.models.ProductMember;
import com.vegas.tranquilo.models.SelectedRowModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ProductGroupAdapter extends RecyclerView.Adapter implements ProductMemberAdapter.OnMessageFromSingleGroup {

    private List<ProductGroup> productGroups;
    private Context context;
    private OnMessageFromAllGroups onMessageFromAllGroups;
    private HashMap<String, Integer> groups_totals_hash = new HashMap<>();


    public ProductGroupAdapter(Context context, List<ProductGroup> productGroups, OnMessageFromAllGroups onMessageFromAllGroups) {
        this.productGroups = productGroups;
        this.context = context;
        this.onMessageFromAllGroups = onMessageFromAllGroups;

        for (ProductGroup productGroup : productGroups) {// initialize totals with 0
            groups_totals_hash.put(productGroup.getGroup_name(), 0);
        }

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
            layout = R.layout.product_group;
            v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(layout, parent, false);
            return new GroupViewHolder(v);
        } else {
            v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(layout, parent, false);
            return new GroupViewHolder(v);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((GroupViewHolder) holder).populate(productGroups.get(position));
        if (productGroups.get(position).getProducts() != null && productGroups.get(position).getProducts().size() > 0)
            setMembersRV(((GroupViewHolder) holder).membersRV, productGroups.get(position).getProducts(), productGroups.get(position).getGroup_name());
    }

    //---------------------------------------------------------------

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView group_name;
        private RecyclerView membersRV;

        GroupViewHolder(View itemView) {
            super(itemView);
            group_name = itemView.findViewById(R.id.group_name);
            membersRV = itemView.findViewById(R.id.membersRV);

        }

        void populate(ProductGroup productGroup) { // all text view is set here
            group_name.setText(productGroup.getGroup_name());
        }
    }

    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& set districtsRV &&&&&&&&&&&&&&&&&&&&&&&&&&&&&
    private void setMembersRV(RecyclerView recyclerView, List<ProductMember> productMembers, String group_name) {
        ProductMemberAdapter productMemberAdapter = new ProductMemberAdapter(context, productMembers, this, group_name);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new ItemDecoration(10));
        recyclerView.setAdapter(productMemberAdapter);
    }
    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

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

    @Override
    public int getItemCount() {
        return productGroups.size();
    }


    @Override
    public void totalFromSingleGroup(String groupName, int singleGroupTotal) {
        groups_totals_hash.put(groupName, singleGroupTotal);// update this group total value
        int supremeTotal = 0;
        for (int x : groups_totals_hash.values()) {
            supremeTotal += x;
        }
        onMessageFromAllGroups.totalFromAllGroups(supremeTotal);
    }

    public LinkedHashMap<String, Object> gimmeTheSelectedList() {

        LinkedHashMap<String, Object> groupsHash = new LinkedHashMap<>();// string is group name and Object is selected members list

        for (ProductGroup pg : productGroups) {
            List<SelectedRowModel> list = new ArrayList<>();

            for (ProductMember pm : pg.getProducts()) {
                if (pm.getCurrentNum() > 0) {
                    list.add(new SelectedRowModel(pm.getTitle(), pm.getCurrentNum(), pm.getTot()));
                }
            }
            if (list.size() > 0) {
                groupsHash.put(pg.getGroup_name(), list);
            }
        }
        return groupsHash;
    }

    public interface OnMessageFromAllGroups {
        void totalFromAllGroups(int supremeTotal);
    }

}
