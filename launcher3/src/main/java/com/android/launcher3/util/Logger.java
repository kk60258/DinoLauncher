package com.android.launcher3.util;

import android.util.Log;

/**
 * Created by NineG on 2016/5/15.
 */
public class Logger {

    private static final String LOG_TAG_PREFIX= "9G_";
    private static final int LOG_TAG_LENGTH_MAX = 15;

    private static boolean loggable(int level) {
        return true;
    }

    public static String getLogTag(Class c) {
        String s = LOG_TAG_PREFIX + c.getSimpleName();
        if (s.length() > LOG_TAG_LENGTH_MAX)
            s = s.substring(0, LOG_TAG_LENGTH_MAX - 1);
        return s;
    }

    public static void d(String tag, String message, Object... args) {
        print(Log.DEBUG, tag, message, args);
    }

    public static void d(String tag, Throwable tr, String message, Object... args) {
        print(Log.DEBUG, tag, tr, message, args);
    }

    private static void print(int level, String tag, String message, Object... args) {
        if (loggable(level))
            android.util.Log.println(level, tag, String.format(message, args));
    }

    private static void print(int level, String tag, Throwable tr, String message, Object... args) {
        if (loggable(level))
            android.util.Log.println(level, tag, String.format(message, args) + '\n' + android.util.Log.getStackTraceString(tr));
    }
}
