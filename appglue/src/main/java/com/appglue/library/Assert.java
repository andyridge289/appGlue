package com.appglue.library;

import android.util.Log;

import com.appglue.BuildConfig;

import static com.appglue.Constants.TAG;

/**
 *
 */
public class Assert {

    public static boolean fail(String message) {
        if (BuildConfig.DEBUG) {
            throw new RuntimeException(message);
        } else {
            Log.e(TAG, message);
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
