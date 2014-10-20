package com.appglue.engine;

import android.util.Log;

import com.appglue.engine.description.CompositeService;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class Schedule {

    private long id = -1;
    private CompositeService composite;

    private ScheduleType type;

    private long numeral;
    private Interval interval;
    private long timeLastExecuted;

    private boolean enabled;

    private TimePeriod period;
    private int minute;
    private int hour;
    private int dayOfWeek;
    private int dayOfMonth;
    private TimePeriod timePeriod;

    public Schedule() {
        this.id = -1;
        this.composite = null;
        this.type = ScheduleType.TIME;
        this.numeral = 0;
        this.interval = Interval.DAY;
        this.timeLastExecuted = System.currentTimeMillis();
        this.enabled = true;
        this.period = TimePeriod.DAY;
        this.minute = 0;
        this.hour = 0;
        this.dayOfWeek = 0;
        this.dayOfMonth = 0;
    }



    public Schedule(long id, CompositeService cs, boolean enabled,
                    int scheduleType, long numeral, int intervalIndex, long lastExecuted,
                    int timePeriod, int dayOfWeek, int dayOfMonth, int hour, int minute) {
        this.id = id;
        this.composite = cs;
        this.enabled = enabled;
        this.type = ScheduleType.values()[scheduleType];
        this.numeral = numeral;
        this.interval = Interval.values()[intervalIndex];
        this.timeLastExecuted = lastExecuted;
        this.period = TimePeriod.values()[timePeriod];
        this.dayOfWeek = dayOfWeek;
        this.dayOfMonth = dayOfMonth;
        this.hour = hour;
        this.minute = minute;
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
        return timeLastExecuted;
    }

    public void setLastExecuteTime(long lastExecuteTime) {
        this.timeLastExecuted = lastExecuteTime;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.type = scheduleType;
    }

    public TimePeriod getTimePeriod() {
        return period;
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

    public int getScheduleIndex() {
        return type.index;
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

        if (this.timeLastExecuted != other.getLastExecuted()) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: last time executed: " + this.timeLastExecuted + " - " + other.getLastExecuted());
            return false;
        }

        if (!this.period.equals(other.getTimePeriod())) {
            if (LOG)
                Log.d(TAG, "Schedule->Equals: time period: " + this.period + " - " + other.getTimePeriod());
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

        return true;
    }
}
