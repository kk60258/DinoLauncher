package com.android.launcher3.util;

import android.util.Log;

/**
 * Created by NineG on 2016/5/15.
 */
public class Logger {

    private static boolean loggable(int level) {
        return true;
    }

    public static void d(String tag, String message, String... args) {
        print(Log.DEBUG, tag, message, args);
    }

    public static void d(String tag, Throwable tr, String message, String... args) {
        print(Log.DEBUG, tag, tr, message, args);
    }

    private static void print(int level, String tag, String message, String... args) {
        if (loggable(level))
            android.util.Log.println(0, tag, String.format(message, args));
    }

    private static void print(int level, String tag, Throwable tr, String message, String... args) {
        if (loggable(level))
            android.util.Log.println(0, tag, String.format(message, args) + '\n' + android.util.Log.getStackTraceString(tr));
    }
}
