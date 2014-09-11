package com.appglue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appglue.Constants.ProcessType;
import com.appglue.Constants.ServiceType;
import com.appglue.description.ServiceDescription;
import com.appglue.library.LocalStorage;

import java.util.ArrayList;

class AdapterComponentList extends ArrayAdapter<ServiceDescription> {

    ArrayList<ServiceDescription> originalItems;
    private ArrayList<ServiceDescription> items;

    private ActivityComponentList parent;

    private LocalStorage localStorage;

    private final Object lock = new Object();

    public AdapterComponentList(Context context, ArrayList<ServiceDescription> items) {
        super(context, R.layout.component_list_item, items);

        localStorage = LocalStorage.getInstance();

        this.parent = (ActivityComponentList) context;

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

        if (v == null)
            v = vi.inflate(R.layout.component_list_item, null);

        ServiceDescription sd;
        synchronized (lock) {
            sd = items.get(position);
        }

        if (sd == null)
            return v;

        ImageView icon = (ImageView) v.findViewById(R.id.simple_list_icon);

        if (sd.getServiceType() == ServiceType.IN_APP)
            icon.setImageResource(R.drawable.icon);
        else if (sd.getServiceType() == ServiceType.LOCAL) {
            if (sd.app() == null)
                icon.setImageResource(R.drawable.ic_lock_silent_mode_vibrate);
            else {
                Drawable d = new BitmapDrawable(parent.getResources(), localStorage.readIcon(sd.app().iconLocation()));
                icon.setImageDrawable(d);
            }
        } else
            icon.setImageResource(R.drawable.ic_menu_upload);

        TextView serviceName = (TextView) v.findViewById(R.id.service_name);
        serviceName.setText(sd.getName());

        if (sd.getProcessType() == ProcessType.TRIGGER) {
            v.findViewById(R.id.comp_item_trigger).setVisibility(View.VISIBLE);
            v.findViewById(R.id.comp_item_trigger_text).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.comp_item_trigger).setVisibility(View.GONE);
            v.findViewById(R.id.comp_item_trigger_text).setVisibility(View.GONE);
        }

        if (sd.getProcessType() == ProcessType.FILTER) {
            v.findViewById(R.id.comp_item_filter).setVisibility(View.VISIBLE);
            v.findViewById(R.id.comp_item_filter_text).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.comp_item_filter).setVisibility(View.GONE);
            v.findViewById(R.id.comp_item_filter_text).setVisibility(View.GONE);
        }

        if (sd.hasInputs()) {
            v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.has_io);
        } else {
            v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.inputs);
        }

        if (sd.hasOutputs()) {
            v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.has_io);
        } else {
            v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.outputs);
        }

        return v;
    }
}
