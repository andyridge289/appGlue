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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.AppGlueFragment;
import com.appglue.MainActivity;
import com.appglue.R;
import com.appglue.library.ComponentLogItem;
import com.appglue.library.LogItem;
import com.appglue.serviceregistry.Registry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FragmentLog extends Fragment implements AppGlueFragment {
    private Registry registry;

    public static FragmentLog create() {
        return new FragmentLog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MainActivity) activity).onSectionAttached(MainActivity.Page.LOG);

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

    // TODO Do we want to put the time that the component started?
    // TODO And maybe show them the data that was given if they click on one of the components?

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

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public String onCreateOptionsMenu(Menu menu) {
        return "Log";
    }

    private class LogAdapter extends ArrayAdapter<LogItem> {
        private ArrayList<LogItem> items;

        public LogAdapter(Context context, ArrayList<LogItem> items) {
            super(context, R.layout.list_item_log, items);

            this.items = items;
        }

        // TODO Add options so that they can actually do things with the log messages
        // TODO Implement clearing of the log
        // TODO Add more options to the log viewer - filtering, sorting, etc.

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
            TextView logMessage = (TextView) v.findViewById(R.id.log_message);

            if (item.getStatus() == LogItem.SUCCESS) {
                icon.setBackgroundResource(R.drawable.ic_assignment_turned_in_white_36dp);
                logStatus.setText("Success");
                logStatus.setTextColor(getResources().getColor(R.color.material_green));
                logMessage.setText("");
            } else {
                icon.setBackgroundResource(R.drawable.ic_assignment_late_white_36dp);
                logStatus.setText("Fail");
                logStatus.setTextColor(getResources().getColor(R.color.material_red));
                logMessage.setText(getCompositeMessage(item.getStatus()));
            }

            if (item.getComposite() == null) {
                // TODO It's a generic trigger fail so there's not much information about it
                return v;
            }

            View backgroundView = v.findViewById(R.id.composite_item_bg);
            backgroundView.setBackgroundResource(item.getComposite().getColour(false));
            nameText.setTextColor(getResources().getColor(R.color.textColor));
            nameText.setText(item.getComposite().getName());

            TextView logTime = (TextView) v.findViewById(R.id.log_start_time);
            TextView duration = (TextView) v.findViewById(R.id.log_duration);

            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(item.getStartTime());
            SimpleDateFormat sdf = new SimpleDateFormat("ccc d MMMM yyyy   HH:mm:ss");
            logTime.setText(sdf.format(cal.getTime()));

            long time = (item.getEndTime() - item.getStartTime()) / 1000;
            duration.setText("in " + time + "s");

            LinearLayout componentContainer = (LinearLayout) v.findViewById(R.id.log_component_list);
            componentContainer.removeAllViews();

            for (ComponentLogItem componentItem : item.getComponentLogs()) {

                View vv = vi.inflate(R.layout.list_item_log_component, null);

                TextView componentName = (TextView) vv.findViewById(R.id.log_component_name);
                componentName.setText(componentItem.getComponent().getDescription().getName());

                // TODO Put the message in the log too

                ImageView componentIcon = (ImageView) vv.findViewById(R.id.log_component_icon);
                if (componentItem.getStatus() == LogItem.SUCCESS) {
                    componentIcon.setImageResource(R.drawable.ic_done_white_18dp);
                } else {
                    componentIcon.setImageResource(R.drawable.ic_clear_white_18dp);
                }

                TextView componentTime = (TextView) vv.findViewById(R.id.log_component_time);
                TextView componentStatus = (TextView) vv.findViewById(R.id.log_component_status);

                componentStatus.setText(getComponentMessage(componentItem.getStatus()));
                cal.setTimeInMillis(componentItem.getTime());
                componentTime.setText(sdf.format(cal.getTime()));

                componentContainer.addView(vv);
            }

            return v;
        }

    }

    private String getCompositeMessage(int logStatus) {
        switch (logStatus) {
            case LogItem.COMPONENT_FAIL:
                return "There was an error in a component";

            case LogItem.TRIGGER_FAIL:
                return "Trigger in the wrong position";

            case LogItem.MESSAGE_FAIL:
            case LogItem.ORCH_FAIL:
                return "An error occurred talking to a component";

            case LogItem.NETWORK_FAIL:
                return "There was an error with the network";

            case LogItem.OTHER_FAIL:
                return "Something bad happened";

            case LogItem.PARAM_STOP:
                return "User parameters stopped a component";

            default: // I think this is the only case left over
                return "";
        }
    }

    private String getComponentMessage(int logStatus) {
        switch (logStatus) {
            case LogItem.COMPONENT_FAIL:
                return "There was an error in the component";

            case LogItem.TRIGGER_FAIL:
                return "Trigger in the wrong position";

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
