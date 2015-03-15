package com.appglue.engine;

import android.util.Log;

import com.appglue.engine.model.CompositeService;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class Schedule {

    private long id = -1;
    private CompositeService composite;

    private ScheduleType type;

    private long numeral;
    private Interval interval;

    private long lastExecuted;
    private long nextExecute;
    private int executionNum;

    private boolean enabled;

    private TimePeriod timePeriod;
    private int minute;
    private int hour;
    private int dayOfWeek;
    private int dayOfMonth;

    private boolean scheduled;


    public Schedule() {
        this.id = -1;
        this.composite = null;
        this.type = ScheduleType.TIME;
        this.numeral = 1;
        this.interval = Interval.DAY;
        this.lastExecuted = System.currentTimeMillis();
        this.enabled = true;
        this.timePeriod = TimePeriod.DAY;
        this.minute = 0;
        this.hour = 12;
        this.dayOfWeek = Calendar.MONDAY;
        this.dayOfMonth = 1;
        this.nextExecute = -1;
        this.scheduled = false;
        this.executionNum = 0;
    }

    public Schedule(long id, CompositeService cs, boolean enabled,
                    int scheduleType, long numeral, int intervalIndex, long lastExecuted,
                    int timePeriod, int dayOfWeek, int dayOfMonth, int hour, int minute,
                    long nextExecute, boolean isScheduled, int executionNum) {
        this.id = id;
        this.composite = cs;
        this.enabled = enabled;
        this.type = ScheduleType.values()[scheduleType];
        this.numeral = numeral;
        this.interval = Interval.values()[intervalIndex];
        this.lastExecuted = lastExecuted;
        this.timePeriod = TimePeriod.values()[timePeriod];
        this.dayOfWeek = dayOfWeek;
        this.dayOfMonth = dayOfMonth;
        this.hour = hour;
        this.minute = minute;
        this.nextExecute = nextExecute;
        this.scheduled = isScheduled;
        this.executionNum = executionNum;
    }

    public CompositeService getComposite() {
        return composite;
    }

    public void setID(long ID) {
        this.id = ID;
    }

    public long getID() {
        return id;
    }

    public long getLastExecuted() {
        return lastExecuted;
    }

    public void setLastExecuteTime(long lastExecuteTime) {
        this.lastExecuted = lastExecuteTime;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.type = scheduleType;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getMinute() {
        return minute;
    }

    public int getHour() {
        return hour;
    }

    public void setComposite(CompositeService composite) {
        this.composite = composite;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public long getNextExecute() {
        return nextExecute;
    }

    public void setNextExecute(long nextExecute) {
        this.nextExecute = nextExecute;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    public int getExecutionNum() {
        return executionNum;
    }

    public void setExecutionNum(int executionNum) {
        this.executionNum = executionNum;
    }

    public enum ScheduleType {
        NONE(0, "None"),
        INTERVAL(1, "Interval"),
        TIME(2, "Time");

        public int index;
        public String name;

        ScheduleType(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    public enum Interval {
        MINUTE(0, 60, "Minutes"),
        HOUR(1, 3600, "Hours"),
        DAY(2, 86400, "Days");

        public int index;
        public int value;
        public String name;

        Interval(int index, int value, String name) {
            this.index = index;
            this.value = value;
            this.name = name;
        }
    }

    public enum TimePeriod {
        HOUR(0, "Hour", new String[] { "at" }, new int[1]), // minute
        DAY(1, "Day", new String[] { "at", ":" }, new int[2]), // hour, minute
        WEEK(2, "Week", new String[] { "on", "at", ":"}, new int[3]), // dayOfWeek, hour, minute
        MONTH(3, "Month", new String[] { "on", "at", ":" }, new int[3]); // dayOfWeek, hour, minute

        public int index;
        public String name;
        public String[] linkWords;
        public int[] values;

        TimePeriod(int index, String name, String[] linkWords, int[] values) {
            this.index = index;
            this.name = name;
            this.linkWords = linkWords;
            this.values = values;
        }
    }

    public long getNumeral() {
        return numeral;
    }

    public void setNumeral(long numeral) {
        this.numeral = numeral;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public ScheduleType getScheduleType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean equals(Object o) {

        if (o == null) {
            if (LOG) Log.d(TAG, "Schedule->Equals: null");
            return false;
        }
        if (!(o instanceof Schedule)) {
            if (LOG) Log.d(TAG, "Schedule->Equals: Not a ComponentService");
            return false;
        }
        Schedule other = (Schedule) o;

        if (this.id != other.getID()) {
            if (LOG) Log.d(TAG, "Schedule->Equals: id: " + this.id + " - " + other.getID());
            return false;
        }

        if (this.composite.getID() != other.getComposite().getID()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: composite: " + this.composite.getID() + " - " + other.getComposite().getID());
            return false;
        }

        if (this.numeral != other.getNumeral()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: numeral: " + this.numeral + " - " + other.getNumeral());
            return false;
        }

        if (this.interval.index != other.getInterval().index) {
            if (LOG) Log.d(TAG, "Schedule->Equals: interval: " + this.interval.index + " - " +
                    other.getInterval().index);
            return false;
        }

        if (this.enabled != other.isEnabled()) {
            if (LOG) Log.d(TAG, "Schedule->Equals: enabled");
            return false;
        }

        if (!this.type.equals(other.getScheduleType())) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: schedule " + type.name + " - " + other.getScheduleType().name);
            return false;
        }

        if (this.lastExecuted != other.getLastExecuted()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: last time executed: " + this.lastExecuted + " - " + other.getLastExecuted());
            return false;
        }

        if (!this.timePeriod.equals(other.getTimePeriod())) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: time period: " + this.timePeriod + " - " + other.getTimePeriod());
            return false;
        }

        if (this.minute != other.getMinute()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: minute: " + this.minute + " - " + other.getMinute());
            return false;
        }

        if (this.hour != other.getHour()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: hour: " + this.hour + " - " + other.getHour());
            return false;
        }

        if (this.dayOfWeek != other.getDayOfWeek()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: dayOfWeek: " + this.dayOfWeek + " - " + other.getDayOfWeek());
            return false;
        }

        if (this.nextExecute != other.getNextExecute()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: next execute: " + this.nextExecute + " - " + other.getNextExecute());
            return false;
        }

        if (this.executionNum != other.getExecutionNum()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: execution num: " + this.executionNum + " - " + other.getExecutionNum());
            return false;
        }

        if (this.scheduled != other.isScheduled()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: is scheduled: " + this.scheduled + " - " + other.isScheduled());
            return false;
        }

        return true;
    }

    public void calculateNextExecute(long time) {
        if (type == Schedule.ScheduleType.INTERVAL) {

            // Work out what the difference is is
            int interval = this.interval.value;
            interval *= 1000; // Turn it into milliseconds
            interval *= this.numeral;

            nextExecute = lastExecuted + interval;

        } else {
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(time);

            if (cal.get(Calendar.SECOND) > 0 || cal.get(Calendar.MILLISECOND) > 0) { // This should always happen, but you never know
                // Set the seconds and milliseconds to be zero
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.MINUTE, 1); // Move forward to the next whole minute


            }

            // If it's hour, move to the next appropriate minute
            int currentMinute = cal.get(Calendar.MINUTE);
            if (currentMinute > minute) {
                // Move forward an hour
                cal.add(Calendar.HOUR, 1);
            }

            // Then move to the right minute
            cal.set(Calendar.MINUTE, minute);

            if (this.timePeriod.index > Schedule.TimePeriod.HOUR.index) {

                // If it's day, Move to the next appropriate minute, then hour
                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                if (hourOfDay > hour) {
                    // Move forward a day
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

                cal.set(Calendar.HOUR_OF_DAY, hour);

                if (timePeriod.equals(Schedule.TimePeriod.WEEK)) {

                    // If it's week, Move to the next appropriate minute, then hour, then day of week
                    int current = cal.get(Calendar.DAY_OF_WEEK);
                    int target = dayOfWeek;

                    if (target > current) {
                        cal.add(Calendar.DAY_OF_YEAR, target - current);
                    } else if (target < current) {
                        int day = Calendar.SATURDAY - current + target - 1;
                        cal.add(Calendar.DAY_OF_YEAR, day);
                    } else {
                        // Its either today or next week
                        if (cal.get(Calendar.HOUR) > hour) {
                            cal.add(Calendar.DAY_OF_YEAR, 7);
                        } else if (cal.get(Calendar.HOUR) == hour && cal.get(Calendar.MINUTE) > currentMinute) {
                            cal.add(Calendar.DAY_OF_YEAR, 7);
                        }
                    }

                } else if (timePeriod.equals(Schedule.TimePeriod.MONTH)) {

                    // If it's month Move to the next appropraite minute, then hour, then day of month
                    int currentDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    if (currentDayOfMonth > dayOfMonth) {
                        // Move forward a month
                        cal.add(Calendar.MONTH, 1);
                    }

                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                }
            }

            nextExecute = cal.getTimeInMillis();
        }
    }
}
