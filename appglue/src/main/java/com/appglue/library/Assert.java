package com.appglue.library;

import com.appglue.BuildConfig;
import com.orhanobut.logger.Logger;

import java.util.List;

/**
 *
 */
public class Assert {

    public static boolean fail(String message) {
        if (BuildConfig.DEBUG) {
            throw new RuntimeException(message);
        } else {
            Logger.e(message);
        }
        return true;
    }

    public static boolean equals(boolean b) {
        if (b) {
            return false;
        }

        String caller = getCaller();
        String message = String.format("Assert failed, boolean null. Called by %s", caller);

        return fail(message);
    }

    public static boolean exists(Object o) {
        if (o != null) {
            return false;
        }

        String caller = getCaller();
        String message = String.format("Assert failed, object doesn't exist. Called by %s", caller);
        return fail(message);
    }

    public static boolean exists(List l, int position) {
        boolean ret = Assert.exists(l);
        if (l == null) {
            return true;
        }

        if (l.size() > position) {
            return true;
        }

        String caller = getCaller();
        String message = String.format("Assert failed, index %d doesn't exist. Called by %s", position, caller);
        return fail(message);
    }

    private static String getCaller() {
        StackTraceElement elements[] = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : elements) {
            if (element.getClassName().equals(Assert.class.getName())) {
                continue;
            }

            return element.getClassName() + ": " + element.getMethodName();
        }

        return null;
    }
}
