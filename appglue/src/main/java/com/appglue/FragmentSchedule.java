package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.appglue.engine.Schedule;
import com.appglue.engine.Scheduler;
import com.appglue.engine.description.CompositeService;
import com.appglue.layout.FloatingActionButton;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FragmentSchedule extends Fragment {

    private Registry registry;
    private Schedule current;
    private long compositeId = -1;

    private View intervalButton;
    private View timeButton;
    private View intervalContainer;
    private View timeContainer;

    private ListView scheduleList;
    private ScheduleAdapter adapter;
    private ArrayList<Schedule> sch;

    private String[] weekDays = new String[] {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };
    String[] monthDays = new String[28];

    public static Fragment create() {
        return new FragmentSchedule();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((ActivityAppGlue) activity).onSectionAttached(ActivityAppGlue.Page.SCHEDULE);
        registry = Registry.getInstance(getActivity());
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        for(int i = 0; i < monthDays.length; i++) {
            monthDays[i] = "" + (i + 1);
        }

    }

    // FIXME When a shcedule is enabled, we need to schedule it in the alarm manager/scheduler
    // FIXME When it's disabled we need to de-schedule it. OR we record in the database when we schedule, and only add to the alarm manager when that flag isn't set

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_schedule, container, false);

        final View newScheduleView = root.findViewById(R.id.new_schedule);
        final TextView noSchedule = (TextView) root.findViewById(R.id.no_schedule);

        sch = registry.getScheduledComposites();
        scheduleList = (ListView) root.findViewById(R.id.schedule_list);
        adapter = new ScheduleAdapter(getActivity(), sch);
        scheduleList.setAdapter(adapter);
        if (sch.size() == 0) {
            scheduleList.setVisibility(View.GONE);
            noSchedule.setVisibility(View.VISIBLE);
        } else {
            scheduleList.setVisibility(View.VISIBLE);
            noSchedule.setVisibility(View.GONE);
        }

        final Spinner compositeNameSpinner = (Spinner) root.findViewById(R.id.composite_name_spinner);
        compositeNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CompositeService composite = (CompositeService) compositeNameSpinner.getSelectedItem();
                current.setComposite(composite);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<Schedule> composites = registry.getScheduledComposites();

        if (composites.size() == 0) {
            noSchedule.setVisibility(View.VISIBLE);
            scheduleList.setVisibility(View.GONE);
        } else {
            noSchedule.setVisibility(View.GONE);
            scheduleList.setVisibility(View.VISIBLE);
        }

        // Setup all the views that deal with the adding of a new schedule item
        intervalButton = root.findViewById(R.id.interval_selector);
        timeButton = root.findViewById(R.id.time_selector);
        intervalContainer = root.findViewById(R.id.interval_container);
        timeContainer = root.findViewById(R.id.time_container);

        intervalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorClick(false);
            }
        });
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorClick(true);
            }
        });

        final View spinnerRow = root.findViewById(R.id.spinner_row);
        final View timeRow = root.findViewById(R.id.time_row);
        final EditText timeEdit = (EditText) root.findViewById(R.id.time_edit);
        final TextView timeText = (TextView) root.findViewById(R.id.time_text);
        final TextView timeIntervalText = (TextView) root.findViewById(R.id.time_interval_text);

        final Spinner weekSpinner = (Spinner) root.findViewById(R.id.week_spinner);
        weekSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), weekDays));
        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                current.setDayOfWeek(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        weekSpinner.setSelection(0);

        final Spinner monthSpinner = (Spinner) root.findViewById(R.id.month_spinner);
        monthSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), monthDays));
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                current.setDayOfMonth(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        monthSpinner.setSelection(0);

        final Spinner timePeriodSpinner = (Spinner) root.findViewById(R.id.time_period_spinner);
        Schedule.TimePeriod[] periods = new Schedule.TimePeriod[] {
                Schedule.TimePeriod.HOUR,
                Schedule.TimePeriod.DAY,
                Schedule.TimePeriod.WEEK,
                Schedule.TimePeriod.MONTH
        };
        timePeriodSpinner.setAdapter(new TimePeriodAdapter(getActivity(), periods));
        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Schedule.TimePeriod tp = (Schedule.TimePeriod) timePeriodSpinner.getSelectedItem();
                if (tp.equals(Schedule.TimePeriod.DAY)) {
                    spinnerRow.setVisibility(View.GONE);
                } else {
                    spinnerRow.setVisibility(View.VISIBLE);

                    if(tp.equals(Schedule.TimePeriod.HOUR)) {
                        timeEdit.setVisibility(View.VISIBLE);
                        weekSpinner.setVisibility(View.GONE);
                        monthSpinner.setVisibility(View.GONE);
                    } else {
                        timeEdit.setVisibility(View.GONE);
                        if (tp.equals(Schedule.TimePeriod.WEEK)) {
                            weekSpinner.setVisibility(View.VISIBLE);
                            monthSpinner.setVisibility(View.GONE);
                        } else { // It's month
                            weekSpinner.setVisibility(View.GONE);
                            monthSpinner.setVisibility(View.VISIBLE);
                        }
                    }
                }

                if (tp.equals(Schedule.TimePeriod.HOUR)) {
                    timeRow.setVisibility(View.GONE);
                    timeIntervalText.setVisibility(View.VISIBLE);
                } else {
                    timeRow.setVisibility(View.VISIBLE);
                    timeIntervalText.setVisibility(View.GONE);
                }

                if (tp.equals(Schedule.TimePeriod.HOUR) || tp.equals(Schedule.TimePeriod.DAY)) {
                    timeText.setText("at");
                } else {
                    timeText.setText("on");
                }

                current.setTimePeriod(tp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timePeriodSpinner.setSelection(0);

        timeEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // This needs to make sure what is entered is between 0 and 59 inclusive
                String contents = timeEdit.getText().toString();
                int value = Integer.parseInt(contents);
                if (value > 59) {
                    Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                    value = 59;
                    timeEdit.setText("59");
                }
                current.setMinute(value);
                return false;
            }
        });

        final TextView timeTextTime = (TextView) root.findViewById(R.id.time_text_time);
        Button setTimeButton = (Button) root.findViewById(R.id.time_button);
        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hh = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
                        String mm = minute < 10 ? "0" + minute : "" + minute;
                        timeTextTime.setText(hh + ":" + mm);

                        current.setHour(hourOfDay);
                        current.setMinute(minute);

                    }
                }, 12, 0, true);
                tpd.show();
            }
        });

        final EditText numeralEdit = (EditText) root.findViewById(R.id.numeral_edit);
        numeralEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int num = Integer.parseInt(numeralEdit.getText().toString());
                current.setNumeral(num);
                return false;
            }
        });

        // TODO Setting to show toast on successful execution
        // TODO Setting to show toast on successful execution of trigger
        // TODO Setting for disabling things that cost money

        final Spinner intervalSpinner = (Spinner) root.findViewById(R.id.interval_spinner);
        Schedule.Interval[] intervals = new Schedule.Interval[] {
                Schedule.Interval.MINUTE,
                Schedule.Interval.HOUR,
                Schedule.Interval.DAY
        };
        intervalSpinner.setAdapter(new IntervalAdapter(getActivity(), intervals));
        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Schedule.Interval interval = (Schedule.Interval) intervalSpinner.getSelectedItem();
                current.setInterval(interval);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final TextView intervalTimeText = (TextView) root.findViewById(R.id.interval_time_text);
        final Button nowButton = (Button) root.findViewById(R.id.now_button);
        final Button intervalStartButton = (Button) root.findViewById(R.id.interval_settime);
        nowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(now);
                String hh = "" + cal.get(Calendar.HOUR_OF_DAY);
                String mm = "" + cal.get(Calendar.MINUTE);
                hh = hh.length() == 1 ? "0" + hh : hh;
                mm = mm.length() == 1 ? "0" + mm : mm;
                intervalTimeText.setText(hh + ":" + mm);
                current.setLastExecuteTime(now);
            }
        });
        intervalStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hh = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
                        String mm = minute < 10 ? "0" + minute : "" + minute;
                        intervalTimeText.setText(hh + ":" + mm);
                        Calendar cal = new GregorianCalendar();
                        cal.setTimeInMillis(System.currentTimeMillis());
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal.set(Calendar.MINUTE, minute); // TODO This might need to be re-worked
                     }
                }, 12, 0, true);
                tpd.show();
            }
        });

        View doneButton = root.findViewById(R.id.schedule_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO check the values are sane

                registry.addSchedule(current);
                Scheduler scheduler = Scheduler.getInstance(getActivity());
                scheduler.schedule(current, System.currentTimeMillis());
                current = null;

                sch = registry.getScheduledComposites();
                adapter = new ScheduleAdapter(getActivity(), sch);
                scheduleList.setAdapter(adapter);

                scheduleList.setVisibility(View.VISIBLE);
                newScheduleView.setVisibility(View.GONE);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<CompositeService> composites = registry.getComposites();
                if (composites.size() == 0) {
                    Toast.makeText(getActivity(), "No composites to schedule", Toast.LENGTH_SHORT).show();
                    return;
                }

                current = new Schedule();
                compositeNameSpinner.setAdapter(new CompositeSpinnerAdapter(getActivity(), composites));

                if (compositeId == -1) {
                    compositeNameSpinner.setSelection(0);
                } else {
                    compositeNameSpinner.setSelection(0);
                    // TODO Lookup the right one
                }

                if (current.getScheduleType() == Schedule.ScheduleType.TIME) {
                    selectorClick(true);
                } else {
                    selectorClick(false);
                }

                noSchedule.setVisibility(View.GONE);
                newScheduleView.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    private void selectorClick(boolean timeSelector) {
        intervalButton.setSelected(!timeSelector);
        timeButton.setSelected(timeSelector);
        if (timeSelector) {
            intervalContainer.setVisibility(View.GONE);
            timeContainer.setVisibility(View.VISIBLE);
        } else {
            intervalContainer.setVisibility(View.VISIBLE);
            timeContainer.setVisibility(View.GONE);
        }
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

    private class TimePeriodAdapter extends ArrayAdapter<Schedule.TimePeriod> {

        public TimePeriodAdapter(Context context, Schedule.TimePeriod[] items) {
            super(context, android.R.layout.simple_list_item_1, items);
        }

        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            Schedule.TimePeriod item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.name);
            return v;
        }

        @Override
        @SuppressLint("InflateParams")
        public View getDropDownView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            Schedule.TimePeriod item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.name);
            return v;
        }
    }

    private class CompositeSpinnerAdapter extends ArrayAdapter<CompositeService> {

        public CompositeSpinnerAdapter(Context context, ArrayList<CompositeService> items) {
            super(context, android.R.layout.simple_list_item_1, items);
        }

        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            CompositeService item = getItem(position);
            TextView tv = ((TextView) v.findViewById(android.R.id.text1));
            tv.setText(item.getName());
            tv.setBackgroundResource(item.getColour(false));

            return v;
        }

        @Override
        @SuppressLint("InflateParams")
        public View getDropDownView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            CompositeService item = getItem(position);
            TextView tv = ((TextView) v.findViewById(android.R.id.text1));
            tv.setText(item.getName());
            tv.setBackgroundResource(item.getColour(false));

            return v;
        }
    }

    private class IntervalAdapter extends ArrayAdapter<Schedule.Interval> {

        public IntervalAdapter(Context context, Schedule.Interval[] items) {
            super(context, android.R.layout.simple_list_item_1, items);
        }

        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            Schedule.Interval item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.name);
            return v;
        }

        @Override
        @SuppressLint("InflateParams")
        public View getDropDownView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            Schedule.Interval item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.name);
            return v;
        }
    }

    private class StringSpinnerAdapter extends ArrayAdapter<String> {
        public StringSpinnerAdapter(Context context, String[] items) {
            super(context, android.R.layout.simple_list_item_1, items);
        }

        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            String item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item);
            return v;
        }

        @Override
        @SuppressLint("InflateParams")
        public View getDropDownView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            String item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item);
            return v;
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<Schedule> {

        public ScheduleAdapter(Context context, ArrayList<Schedule> items) {
            super(context, R.layout.list_item_schedule, items);
        }

        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item_schedule, null);
            }

            final Schedule item = getItem(position);

            TextView compositeName = (TextView) v.findViewById(R.id.composite_name);
            compositeName.setText(item.getComposite().getName());

            View timeContainer = v.findViewById(R.id.time_container);
            View intervalContainer = v.findViewById(R.id.interval_container);
            if (item.getScheduleType() == Schedule.ScheduleType.TIME) {
                timeContainer.setVisibility(View.VISIBLE);
                intervalContainer.setVisibility(View.GONE);
            } else {
                timeContainer.setVisibility(View.GONE);
                intervalContainer.setVisibility(View.VISIBLE);
            }

            TextView timePeriod = (TextView) v.findViewById(R.id.time_period);
            timePeriod.setText(item.getTimePeriod().name);

            View rowDay = v.findViewById(R.id.period_row_day);
            if (item.getTimePeriod().equals(Schedule.TimePeriod.DAY)) {
                rowDay.setVisibility(View.GONE);
            } else {
                rowDay.setVisibility(View.VISIBLE);
            }

            TextView periodDay = (TextView) v.findViewById(R.id.time_period_days);
            if (item.getTimePeriod().equals(Schedule.TimePeriod.WEEK)) {
                periodDay.setText(weekDays[item.getDayOfWeek()]);
            } else if (item.getTimePeriod().equals(Schedule.TimePeriod.MONTH)) {
                periodDay.setText(monthDays[item.getDayOfMonth()]);
            } else if (item.getTimePeriod().equals(Schedule.TimePeriod.DAY)) {
                periodDay.setText("" + item.getMinute());
            }

            View rowTime = v.findViewById(R.id.period_row_time);
            if (item.getTimePeriod().equals(Schedule.TimePeriod.HOUR)) {
                rowTime.setVisibility(View.GONE);
            } else {
                rowTime.setVisibility(View.VISIBLE);
            }

            TextView periodTime = (TextView) v.findViewById(R.id.time_period_time);
            periodTime.setText(item.getHour() + ":" + item.getMinute());

            TextView intervalNumeral = (TextView) v.findViewById(R.id.interval_numeral);
            intervalNumeral.setText("" + item.getNumeral());

            TextView intervalInterval = (TextView) v.findViewById(R.id.interval_interval);
            intervalInterval.setText(item.getInterval().name);

            TextView intervalStart = (TextView) v.findViewById(R.id.interval_start);
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(item.getLastExecuted()); // TODO This isn't actually right, but it won't change how stuff works
            int minute = cal.get(Calendar.MINUTE);
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            String dayEnd = "th";
            switch(dayOfMonth) {
                case 1:
                case 21:
                case 31:
                    dayEnd = "st";
                    break;

                case 2:
                case 22:
                    dayEnd = "nd";
                    break;
            }
            int month = cal.get(Calendar.MONTH);
            String[] months = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

            int year = cal.get(Calendar.YEAR);
            intervalStart.setText(String.format("%s %d%s %s %d -- %d:%d", weekDays[dayOfWeek], dayOfMonth, dayEnd, months[month], year, hour, minute));

            // TODO Implement this when we've actually worked out how to to scheduling proper
            TextView next = (TextView) v.findViewById(R.id.next_time);

            Switch enabledSwitch = (Switch) v.findViewById(R.id.enabled_switch);
            enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.setEnabled(isChecked);
                    notifyDataSetChanged();
                }
            });
            enabledSwitch.setChecked(item.isEnabled());

            return v;
        }
    }


}
