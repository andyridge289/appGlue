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

    @Override
    public void onClick(View v) {
//        if (mItem.isEnabled()) {
//            mFragment.showToolbar(getAdapterPosition());
//                    notifyDataSetChanged();
//                } else if (mFragment.getParentFragment() != null) {
//                    ((FragmentComposites) mFragment.getParentFragment()).viewComposite(item.getID());
//                } else {
//                    // TODO Not sure why this would happen, it seems that android might have killed it. Maybe because there's not a reference to it?
//                    Log.e(TAG, "Parent fragment is null");
//        }
    }

//
//        v.findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mFragment.getParentFragment() != null) {
//                    ((FragmentComposites) mFragment.getParentFragment()).viewComposite(item.getID());
//                } else {
//                    // Not sure why this would happen, it seems that android might have killed it. Maybe because there's not a reference to it?
//                    Log.e(TAG, "Parent fragment is null");
//                }
//            }
//        });
//
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//
//        LinearLayout componentContainer = (LinearLayout) v.findViewById(R.id.composite_components);
//        componentContainer.removeAllViews();
//
//        Resources res = mFragment.getResources();
//
//        if (expanded[position]) {
//            for (int i = 0; i < item.getComponents().size(); i++) {
//                ComponentService component = item.getComponents().get(i);
//                TextView tv = new TextView(getContext());
//                tv.setText(component.getDescription().getName());
//
//                // TODO In expanded mode we need to add more information about the components
//
//                if (item.isEnabled()) {
//                    if (position == mSelectedIndex) {
//                        tv.setTextColor(res.getColor(R.color.textColor));
//                    } else {
//                        tv.setTextColor(res.getColor(R.color.textColor_dim));
//                    }
//                } else {
//                    tv.setTextColor(res.getColor(R.color.textColor_dimmer));
//                }
//
//                componentContainer.addView(tv);
//            }
//        } else {
//            for (int i = 0; i < item.getComponents().size(); i++) {
//                ComponentService component = item.getComponents().get(i);
//                TextView tv = new TextView(getContext());
//                tv.setText(component.getDescription().getName());
//
//                if (item.isEnabled()) {
//                    if (position == mSelectedIndex) {
//                        tv.setTextColor(res.getColor(R.color.textColor));
//                    } else {
//                        tv.setTextColor(res.getColor(R.color.textColor_dim));
//                    }
//                } else {
//                    tv.setTextColor(res.getColor(R.color.textColor_dimmer));
//                }
//
//                componentContainer.addView(tv);
//            }
//        }
//
//        View backgroundView = v.findViewById(R.id.composite_item_bg);
//        if (item.isEnabled()) {
//            // The text needs to be brighter
//            if (position == mSelectedIndex) {
//                // This is selected so it should be bright
//                backgroundView.setBackgroundResource(item.getColour(true));
//                nameText.setTextColor(res.getColor(R.color.textColorInverse));
//            } else {
//                backgroundView.setBackgroundResource(item.getColour(false));
//                nameText.setTextColor(res.getColor(R.color.textColor));
//            }
//
//            // The image needs to be in colour
//            icon.setColorFilter(null);
//        } else {
//            backgroundView.setBackgroundResource(item.getColour(false));
//            nameText.setTextColor(res.getColor(R.color.textColorInverse_dim));
//
//            ColorMatrix matrix = new ColorMatrix();
//            matrix.setSaturation(0);
//            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
//            icon.setColorFilter(filter);
//        }
//
//        v.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                expanded[position] = !expanded[position];
//                notifyDataSetChanged();
//                return true;
//            }
//        });
//
//        return v;
//    }
//
}
