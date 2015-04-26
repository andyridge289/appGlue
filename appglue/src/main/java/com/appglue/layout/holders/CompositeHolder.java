package com.appglue.layout.holders;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appglue.R;
import com.appglue.description.AppDescription;
import com.appglue.engine.model.ComponentService;
import com.appglue.engine.model.CompositeService;
import com.appglue.library.Assert;
import com.appglue.library.LocalStorage;

/**
 *
 */
public class CompositeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private View mView;
    private View mInfoButton;
    private View mBackgroundView;

    private TextView mNameText;
    private ImageView mIcon;

    private LinearLayout mComponentContainer;

    private LocalStorage mLocalStorage;

    private Fragment mFragment;

    public CompositeHolder(View v, Fragment fragment) {
        super(v);
        mView = v;
        mFragment = fragment;

        mNameText = (TextView) v.findViewById(R.id.composite_name);
        mIcon = (ImageView) v.findViewById(R.id.composite_icon);

        mInfoButton = v.findViewById(R.id.info_button);

        mComponentContainer = (LinearLayout) v.findViewById(R.id.composite_components);
        mBackgroundView = v.findViewById(R.id.composite_item_bg);

        mLocalStorage = LocalStorage.getInstance();
    }

    public void setName(String name) {
        if (Assert.exists(name)) {
            return;
        }

        if (name.equals("")) {
            mNameText.setText("Temp ");
        } else {
            mNameText.setText(name);
        }
    }

    public void setIcon(CompositeService item) {
        SparseArray<ComponentService> components = item.getComponents();

        AppDescription app = components.get(0).getDescription().getApp();
        if (app == null || app.iconLocation() == null) {
            mIcon.setBackgroundResource(R.drawable.icon);
        } else {
            String iconLocation = app.iconLocation();
            Bitmap b = mLocalStorage.readIcon(iconLocation);
            if (b != null) {
                mIcon.setImageBitmap(b);
            } else {
                mIcon.setBackgroundResource(R.drawable.icon);
            }
        }
    }

    public void setExpanded(boolean expanded) {

    }

    @Override public void onClick(View v) {     }
}
