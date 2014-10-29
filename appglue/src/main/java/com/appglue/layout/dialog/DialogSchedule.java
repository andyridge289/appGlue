package com.appglue.layout.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.appglue.FragmentSchedule;
import com.appglue.R;
import com.appglue.engine.Schedule;
import com.appglue.engine.Scheduler;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class DialogSchedule extends AlertDialog {

    private FragmentSchedule fragment;
    private Schedule item;

    private View intervalButton;
    private View timeButton;
    private View intervalContainer;
    private View timeContainer;

    private Activity activity;

    public static enum WeekDay {
        MON(Calendar.MONDAY, "Monday"),
        TUE(Calendar.TUESDAY, "Tuesday"),
        WED(Calendar.WEDNESDAY, "Wednesday"),
        THU(Calendar.THURSDAY, "Thursday"),
        FRI(Calendar.FRIDAY, "Friday"),
        SAT(Calendar.SATURDAY, "Saturday"),
        SUN(Calendar.SUNDAY, "Sunday");

        public int index;
        public String name;

        WeekDay(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    public static WeekDay getWeekDay(int index) {
        for (int i = 0; i < WeekDay.values().length; i++) {
            if (WeekDay.values()[i].index == index)
                return WeekDay.values()[i];
        }

        return WeekDay.MON;
    }

    public DialogSchedule(Activity context, final FragmentSchedule fragment, final Schedule item, long compositeId) {
        super(context);

        this.activity = context;
        this.item = item;

        final Registry registry = Registry.getInstance(activity);

        LayoutInflater inflater = context.getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_schedule, null);
        setView(root);

        final Spinner compositeNameSpinner = (Spinner) root.findViewById(R.id.composite_name_spinner);
        ArrayList<CompositeService> composites = registry.getComposites();
        if (composites.size() == 0) {
            Toast.makeText(activity, "No composites to schedule", Toast.LENGTH_SHORT).show();
            return;
        }

        compositeNameSpinner.setAdapter(new CompositeSpinnerAdapter(activity, composites));
        compositeNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CompositeService composite = (CompositeService) compositeNameSpinner.getSelectedItem();
                item.setComposite(composite);
                if (LOG)
                    Log.d(TAG, "SCHEDULE: Set composite " + composite.getID() + "(" + composite.getName() + ")");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (item.getComposite() != null) {
            int index = ((CompositeSpinnerAdapter) compositeNameSpinner.getAdapter()).getIndexForId(item.getComposite().getID());
            compositeNameSpinner.setSelection(index);
        } else if (compositeId == -1) {
            compositeNameSpinner.setSelection(0);
        } else {
            int index = ((CompositeSpinnerAdapter) compositeNameSpinner.getAdapter()).getIndexForId(compositeId);
            compositeNameSpinner.setSelection(index);
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
        if (item.getScheduleType() == Schedule.ScheduleType.INTERVAL) {
            selectorClick(false);
        } else {
            selectorClick(true);
        }

        final View spinnerRow = root.findViewById(R.id.spinner_row);
        final View timeRow = root.findViewById(R.id.time_row);
        final EditText minuteEdit = (EditText) root.findViewById(R.id.minute_edit);
        final TextView timeText = (TextView) root.findViewById(R.id.time_text);
        final TextView timeIntervalText = (TextView) root.findViewById(R.id.time_interval_text);

        final Spinner weekSpinner = (Spinner) root.findViewById(R.id.week_spinner);
        weekSpinner.setAdapter(new WeekDayAdapter(activity, WeekDay.values()));
        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WeekDay day = (WeekDay) weekSpinner.getSelectedItem();
                item.setDayOfWeek(day.index);
                if (LOG) Log.d(TAG, "SCHEDULE: Set day of week - " + day.name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        weekSpinner.setSelection(((WeekDayAdapter) weekSpinner.getAdapter()).getPosition(getWeekDay(item.getDayOfWeek())));

        final Spinner monthSpinner = (Spinner) root.findViewById(R.id.month_spinner);
        monthSpinner.setAdapter(new StringSpinnerAdapter(activity, fragment.monthDays));
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item.setDayOfMonth(position + 1);
                if (LOG) Log.d(TAG, "SCHEDULE: Set day of month - " + position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        monthSpinner.setSelection(item.getDayOfMonth() - 1);

        final Spinner timePeriodSpinner = (Spinner) root.findViewById(R.id.time_period_spinner);
        Schedule.TimePeriod[] periods = new Schedule.TimePeriod[]{
                Schedule.TimePeriod.HOUR,
                Schedule.TimePeriod.DAY,
                Schedule.TimePeriod.WEEK,
                Schedule.TimePeriod.MONTH
        };
        timePeriodSpinner.setAdapter(new TimePeriodAdapter(activity, periods));
        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Schedule.TimePeriod tp = (Schedule.TimePeriod) timePeriodSpinner.getSelectedItem();
                if (tp.equals(Schedule.TimePeriod.DAY)) {
                    spinnerRow.setVisibility(View.GONE);
                } else {
                    spinnerRow.setVisibility(View.VISIBLE);

                    if (tp.equals(Schedule.TimePeriod.HOUR)) {
                        minuteEdit.setVisibility(View.VISIBLE);
                        weekSpinner.setVisibility(View.GONE);
                        monthSpinner.setVisibility(View.GONE);
                    } else {
                        minuteEdit.setVisibility(View.GONE);
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

                item.setTimePeriod(tp);
                if (LOG) Log.d(TAG, "Set time period " + tp.name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timePeriodSpinner.setSelection(((TimePeriodAdapter) timePeriodSpinner.getAdapter()).getPosition(item.getTimePeriod()));

        minuteEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // This needs to make sure what is entered is between 0 and 59 inclusive
                String contents = minuteEdit.getText().toString();
                int value = Integer.parseInt(contents);
                if (value > 59) {
                    Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
                    value = 59;
                    minuteEdit.setText("59");
                }
                item.setMinute(value);
                return false;
            }
        });
        minuteEdit.setTextColor(context.getResources().getColor(R.color.textColor));
        minuteEdit.setText("" + item.getMinute());

        final TextView timeTextTime = (TextView) root.findViewById(R.id.time_text_time);
        Button setTimeButton = (Button) root.findViewById(R.id.time_button);
        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hh = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
                        String mm = minute < 10 ? "0" + minute : "" + minute;
                        timeTextTime.setText(hh + ":" + mm);

                        item.setHour(hourOfDay);
                        item.setMinute(minute);

                    }
                }, 12, 0, true);
                tpd.show();
            }
        });
        String hh = item.getHour() < 10 ? "0" + item.getHour() : "" + item.getHour();
        String mm = item.getMinute() < 10 ? "0" + item.getMinute() : "" + item.getMinute();
        timeTextTime.setText(hh + ":" + mm);

        final EditText numeralEdit = (EditText) root.findViewById(R.id.numeral_edit);
        numeralEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int num = Integer.parseInt(numeralEdit.getText().toString());
                item.setNumeral(num);
                return false;
            }
        });
        numeralEdit.setText("" + item.getNumeral());

        final Spinner intervalSpinner = (Spinner) root.findViewById(R.id.interval_spinner);
        Schedule.Interval[] intervals = new Schedule.Interval[]{
                Schedule.Interval.MINUTE,
                Schedule.Interval.HOUR,
                Schedule.Interval.DAY
        };
        intervalSpinner.setAdapter(new IntervalAdapter(activity, intervals));
        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Schedule.Interval interval = (Schedule.Interval) intervalSpinner.getSelectedItem();
                item.setInterval(interval);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        intervalSpinner.setSelection(((IntervalAdapter) intervalSpinner.getAdapter()).getPosition(item.getInterval()));

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
                item.setLastExecuteTime(now);
            }
        });
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(item.getLastExecuted());
        hh = "" + cal.get(Calendar.HOUR_OF_DAY);
        mm = "" + cal.get(Calendar.MINUTE);
        hh = hh.length() == 1 ? "0" + hh : hh;
        mm = mm.length() == 1 ? "0" + mm : mm;
        intervalTimeText.setText(hh + ":" + mm);


        intervalStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hh = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
                        String mm = minute < 10 ? "0" + minute : "" + minute;
                        intervalTimeText.setText(hh + ":" + mm);
                        Calendar cal = new GregorianCalendar();
                        cal.setTimeInMillis(System.currentTimeMillis());
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal.set(Calendar.MINUTE, minute);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                    }
                }, 12, 0, true);
                tpd.show();
            }
        });

        View doneButton = root.findViewById(R.id.dialog_io_positive);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Scheduler scheduler = new Scheduler(activity);

                if (item.getID() == -1) {
//                    item.calculateNextExecute(System.currentTimeMillis());
                    registry.add(item);
//                    scheduler.schedule(item);
                } else {

//                    long oldTime = item.getNextExecute();
//                    item.calculateNextExecute(System.currentTimeMillis());
//                    if (item.getNextExecute() != oldTime) {
//                        // Increment the index so that the old one is ignored.
//                        item.setExecutionNum(item.getExecutionNum() + 1);
//                        scheduler.schedule(item);
//                    }
//
                    // Update it in the database
                    registry.update(item);
                }

                if (item.isEnabled()) {
                    Schedule s = registry.getSchedule(item.getID());
                    item.calculateNextExecute(System.currentTimeMillis());
                    item.setExecutionNum(s.getExecutionNum() + 1);
                    scheduler.schedule(item);
                }

                fragment.dialogDone();
                dismiss();
            }
        });

        View negativeButton = root.findViewById(R.id.dialog_io_negative);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancel();
            }
        });

        View deleteButton = root.findViewById(R.id.dialog_io_neutral);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registry.delete(item);
                fragment.dialogDone();
                dismiss();
            }
        });
    }

    private void selectorClick(boolean timeSelector) {
        intervalButton.setSelected(!timeSelector);
        timeButton.setSelected(timeSelector);
        if (timeSelector) {
            intervalContainer.setVisibility(View.GONE);
            timeContainer.setVisibility(View.VISIBLE);
            item.setScheduleType(Schedule.ScheduleType.TIME);
            if (LOG) Log.d(TAG, "SCHEDULE: Set type - time");
        } else {
            intervalContainer.setVisibility(View.VISIBLE);
            timeContainer.setVisibility(View.GONE);
            item.setScheduleType(Schedule.ScheduleType.INTERVAL);
            if (LOG) Log.d(TAG, "SCHEDULE: Set type - interval");
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
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            CompositeService item = getItem(position);
            TextView tv = ((TextView) v.findViewById(android.R.id.text1));
            tv.setText(item.getName());
            tv.setBackgroundResource(item.getColour(false));

            return v;
        }

        public int getIndexForId(long id) {
            for (int i = 0; i < getCount(); i++) {
                if (getItem(i).getID() == id) {
                    return i;
                }
            }

            return -1;
        }
    }

    private class WeekDayAdapter extends ArrayAdapter<WeekDay> {

        public WeekDayAdapter(Context context, WeekDay[] items) {
            super(context, android.R.layout.simple_list_item_1, items);
        }

        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            WeekDay item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.name);
            return v;
        }

        @Override
        @SuppressLint("InflateParams")
        public View getDropDownView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            WeekDay item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.name);
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
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            String item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item);
            return v;
        }
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
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            Schedule.TimePeriod item = getItem(position);
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.name);
            return v;
        }
    }

}