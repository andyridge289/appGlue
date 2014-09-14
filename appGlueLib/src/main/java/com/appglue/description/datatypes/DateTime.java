package com.appglue.description.datatypes;

import android.os.Bundle;

public class DateTime extends IOType
{
    private int h;
    private int m;
    private int s;

    private int y;
    private int M;
    private int d;

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";


    public DateTime()
    {
        super();
        this.name = "DateTime";
        this.className = DateTime.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
    }

    public DateTime(int h, int m, int s, int y, int M, int d) {
        this();
        this.h = h;
        this.m = m;
        this.s = s;
        this.y = y;
        this.M = M;
        this.d = d;
    }

    @Override
    public Object getFromBundle(Bundle b, String key, Object defaultValue) {

        int hour = b.getInt(HOUR);
        int minute = b.getInt(MINUTE);
        int second = b.getInt(SECOND);
        int year = b.getInt(YEAR);
        int month = b.getInt(MONTH);
        int day = b.getInt(DAY);

        // TODO Probably need to do something with the default value
        return new DateTime(hour, minute, second, year, month, day);
    }

    @Override
    public void addToBundle(Bundle b, Object o, String key) {
        // FIXME Not sure what I'm adding to the bundle to be honest?
    }

    @Override
    public String toString(Object value) {

        return String.format("%d:%d:%d %d/%d/%d", h, m, s, y, M, d);
    }

    @Override
    public Object fromString(String value) {
        // FIXME Parse the string back as a date time
        return null;
    }

    public boolean compare(Object a, Object b) {
        return false;
    }
}
