package com.appglue.library;

import android.util.SparseArray;

import java.lang.reflect.Method;

public class IOFilter {
    public static SparseArray<FilterValue> filters = new SparseArray<FilterValue>();

    /**
     * ******************************************************
     * String methods
     * ******************************************************
     */

    public static boolean strEquals(String first, String second) {
        return first.equals(second);
    }

    public static boolean strNotEquals(String first, String second) {
        return !first.equals(second);
    }

    public static boolean strContains(String first, String second) {
        return first.contains(second);
    }

    /**
     * ******************************************************
     * Number methods
     * ******************************************************
     */

    public static boolean numEquals(Integer first, Integer second) {
        return first.equals(second);
    }

    public static boolean numNotEquals(Integer first, Integer second) {
        return !first.equals(second);
    }

    public static boolean numLEquals(Integer first, Integer second) {
        return first <= second;
    }

    public static boolean numGEquals(Integer first, Integer second) {
        return first >= second;
    }

    public static boolean numLT(Integer first, Integer second) {
        return first < second;
    }

    public static boolean numGT(Integer first, Integer second) {
        return first > second;
    }

    /**
     * *************************************************
     * Boolean methods
     * ************************************************
     */

    public static boolean boolEquals(Boolean first, Boolean second) {
        return first == second;
    }

    public static boolean boolNotEquals(Boolean first, Boolean second) {
        return first != second;
    }

    public static void filterFactory() {
        try {
            // Strings
            STR_EQUALS = new FilterValue("Equals", 0x00, IOFilter.class.getMethod("strEquals", String.class, String.class));
            filters.put(STR_EQUALS.index, STR_EQUALS);

            STR_NOTEQUALS = new FilterValue("Doesn't equal", 0x01, IOFilter.class.getMethod("strNotEquals", String.class, String.class));
            filters.put(STR_NOTEQUALS.index, STR_NOTEQUALS);

            STR_CONTAINS = new FilterValue("Contains", 0x02, IOFilter.class.getMethod("strContains", String.class, String.class));
            filters.put(STR_CONTAINS.index, STR_CONTAINS);

            // Numbers
            INT_EQUALS = new FilterValue("=", 0x10, IOFilter.class.getMethod("numEquals", Integer.class, Integer.class));
            filters.put(INT_EQUALS.index, INT_EQUALS);

            INT_NOTEQUALS = new FilterValue(Character.toString((char) 0x2260), 0x11, IOFilter.class.getMethod("numNotEquals", Integer.class, Integer.class)); // !=
            filters.put(INT_NOTEQUALS.index, INT_NOTEQUALS);

            INT_LEQUALS = new FilterValue(Character.toString((char) 0x2264), 0x12, IOFilter.class.getMethod("numLEquals", Integer.class, Integer.class)); // <=
            filters.put(INT_LEQUALS.index, INT_LEQUALS);

            INT_GEQUALS = new FilterValue(Character.toString((char) 0x2265), 0x13, IOFilter.class.getMethod("numGEquals", Integer.class, Integer.class)); // >=
            filters.put(INT_GEQUALS.index, INT_GEQUALS);

            INT_LT = new FilterValue(Character.toString((char) 0x003E), 0x14, IOFilter.class.getMethod("numLT", Integer.class, Integer.class)); // <
            filters.put(INT_LT.index, INT_LT);

            INT_GT = new FilterValue(Character.toString((char) 0x003D), 0x15, IOFilter.class.getMethod("numGT", Integer.class, Integer.class)); // >
            filters.put(INT_GT.index, INT_GT);

            BOOL_EQUALS = new FilterValue("is", 0x20, IOFilter.class.getMethod("boolEquals", Boolean.class, Boolean.class));
            filters.put(BOOL_EQUALS.index, BOOL_EQUALS);

            BOOL_NOTEQUALS = new FilterValue("isn't", 0x21, IOFilter.class.getMethod("boolNotEquals", Boolean.class, Boolean.class));
            filters.put(BOOL_NOTEQUALS.index, BOOL_NOTEQUALS);

            SET_EQUALS = new FilterValue("is", 0x30, IOFilter.class.getMethod("numEquals", Integer.class, Integer.class));
            filters.put(SET_EQUALS.index, SET_EQUALS);

            SET_NOTEQUALS = new FilterValue("isn't", 0x31, IOFilter.class.getMethod("numNotEquals", Integer.class, Integer.class));
            filters.put(SET_NOTEQUALS.index, SET_NOTEQUALS);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static FilterValue STR_EQUALS;
    public static FilterValue STR_NOTEQUALS;
    public static FilterValue STR_CONTAINS;

    public static FilterValue INT_EQUALS;
    public static FilterValue INT_NOTEQUALS;
    public static FilterValue INT_LEQUALS;
    public static FilterValue INT_GEQUALS;
    public static FilterValue INT_LT;
    public static FilterValue INT_GT;

    public static FilterValue BOOL_EQUALS;
    public static FilterValue BOOL_NOTEQUALS;

    public static FilterValue SET_EQUALS;
    public static FilterValue SET_NOTEQUALS;

    public static class FilterValue {
        public String text;
        public int index;
        public Method method;

        FilterValue(String text, int index, Method method) {
            this.text = text;
            this.index = index;
            this.method = method;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }
}
