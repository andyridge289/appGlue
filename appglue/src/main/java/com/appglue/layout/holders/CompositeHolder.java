package com.appglue.layout.holders;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
import com.appglue.layout.FragmentCompositeList;
import com.appglue.layout.adapter.CompositeListAdapter;
import com.appglue.library.Assert;
import com.appglue.library.LocalStorage;

/**
 *
 */
public class CompositeHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {

    private View mView;
    private View mInfoButton;
    private View mBackgroundView;

    private TextView mNameText;
    private ImageView mIcon;

    private LinearLayout mComponentContainer;

    private LocalStorage mLocalStorage;

    private FragmentCompositeList mFragment;

    private CompositeListAdapter mAdapter;

    private boolean mExpanded;

    public CompositeHolder(View v, FragmentCompositeList fragment, CompositeListAdapter adapter) {
        super(v);
        mView = v;
        mFragment = fragment;
        mAdapter = adapter;

        mNameText = (TextView) v.findViewById(R.id.composite_name);
        mIcon = (ImageView) v.findViewById(R.id.composite_icon);

        mInfoButton = v.findViewById(R.id.info_button);

        mComponentContainer = (LinearLayout) v.findViewById(R.id.composite_components);
        mBackgroundView = v.findViewById(R.id.composite_item_bg);

        mLocalStorage = LocalStorage.getInstance();

        mView.setOnClickListener(this);
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

    public CompositeService getItem() {
        int position = getAdapterPosition();
        return mFragment.getComposite(position);
    }

    public void setBackground(CompositeService item) {

        Resources res = mFragment.getActivity().getResources();

        if (item.isEnabled()) {
            // The text needs to be brighter
            if (getAdapterPosition() == mAdapter.getSelectedIndex()) {
                // This is selected so it should be bright
                mBackgroundView.setBackgroundResource(item.getColour(true));
                mNameText.setTextColor(res.getColor(R.color.textColorInverse));
            } else {
                mBackgroundView.setBackgroundResource(item.getColour(false));
                mNameText.setTextColor(res.getColor(R.color.textColor));
            }

            // The image needs to be in colour
            mIcon.setColorFilter(null);
        } else {
            mBackgroundView.setBackgroundResource(item.getColour(false));
            mNameText.setTextColor(res.getColor(R.color.textColorInverse_dim));

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            mIcon.setColorFilter(filter);
        }
    }

//                        v.findViewById(R.id.info_button).setOnClickListener(new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                                if (getParentFragment() != null) {
//                                        ((FragmentComposites) getParentFragment()).viewComposite(item.getID());
//                                    } else {
//                                        // Not sure why this would happen, it seems that android might have killed it. Maybe because there's not a reference to it?
//                                                Logger.e("Parent fragment is null");
//                                    }
//                            }
//                    });
//    
//                        v.setOnClickListener(new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                                {
//                    });
//    
//                        LinearLayout componentContainer = (LinearLayout) v.findViewById(R.id.composite_components);
//                componentContainer.removeAllViews();
//    
//
//    
//            
//    
//                        v.setOnLongClickListener(new View.OnLongClickListener() {
//                        @Override
//                        public boolean onLongClick(View v) {
//                                expanded[position] = !expanded[position];
//                                notifyDataSetChanged();
//                                return true;
//                            }
//                    });

    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
        if (mExpanded) {
            mComponentContainer.removeAllViews();
        }

        Context context = mFragment.getActivity();

        CompositeService item = getItem();
        if (mExpanded) {
            for (ComponentService component : item.getComponentsAL()) {
                TextView tv = new TextView(context);
                tv.setText(component.getDescription().getName());

                // TODO In expanded mode we need to add more information about the components
                if (item.isEnabled()) {
                    if (getAdapterPosition() == mAdapter.getSelectedIndex()) {
                        tv.setTextColor(context.getResources().getColor(R.color.textColor));
                    } else {
                        tv.setTextColor(context.getResources().getColor(R.color.textColor_dim));
                    }
                } else {
                    tv.setTextColor(context.getResources().getColor(R.color.textColor_dimmer));
                }

                mComponentContainer.addView(tv);
            }
        } else {
            for (int i = 0; i < item.getComponents().size(); i++) {
                ComponentService component = item.getComponents().get(i);
                TextView tv = new TextView(context);
                tv.setText(component.getDescription().getName());

                if (item.isEnabled()) {
                    if (getAdapterPosition() == mAdapter.getSelectedIndex()) {
                        tv.setTextColor(context.getResources().getColor(R.color.textColor));
                    } else {
                        tv.setTextColor(context.getResources().getColor(R.color.textColor_dim));
                    }
                } else {
                    tv.setTextColor(context.getResources().getColor(R.color.textColor_dimmer));
                }

                mComponentContainer.addView(tv);
            }
        }
    }

    @Override public void onClick(View v) {
//        if (item.isEnabled()) {
//                                        selectedIndex = showToolbar(selectedIndex, position, item);
//                                        notifyDataSetChanged();
//                                    } else if (getParentFragment() != null) {
//                                        ((FragmentComposites) getParentFragment()).viewComposite(item.getID());
//                                    } else {
//                                        // TODO Not sure why this would happen, it seems that android might have killed it. Maybe because there's not a reference to it?
//                                                Logger.e("Parent fragment is null");
//                                    }
//                            }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mExpanded) {
            setExpanded(false);
        } else {
            setExpanded(true);
        }
        return true;
    }
}
