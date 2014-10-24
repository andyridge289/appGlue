package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.engine.Schedule;
import com.appglue.engine.Scheduler;
import com.appglue.layout.FloatingActionButton;
import com.appglue.layout.dialog.DialogSchedule;
import com.appglue.serviceregistry.Registry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FragmentSchedule extends Fragment {

    private Registry registry;
    private Schedule current;
    private long compositeId = -1;

    private ListView scheduleList;
    private TextView noSchedule;

    private ScheduleAdapter adapter;
    private ArrayList<Schedule> sch;

    public String[] monthDays = new String[28];

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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_schedule, container, false);

        noSchedule = (TextView) root.findViewById(R.id.no_schedule);

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

        ArrayList<Schedule> composites = registry.getScheduledComposites();

        if (composites.size() == 0) {
            noSchedule.setVisibility(View.VISIBLE);
            scheduleList.setVisibility(View.GONE);
        } else {
            noSchedule.setVisibility(View.GONE);
            scheduleList.setVisibility(View.VISIBLE);
        }

        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogSchedule ds = new DialogSchedule(getActivity(), FragmentSchedule.this, new Schedule(), compositeId);
                ds.show();
            }
        });

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

    public void dialogDone() {
        sch = registry.getScheduledComposites();
        adapter = new ScheduleAdapter(getActivity(), sch);
        scheduleList.setAdapter(adapter);

        if (sch.size() > 0) {
            scheduleList.setVisibility(View.VISIBLE);
            noSchedule.setVisibility(View.GONE);
        } else {
            scheduleList.setVisibility(View.GONE);
            noSchedule.setVisibility(View.VISIBLE);
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

            View compositeBg = v.findViewById(R.id.composite_item_bg);
            if (item.isEnabled()) {
                compositeBg.setBackgroundResource(item.getComposite().getColour(false));
            } else {
                compositeBg.setBackgroundResource(R.color.card_disabled);
            }

            ImageView icon = (ImageView) v.findViewById(R.id.schedule_icon);
            if (item.isEnabled()) {
                icon.setBackgroundResource(R.drawable.ic_alarm_black_36dp);
            } else {
                icon.setBackgroundResource(R.drawable.ic_alarm_off_grey600_36dp);
            }

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
            if (item.getTimePeriod().equals(Schedule.TimePeriod.DAY) ||
                    item.getTimePeriod().equals(Schedule.TimePeriod.HOUR)) {
                rowDay.setVisibility(View.GONE);
            } else {
                rowDay.setVisibility(View.VISIBLE);
            }

            TextView periodDay = (TextView) v.findViewById(R.id.time_period_days);
            if (item.getTimePeriod().equals(Schedule.TimePeriod.WEEK)) {
                periodDay.setText(DialogSchedule.getWeekDay(item.getDayOfWeek()).name);
            } else if (item.getTimePeriod().equals(Schedule.TimePeriod.MONTH)) {
                int dayOfMonth = item.getDayOfMonth();
                String dayEnd = "th";
                switch (dayOfMonth) {
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
                periodDay.setText(dayOfMonth + dayEnd + " of the month");
            } else if (item.getTimePeriod().equals(Schedule.TimePeriod.DAY)) {
                periodDay.setText("" + item.getMinute());
            }

            TextView periodTime = (TextView) v.findViewById(R.id.time_period_time);
            if (item.getTimePeriod().equals(Schedule.TimePeriod.HOUR)) {
                periodTime.setText("" + item.getMinute() + " minutes");
            } else {
                String strMinute = item.getMinute() < 10 ? "0" + item.getMinute() : "" + item.getMinute();
                periodTime.setText(item.getHour() + ":" + strMinute);
            }

            TextView intervalNumeral = (TextView) v.findViewById(R.id.interval_numeral);
            intervalNumeral.setText("" + item.getNumeral());

            TextView intervalInterval = (TextView) v.findViewById(R.id.interval_interval);
            intervalInterval.setText(item.getInterval().name);

            TextView intervalStart = (TextView) v.findViewById(R.id.interval_start);
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(item.getLastExecuted());
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
            intervalStart.setText(String.format("%s %d%s %s %d at %d:%d", DialogSchedule.getWeekDay(item.getDayOfWeek()).name, dayOfMonth, dayEnd, months[month], year, hour, minute));

            TextView next = (TextView) v.findViewById(R.id.next_time);
            if (item.getNextExecute() == -1L) {
                next.setText(" - ");
            } else {
                cal.setTimeInMillis(item.getNextExecute());
                SimpleDateFormat sdf = new SimpleDateFormat("cccc d MMMM yyyy   HH:mm");

                next.setText(sdf.format(cal.getTime()));
            }

            // TODO Can this be red instead?
            final SwitchCompat enabledSwitch = (SwitchCompat) v.findViewById(R.id.enabled_switch);
            enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    item.setEnabled(isChecked);
                    if (isChecked) {
                        if (!item.getComposite().isEnabled() && item.getComposite().canEnable()) {
                            enabledSwitch.setChecked(false);
                            enableDialog(item, enabledSwitch);
                            return;
                        } else if (!item.getComposite().canEnable()) {
                            Toast.makeText(getContext(), "Can't enable until you fix " + item.getComposite().getName(), Toast.LENGTH_SHORT).show();
                        }

                        if (item.getScheduleType() == Schedule.ScheduleType.INTERVAL) {
                            item.setLastExecuteTime(System.currentTimeMillis());
                        }

                        // item execution num probably isn't up to date, so get it out of the database
                        Schedule s = registry.getSchedule(item.getID());
                        item.calculateNextExecute(System.currentTimeMillis());
                        item.setExecutionNum(s.getExecutionNum() + 1);
                        Scheduler scheduler = new Scheduler(getActivity());
                        scheduler.schedule(item);
                    } else {
                        item.setNextExecute(-1L);
                    }
                    registry.update(item);


                    notifyDataSetChanged();
                }
            });
            enabledSwitch.setChecked(item.isEnabled());

            final View editContainer = v.findViewById(R.id.edit_container);
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    editContainer.setVisibility(View.VISIBLE);
                    return false;
                }
            });

            editContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editContainer.setVisibility(View.GONE);
                }
            });

            View editButton = v.findViewById(R.id.edit_schedule);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogSchedule ds = new DialogSchedule(getActivity(), FragmentSchedule.this, item, -1);
                    ds.show();
                    editContainer.setVisibility(View.GONE);
                }
            });

            View deleteButton = v.findViewById(R.id.delete_schedule);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registry.delete(item);
                    ArrayList<Schedule> ss = registry.getScheduledComposites();
                    ScheduleAdapter sa = new ScheduleAdapter(getActivity(), ss);
                    scheduleList.setAdapter(sa);
                }
            });

            return v;
        }
    }

    private void enableDialog(final Schedule s, final SwitchCompat enableSwitch) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Enable")
                .setMessage(String.format("Composite %s is disabled. Enable it?", s.getComposite().getName()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        s.getComposite().setEnabled(true);
                        enableSwitch.setChecked(true);
                    }
                })
                .setNegativeButton("No", null) // We've turned the switch back off already
                .show();
    }

    // TODO Setting the next calculated time for month isn't working -- maybe it isn't calculating on a new one
}
