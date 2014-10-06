package com.appglue.layout.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.appglue.ActivityWiring;
import com.appglue.R;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.library.FilterFactory;

import java.util.List;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class DialogApp extends DialogCustom {

    @SuppressLint("InflateParams")
    public DialogApp(final ActivityWiring activity, final ServiceIO item) {
        super(activity, item);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_app_picker, null);

        // Get a list of the installed apps and then show them on something
        final PackageManager pm = activity.getPackageManager();
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        final AppChooserAdapter adapter = new AppChooserAdapter(activity, packages, pm);

        GridView g = (GridView) v.findViewById(R.id.app_grid);
        g.setAdapter(adapter);

        setView(v);

        Button positiveButton = (Button) v.findViewById(R.id.dialog_app_positive);
        Button negativeButton = (Button) v.findViewById(R.id.dialog_app_negative);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.selectedIndex == -1) {
                    if (LOG) Log.d(TAG, "No selected index");
                    cancel();
                    return;
                }

                // The app they want to load is selectedApp.packageName
                ApplicationInfo selected = packages.get(adapter.selectedIndex);
                if (selected == null) {
                    if (LOG) Log.d(TAG, "No selected app info");
                    cancel();
                    return;
                }

                if (LOG) Log.d(TAG, "Setting package name to " + selected.packageName);

                IOValue value = new IOValue(FilterFactory.NONE, selected.packageName, item);
                item.setValue(value);
                DialogApp.this.activity.setStatus("Chosen app: " + selected.packageName);

                registry.updateComposite(activity.getComposite());
                activity.redraw();
                dismiss();
            }
        });

        setTitle("Select app");

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    private class AppChooserAdapter extends ArrayAdapter<ApplicationInfo> {
        private List<ApplicationInfo> values;
        private PackageManager pm;
        private int selectedIndex;

        public AppChooserAdapter(Context context, List<ApplicationInfo> values, PackageManager pm) {
            super(context, R.layout.grid_item_app_selector, values);

            this.pm = pm;
            this.values = values;
            this.selectedIndex = -1;
        }

        public View getView(final int position, View v, ViewGroup parent) {
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.grid_item_app_selector, parent);
            }

            if (position != selectedIndex) {
                v.setBackgroundResource(R.drawable.textview_button);
            } else {
                v.setBackgroundResource(R.drawable.textview_button_focused);
            }

            final ApplicationInfo app = values.get(position);

            TextView appName = (TextView) v.findViewById(R.id.load_list_name);
            ImageView appIcon = (ImageView) v.findViewById(R.id.service_icon);

            // Load all the icons in the background?
            appName.setText(app.loadLabel(pm));
            appIcon.setImageDrawable(app.loadIcon(pm));

            v.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // De-select everything
                    selectedIndex = position;
                    notifyDataSetChanged();
                }
            });

            return v;
        }
    }
}