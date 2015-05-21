package com.appglue.layout.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appglue.R;
import com.appglue.engine.model.CompositeService;
import com.appglue.layout.FragmentCompositeList;
import com.appglue.layout.holders.CompositeHolder;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;

public class CompositeListAdapter extends RecyclerView.Adapter<CompositeHolder> {

    private FragmentCompositeList mFragment;
    private ArrayList<CompositeService> mItems;

    private int mSelectedIndex = -1;

    public CompositeListAdapter(FragmentCompositeList fragment, ArrayList<CompositeService> items) {
        mFragment = fragment;
        mItems = items;

        this.setHasStableIds(true);
    }

    @Override
    public CompositeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_composite, parent, false);

        Log.d(TAG, "Returning composite holder");
        return new CompositeHolder(v, mFragment, this);
    }

    @Override
    public void onBindViewHolder(CompositeHolder holder, int position) {
        CompositeService composite = mItems.get(position);
        holder.setName(composite.getName());
        holder.setIcon(composite);
        holder.setBackground(composite);
        holder.setExpanded(false);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "Returning size " + mItems.size());
        return mItems.size();
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
    }
}
