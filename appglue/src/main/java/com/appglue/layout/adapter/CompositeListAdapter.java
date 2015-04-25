package com.appglue.layout.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appglue.R;
import com.appglue.description.AppDescription;
import com.appglue.engine.model.ComponentService;
import com.appglue.engine.model.CompositeService;
import com.appglue.layout.FragmentCompositeList;
import com.appglue.layout.FragmentComposites;
import com.appglue.library.LocalStorage;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;

public class CompositeListAdapter extends ArrayAdapter<CompositeService> {

    int mSelectedIndex = -1;
    private Boolean[] expanded;

    private LocalStorage mLocalStorage;
    private FragmentCompositeList mFragment;

    public CompositeListAdapter(Context context, FragmentCompositeList fragment,
                                ArrayList<CompositeService> items) {
        super(context, R.layout.list_item_composite, items);
        expanded = new Boolean[items.size()];
        for (int i = 0; i < expanded.length; i++) {
            expanded[i] = false;
        }

        mFragment = fragment;
        mLocalStorage = LocalStorage.getInstance();
    }

    @SuppressLint("InflateParams")
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_composite, null);
        }

        final CompositeService item = getItem(position);

        if (v == null)
            return null;

        TextView nameText = (TextView) v.findViewById(R.id.composite_name);
        if (nameText == null) // This way it doesn't die, but this way of fixing it doesn't seem to be a problem...
            return v;

        if (item.getName().equals(""))
            nameText.setText("Temp ");
        else
            nameText.setText(item.getName());

        ImageView icon = (ImageView) v.findViewById(R.id.composite_icon);
        SparseArray<ComponentService> components = item.getComponents();

        AppDescription app = components.get(0).getDescription().getApp();
        if (app == null || app.iconLocation() == null) {
            icon.setBackgroundResource(R.drawable.icon);
        } else {
            String iconLocation = app.iconLocation();
            Bitmap b = mLocalStorage.readIcon(iconLocation);
            if (b != null) {
                icon.setImageBitmap(b);
            } else {
                icon.setBackgroundResource(R.drawable.icon);
            }
        }

        v.findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment.getParentFragment() != null) {
                    ((FragmentComposites) mFragment.getParentFragment()).viewComposite(item.getID());
                } else {
                    // Not sure why this would happen, it seems that android might have killed it. Maybe because there's not a reference to it?
                    Log.e(TAG, "Parent fragment is null");
                }
            }
        });

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isEnabled()) {
                    mSelectedIndex = mFragment.showToolbar(mSelectedIndex, position, item);
                    notifyDataSetChanged();
                } else if (mFragment.getParentFragment() != null) {
                    ((FragmentComposites) mFragment.getParentFragment()).viewComposite(item.getID());
                } else {
                    // TODO Not sure why this would happen, it seems that android might have killed it. Maybe because there's not a reference to it?
                    Log.e(TAG, "Parent fragment is null");
                }
            }
        });

        LinearLayout componentContainer = (LinearLayout) v.findViewById(R.id.composite_components);
        componentContainer.removeAllViews();

        Resources res = mFragment.getResources();

        if (expanded[position]) {
            for (int i = 0; i < item.getComponents().size(); i++) {
                ComponentService component = item.getComponents().get(i);
                TextView tv = new TextView(getContext());
                tv.setText(component.getDescription().getName());

                // TODO In expanded mode we need to add more information about the components

                if (item.isEnabled()) {
                    if (position == mSelectedIndex) {
                        tv.setTextColor(res.getColor(R.color.textColor));
                    } else {
                        tv.setTextColor(res.getColor(R.color.textColor_dim));
                    }
                } else {
                    tv.setTextColor(res.getColor(R.color.textColor_dimmer));
                }

                componentContainer.addView(tv);
            }
        } else {
            for (int i = 0; i < item.getComponents().size(); i++) {
                ComponentService component = item.getComponents().get(i);
                TextView tv = new TextView(getContext());
                tv.setText(component.getDescription().getName());

                if (item.isEnabled()) {
                    if (position == mSelectedIndex) {
                        tv.setTextColor(res.getColor(R.color.textColor));
                    } else {
                        tv.setTextColor(res.getColor(R.color.textColor_dim));
                    }
                } else {
                    tv.setTextColor(res.getColor(R.color.textColor_dimmer));
                }

                componentContainer.addView(tv);
            }
        }

        View backgroundView = v.findViewById(R.id.composite_item_bg);
        if (item.isEnabled()) {
            // The text needs to be brighter
            if (position == mSelectedIndex) {
                // This is selected so it should be bright
                backgroundView.setBackgroundResource(item.getColour(true));
                nameText.setTextColor(res.getColor(R.color.textColorInverse));
            } else {
                backgroundView.setBackgroundResource(item.getColour(false));
                nameText.setTextColor(res.getColor(R.color.textColor));
            }

            // The image needs to be in colour
            icon.setColorFilter(null);
        } else {
            backgroundView.setBackgroundResource(item.getColour(false));
            nameText.setTextColor(res.getColor(R.color.textColorInverse_dim));

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            icon.setColorFilter(filter);
        }

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                expanded[position] = !expanded[position];
                notifyDataSetChanged();
                return true;
            }
        });

        return v;
    }

    public CompositeService getCurrentComposite() {
        return getItem(mSelectedIndex);
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }
}
