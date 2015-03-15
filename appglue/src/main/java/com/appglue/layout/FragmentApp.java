package com.appglue.layout;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.AppGlueFragment;
import com.appglue.R;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.library.AppGlueLibrary;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.PACKAGENAME;

public class FragmentApp extends Fragment implements AppGlueFragment {

    private AppDescription app;
    private Registry registry;

    public static Fragment create() {
        return new FragmentApp();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        registry = Registry.getInstance(getActivity());

        if (getArguments() != null) {
            String packageName = getArguments().getString(PACKAGENAME);
            app = registry.getApp(packageName);

        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.activity_app, container, false);

        ((TextView) root.findViewById(R.id.app_name)).setText(app.getName());
        ((TextView) root.findViewById(R.id.app_dev)).setText(app.getDeveloper());
        ((TextView) root.findViewById(R.id.app_description)).setText(app.getDescription());

        ListView componentList = (ListView) root.findViewById(R.id.app_component_list);
        ArrayList<ServiceDescription> components = registry.getComponentsForApp(app.getPackageName());

        componentList.setAdapter(new AppComponentAdapter(getActivity(), components));
        return root;
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setData(String packageName) {
        if (registry == null)
            registry = Registry.getInstance(getActivity());

        app = registry.getApp(packageName);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public String onCreateOptionsMenu(Menu menu) {
        return app.getName();
    }

    private class AppComponentAdapter extends ArrayAdapter<ServiceDescription> {
        private ArrayList<ServiceDescription> items;

        public AppComponentAdapter(Context context, ArrayList<ServiceDescription> items) {
            super(context, R.layout.list_item_component, items);

            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (v == null) {
                v = vi.inflate(R.layout.list_item_component, null);
            }

            ServiceDescription sd = items.get(position);

//            ImageView icon = (ImageView) v.findViewById(R.id.simple_list_icon);

//            if (sd.getServiceType() == ServiceType.IN_APP)
//                icon.setImageResource(R.drawable.icon);
//            else if (sd.getServiceType() == ServiceType.LOCAL) {
//                if (sd.getApp() == null) {
//                    icon.setImageResource(R.drawable.ic_lock_silent_mode_vibrate);
//                } else {
//                    Drawable d = new BitmapDrawable(getResources(), localStorage.readIcon(sd.getApp().iconLocation()));
//                    icon.setImageDrawable(d);
//                }
//            } else {
//                icon.setImageResource(R.drawable.ic_menu_upload);
//            }

            // TODO OnBackPressed with drawer open -> close drawer

            TextView serviceName = (TextView) v.findViewById(R.id.service_name);
            serviceName.setText(sd.getName());

            LinearLayout flagContainer = (LinearLayout) v.findViewById(R.id.flag_container);
            AppGlueLibrary.addFlagsToLayout(flagContainer, sd, vi, false, true);

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
}
