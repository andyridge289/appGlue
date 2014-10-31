package com.appglue;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.library.ComponentLogItem;
import com.appglue.library.LogItem;
import com.appglue.serviceregistry.Registry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FragmentLog extends Fragment {
    private Registry registry;

    public static Fragment create() {
        return new FragmentLog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((ActivityAppGlue) activity).onSectionAttached(ActivityAppGlue.Page.LOG);

        registry = Registry.getInstance(getActivity());
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.activity_log, container, false);

        ListView logList = (ListView) root.findViewById(R.id.log_list);
        TextView noLog = (TextView) root.findViewById(R.id.no_log);

        ArrayList<LogItem> log = registry.getExecutionLog();

        if (log == null || log.size() == 0) {
            logList.setVisibility(View.GONE);
            noLog.setVisibility(View.VISIBLE);
        } else {
            logList.setAdapter(new LogAdapter(getActivity(), log));
            noLog.setVisibility(View.GONE);
        }

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

//    private class CompositeListAdapter extends ArrayAdapter<CompositeService> {
//
//        int selectedIndex = -1;
//        private Boolean[] expanded;
//
//        public CompositeListAdapter(Context context, ArrayList<CompositeService> items) {
//            super(context, R.layout.list_item_composite, items);
//            expanded = new Boolean[items.size()];
//            for (int i = 0; i < expanded.length; i++) {
//                expanded[i] = false;
//            }
//        }
//
//        @SuppressLint("InflateParams")
//        public View getView(final int position, View convertView, final ViewGroup parent) {
//
//
//            AppDescription app = components.get(0).getDescription().getApp();
//            if (app == null || app.iconLocation() == null) {
//                icon.setBackgroundResource(R.drawable.icon);
//            } else {
//                String iconLocation = app.iconLocation();
//                Bitmap b = localStorage.readIcon(iconLocation);
//                if (b != null) {
//                    icon.setImageBitmap(b);
//                } else {
//                    icon.setBackgroundResource(R.drawable.icon);
//                }
//            }
//

//
//
//            v.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    if (expanded[position]) {
//                        expanded[position] = false;
//                    } else {
//                        expanded[position] = true;
//                    }
//
//                    notifyDataSetChanged();
//                    return true;
//                }
//            });
//
//            return v;
//        }
//
//        private CompositeService getCurrentComposite() {
//            if (selectedIndex == -1) {
//                return null;
//            }
//
//            return composites.get(selectedIndex);
//        }
//    }
//
//    private class BackgroundCompositeLoader extends AsyncTask<Void, Void, ArrayList<CompositeService>> {
//
//        @Override
//        protected ArrayList<CompositeService> doInBackground(Void... arg0) {
//            try {
//                ServiceFactory sf = ServiceFactory.getInstance(registry, getActivity());
//                sf.setupServices();
//
//            } catch (JSONException e) {
//                Log.e(TAG, "JSONException - Failed to create services (CompositeListActivity) " + e.getMessage());
//            }
//
//            if (getActivity() == null) {
//                return new ArrayList<CompositeService>();
//            }
//
//            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
//
//            if (manager != null) {
//                boolean registryRunning = false;
//
//                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//                    if (RegistryService.class.getCanonicalName().equals(service.service.getClassName())) {
//                        registryRunning = true;
//                    }
//                }
//
//                if (!registryRunning) {
//                    Intent registryIntent = new Intent(getActivity(), RegistryService.class);
//                    getActivity().startService(registryIntent);
//                }
//            }
//
//            ArrayList<CompositeService> composites = registry.getComposites();
//            return composites;
//        }
//
//        protected void onPostExecute(ArrayList<CompositeService> composites) {
//            FragmentCompositeList.this.composites = composites;
//            listAdapter = new CompositeListAdapter(getActivity(), composites);
//            compositeList.setAdapter(listAdapter);
//
//            loader.setVisibility(View.GONE);
//
//            if (composites.size() > 0) {
//                compositeList.setVisibility(View.VISIBLE);
//                noComposites.setVisibility(View.GONE);
//            } else {
//                noComposites.setVisibility(View.VISIBLE);
//                compositeList.setVisibility(View.GONE);
//            }
//        }
//    }

    private class LogAdapter extends ArrayAdapter<LogItem> {
        private ArrayList<LogItem> items;

        public LogAdapter(Context context, ArrayList<LogItem> items) {
            super(context, R.layout.list_item_log, items);

            this.items = items;
        }

        // Add options so that they can actually do things with the log messages
        // Make the log look a bit nicer
        // Implement clearing of the log
        // Add more options to the log viewer - filtering, sorting, etc.

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (v == null) {
                v = vi.inflate(R.layout.list_item_log, null);
            }

            LogItem item = items.get(position);

            if (v == null)
                return null;

            TextView nameText = (TextView) v.findViewById(R.id.composite_name);
            if (nameText == null) // This way it doesn't die, but this way of fixing it doesn't seem to be a problem...
                return v;

            ImageView icon = (ImageView) v.findViewById(R.id.log_icon);
            TextView logStatus = (TextView) v.findViewById(R.id.log_status);
            if (item.getStatus() == LogItem.SUCCESS) {
                icon.setBackgroundResource(R.drawable.ic_assignment_turned_in_white_36dp);
                logStatus.setText("Success");
                logStatus.setTextColor(getResources().getColor(R.color.material_green));
            } else {
                icon.setBackgroundResource(R.drawable.ic_assignment_late_white_36dp);
                logStatus.setText("Fail");
                logStatus.setTextColor(getResources().getColor(R.color.material_red));
            }

            View backgroundView = v.findViewById(R.id.composite_item_bg);
            backgroundView.setBackgroundResource(item.getComposite().getColour(false));
            nameText.setTextColor(getResources().getColor(R.color.textColor));
            nameText.setText(item.getComposite().getName());

            TextView logTime = (TextView) v.findViewById(R.id.log_time);

            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(item.getEndTime());
            SimpleDateFormat sdf = new SimpleDateFormat("cccc d MMMM yyyy   HH:mm:ss");
            logTime.setText(sdf.format(cal.getTime()));

            LinearLayout componentContainer = (LinearLayout) v.findViewById(R.id.log_component_list);
            componentContainer.removeAllViews();

            for (ComponentLogItem componentItem : item.getComponentLogs()) {

                View vv = vi.inflate(R.layout.list_item_log_component, null);

                TextView componentName = (TextView) vv.findViewById(R.id.log_component_name);
                componentName.setText(componentItem.getComponent().getDescription().getName());

                // TODO Something was null above here, make sure it's looking it up right from the database
                // TODO Put the message in the log too

                ImageView componentIcon = (ImageView) vv.findViewById(R.id.log_component_icon);
                if (componentItem.getStatus() == LogItem.SUCCESS) {
                    componentIcon.setImageResource(R.drawable.ic_done_white_18dp);
                } else {
                    componentIcon.setImageResource(R.drawable.ic_clear_white_18dp);
                }

                TextView componentTime = (TextView) vv.findViewById(R.id.log_component_time);
                TextView componentStatus = (TextView) vv.findViewById(R.id.log_component_status);

                componentStatus.setText(getMessage(componentItem.getStatus()));
                cal.setTimeInMillis(componentItem.getTime());
                componentTime.setText(sdf.format(cal.getTime()));

                componentContainer.addView(vv);
            }

            return v;
        }

    }

    private String getMessage(int logStatus) {
        switch (logStatus) {
            case LogItem.COMPONENT_FAIL:
                return "There was an error in the component";

            case LogItem.MESSAGE_FAIL:
            case LogItem.ORCH_FAIL:
                return "An error occurred talking to the component";

            case LogItem.NETWORK_FAIL:
                return "There was an error with the network";

            case LogItem.OTHER_FAIL:
                return "Something bad happened";

            case LogItem.PARAM_STOP:
                return "User parameters stopped the component";

            default: // I think this is the only case left over
                return "Success";
        }
    }
}
