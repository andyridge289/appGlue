package com.appglue.engine;

import android.util.Log;

import com.appglue.Constants;
import com.appglue.engine.description.CompositeService;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class Schedule {

    private long id = -1;
    private CompositeService composite;

    private ScheduleType type;

    private long numeral;
    private Interval interval;

    private boolean enabled;

    private long timeLastExecuted;

    // TODO Let them specify a start time, i.e. every 20 minutes from 12:00

    public Schedule(long id, CompositeService cs, boolean enabled, int scheduleType, long numeral, int intervalIndex, long lastExecuted) {
        this.id = id;
        this.composite = cs;
        this.enabled = enabled;
        this.type = ScheduleType.values()[scheduleType];
        this.numeral = numeral;
        this.interval = Interval.values()[intervalIndex];
        this.timeLastExecuted = lastExecuted;
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

    public enum Interval
    {
        NONE(0, 0, "None"),
        SECONDS(1, 1, "Second"),
        MINUTES(2, 60, "Minute"),
        HOURS(3, 3600, "Hour"),
        DAYS(4, 86400, "Day");

        public int index;
        public int value;
        public String name;

        Interval(int index, int value, String name)
        {
            this.index = index;
            this.value = value;
            this.name = name;
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

        if(o == null) {
            if(LOG) Log.d(TAG, "Schedule->Equals: null");
            return false;
        }
        if(!(o instanceof Schedule)) {
            if (LOG) Log.d(TAG, "Schedule->Equals: Not a ComponentService");
            return false;
        }
        Schedule other = (Schedule) o;

        if (this.id != other.getID()) {
            if (LOG) Log.d(TAG, "Schedule->Equals: id: " + this.id + " - " + other.getID());
            return false;
        }

        if (this.composite.getID() != other.getComposite().getID()) {
            if (LOG) Log.d(TAG, "Schedule->Equals: composite: " + this.composite.getID() + " - " + other.getComposite().getID());
            return false;
        }

        if(this.numeral != other.getNumeral()) {
            if (LOG) Log.d(TAG, "Schedule->Equals: numeral: " + this.numeral + " - " + other.getNumeral());
            return false;
        }

        if(this.interval.index != other.getInterval().index) {
            if (LOG) Log.d(TAG, "Schedule->Equals: interval: " + this.interval.index + " - " +
                    other.getInterval().index);
            return false;
        }

        if(this.enabled != other.isEnabled()) {
            if (LOG) Log.d(TAG, "Schedule->Equals: enabled");
            return false;
        }

        if(!this.type.equals(other.getScheduleType())) {
            if (LOG) Log.d(TAG, "Schedule->Equals: schedule " + type.name + " - " + other.getScheduleType().name);
            return false;
        }

        if (this.timeLastExecuted != other.getLastExecuted()) {
            if (LOG) Log.d(TAG, "Schedule->Equals: last time executed: " + this.timeLastExecuted + " - " + other.getLastExecuted());
            return false;
        }

        return true;
    }
}
