package com.appglue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.Constants.ProcessType;
import com.appglue.Constants.ServiceType;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.TAG;

public class ActivityApp extends Activity {

    private LocalStorage localStorage;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_app);

        Registry registry = Registry.getInstance(this);
        localStorage = LocalStorage.getInstance();

        Intent intent = this.getIntent();
        String packageName = intent.getStringExtra(PACKAGENAME);
        if (LOG) Log.d(TAG, "Got package " + packageName);

        AppDescription app = registry.getApp(packageName);

        ((TextView) findViewById(R.id.app_name)).setText(app.getName());
        ((TextView) findViewById(R.id.app_dev)).setText(app.getDeveloper());
        ((TextView) findViewById(R.id.app_description)).setText(app.getDescription());

        ListView componentList = (ListView) findViewById(R.id.app_component_list);
        ArrayList<ServiceDescription> components = registry.getComponentsForApp(app.getPackageName());

        componentList.setAdapter(new AppComponentAdapter(this, components));
    }

    private class AppComponentAdapter extends ArrayAdapter<ServiceDescription> {
        private ArrayList<ServiceDescription> items;

        public AppComponentAdapter(Context context, ArrayList<ServiceDescription> items) {
            super(context, R.layout.component_list_item, items);

            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (v == null) {
                v = vi.inflate(R.layout.component_list_item, viewGroup);
            }

            ServiceDescription sd = items.get(position);


            ImageView icon = (ImageView) v.findViewById(R.id.simple_list_icon);

            if (sd.getServiceType() == ServiceType.IN_APP)
                icon.setImageResource(R.drawable.icon);
            else if (sd.getServiceType() == ServiceType.LOCAL) {
                if (sd.getApp() == null)
                    icon.setImageResource(R.drawable.ic_lock_silent_mode_vibrate);
                else {
                    Drawable d = new BitmapDrawable(getResources(), localStorage.readIcon(sd.getApp().getIconLocation()));
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


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Make the component link go to the page of the relevant component
                }
            });

            return v;
        }
    }


    // Need to pass down that they might want to tick it if they've come from the composition page
    // Need to pass back if they do actually click the tick. It should just bubble back up but you never know
}
