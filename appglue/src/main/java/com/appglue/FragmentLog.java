package com.appglue;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.library.LogItem;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

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

            LogItem log = items.get(position);

            TextView logTitle = (TextView) v.findViewById(R.id.log_title);
            logTitle.setText(log.getComposite().getName());

            TextView logTime = (TextView) v.findViewById(R.id.log_time);
            logTime.setText("");

            TextView logMessage = (TextView) v.findViewById(R.id.log_message);
            logMessage.setText(log.getMessage());

            return v;
        }

    }
}
