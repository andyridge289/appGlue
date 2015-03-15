package com.appglue.layout.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appglue.R;
import com.appglue.SystemFeature;
import com.appglue.WiringActivity;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.OrchestrationServiceConnection;
import com.appglue.layout.FragmentComponentListPager;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.LocalStorage;

import java.util.ArrayList;

public class AdapterComponentList extends ArrayAdapter<ServiceDescription> {

    ArrayList<ServiceDescription> originalItems;
    private ArrayList<ServiceDescription> items;

    private Activity parent;

    private FragmentComponentListPager parentFragment;

    private final Object lock = new Object();

    public AdapterComponentList(Context context, ArrayList<ServiceDescription> items, FragmentComponentListPager parentFragment) {
        super(context, R.layout.list_item_component, items);

        this.parent = (Activity) context;
        this.parentFragment = parentFragment;

        this.items = items;
        cloneItems();
    }

    private void cloneItems() {
        originalItems = new ArrayList<ServiceDescription>();

        for (ServiceDescription item : items) {
            originalItems.add(item);
        }
    }

    @Override
    public int getCount() {
        synchronized (lock) {
            return items != null ? items.size() : 0;
        }
    }

    @Override
    public ServiceDescription getItem(int item) {
        ServiceDescription gi;
        synchronized (lock) {
            gi = items != null ? items.get(item) : null;
        }
        return gi;
    }

    @Override
    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View v = convertView;
        LayoutInflater vi = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (v == null) {
            v = vi.inflate(R.layout.list_item_component, null);
        }

        ServiceDescription sd;
        synchronized (lock) {
            sd = items.get(position);
        }

        if (sd == null)
            return v;

        boolean enabled = true;
        ImageView appIcon = (ImageView) v.findViewById(R.id.component_app_icon);
        TextView versionText = (TextView) v.findViewById(R.id.version_text);
        versionText.setVisibility(View.GONE);

        ArrayList<SystemFeature> missingFeatures = sd.missingFeatures(getContext());
        if (!sd.matchesVersion()) {

            enabled = false;
            String version = AppGlueLibrary.getVersionName(sd.getMinVersion());
            appIcon.setImageResource(R.drawable.ic_android_black_24dp);
            versionText.setText(version);
            versionText.setVisibility(View.VISIBLE);

        } else if (missingFeatures.size() > 0) {

            // Just use the first one for now
            enabled = false;
            appIcon.setImageResource(missingFeatures.get(0).icon);

        } else {
            AppDescription app = sd.getApp();
            if (app != null) {
                String iconLocation = app.getIconLocation();
                LocalStorage ls = LocalStorage.getInstance();
                Bitmap bmp = ls.readIcon(iconLocation);
                if (bmp == null) {
                    appIcon.setImageResource(R.drawable.icon);
                } else {
                    appIcon.setImageBitmap(bmp);
                }
            } else {
                // Just use our icon as the default
                appIcon.setImageResource(R.drawable.icon);
            }
        }

        int paramStatus = OrchestrationServiceConnection.paramTest(sd, getContext());
        if (paramStatus != 0) {
            enabled = false;
            appIcon.setImageResource(R.drawable.ic_settings_black_24dp);
        }

        View iconContainer = v.findViewById(R.id.component_icon_container);
        TextView serviceName = (TextView) v.findViewById(R.id.service_name);
        serviceName.setText(sd.getName());

        LinearLayout flagContainer = (LinearLayout) v.findViewById(R.id.flag_container);
        AppGlueLibrary.addFlagsToLayout(flagContainer, sd, vi, false, enabled);

        View inputs = v.findViewById(R.id.comp_item_inputs);
        if (sd.hasInputs()) {
            inputs.setBackgroundResource(R.drawable.has_io);
        } else {
            v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.inputs);
        }

        View outputs = v.findViewById(R.id.comp_item_outputs);
        if (sd.hasOutputs()) {
            outputs.setBackgroundResource(R.drawable.has_io);
        } else {
            v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.outputs);
        }

        final ServiceDescription sd2 = sd;


        if (enabled && !parentFragment.isJustList()) {

            // If it's not a list and it's enabled then choose it
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseClick(sd2);
                }
            });

            v.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    viewClick(sd2);
                    return true;
                }
            });
        } else { // It's either just a list or the component is disabled

            // If it's not a list and its disable
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewClick(sd2);
                }
            });
        }

        if (enabled) {
            serviceName.setTextColor(getContext().getResources().getColor(R.color.textColor));
            iconContainer.setBackgroundResource(R.drawable.circle_component);
        } else {
            serviceName.setTextColor(getContext().getResources().getColor(R.color.hex888));
            iconContainer.setBackgroundResource(R.drawable.circle_component_off);

            if (sd.hasInputs()) {
                inputs.setBackgroundResource(R.drawable.has_io_off);
            }

            if (sd.hasOutputs()) {
                outputs.setBackgroundResource(R.drawable.has_io_off);
            }
        }

        return v;
    }

    private void viewClick(ServiceDescription sd2) {
        // Tell the parent fragment to show the component page
        parentFragment.showServiceDescription(sd2.getClassName());
    }

    private void chooseClick(ServiceDescription sd2) {
        // Choose the component to go in the composite
        if (parentFragment.getActivity() != null) {
            ((WiringActivity) parentFragment.getActivity()).chooseItem(sd2.getClassName());
        }
    }
}
