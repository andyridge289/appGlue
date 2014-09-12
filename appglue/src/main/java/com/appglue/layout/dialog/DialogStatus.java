package com.appglue.layout.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.appglue.ActivityWiring;
import com.appglue.IODescription;
import com.appglue.R;
import com.appglue.engine.description.ServiceIO;

import java.util.ArrayList;
import java.util.List;

class DialogStatus extends DialogCustom {
    // FIXME Doing the dialog for the statuses

    public DialogStatus(final ActivityWiring activity, final ServiceIO item) {
        super(activity, item);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_status, null);

        ListView lv = (ListView) v.findViewById(R.id.status_list);
        lv.setAdapter(new StatusAdapter(activity, new ArrayList<String>()));

        // Get a list of the installed apps and then show them on something
//		final PackageManager pm = activity.getPackageManager();
//		final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
//		final AppChooserAdapter adapter = new AppChooserAdapter(activity, packages, pm);
//
//		GridView g = (GridView) v.findViewById(R.getID.app_grid);
//		g.setAdapter(adapter);
//
//		setView(v);
//
//		Button positiveButton = (Button) v.findViewById(R.getID.dialog_app_positive);
//		Button negativeButton = (Button) v.findViewById(R.getID.dialog_app_negative);
//
//		positiveButton.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				if(adapter.selectedIndex == -1)
//				{
//					if(LOG) Log.d(TAG, "No selected index");
//					cancel();
//					return;
//				}
//
//				// The app they want to load is selectedApp.packageName
//				ApplicationInfo selected = packages.get(adapter.selectedIndex);
//				if(selected == null)
//				{
//					if(LOG) Log.d(TAG, "No selected app info");
//					cancel();
//					return;
//				}
//
//				if(LOG) Log.d(TAG, "Setting package name to " + selected.packageName);
//				item.setManualValue(selected.packageName);
//				item.setFilterState(ServiceIO.MANUAL_FILTER);
//				DialogStatus.this.activity.setStatus("Chosen app: " + selected.packageName);
//
//				registry.updateCurrent();
//				parent.redraw();
//				dismiss();
//			}
//		});
//
//		setTitle("Select app");
//
//		negativeButton.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				cancel();
//			}
//		});
    }

    private class StatusAdapter extends ArrayAdapter<String> {

        public StatusAdapter(Context context, List<String> values) {
            super(context, R.layout.list_item_app_selector, values);
        }

        public View getView(final int position, View v, ViewGroup parent) {
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item_status, parent);
            }

//            TextView tv = (TextView) v.findViewById(R.getID.status_text);

            return v;
        }
    }
}