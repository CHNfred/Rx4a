package com.lytek.asyn;

import android.util.Log;

public class LogUtils {

    protected static boolean mLoggingEnabled = true;

    private static final String PRE_TAG = "SDK_";
    private static String mPreTag = PRE_TAG;
    private static final String DEFAULT_MSG = "User passed in an empty message!";

    public static void setPreTag(String preTag) {
        mPreTag = preTag;
    }

    public static void setDebugLogging(boolean enabled) {
        mLoggingEnabled = enabled;
    }

    public static boolean isDebugLogging() {
        return mLoggingEnabled;
    }

    public static int v(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.v(getPreTag() + tag, verifyMessage(msg));
        }
        return result;
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.v(getPreTag() + tag, verifyMessage(msg), tr);
        }
        return result;
    }

    public static int d(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.d(getPreTag() + tag, verifyMessage(msg));
        }
        return result;
    }

    public static int d(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.d(getPreTag() + tag, verifyMessage(msg), tr);
        }
        return result;
    }

    public static int i(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.i(getPreTag() + tag, verifyMessage(msg));
        }
        return result;
    }

    public static int i(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.i(getPreTag() + tag, verifyMessage(msg), tr);
        }
        return result;
    }

    public static int w(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(getPreTag() + tag, verifyMessage(msg));
        }
        return result;
    }

    public static int w(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(getPreTag() + tag, verifyMessage(msg), tr);
        }
        return result;
    }

    public static int w(String tag, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(getPreTag() + tag, tr);
        }
        return result;
    }

    public static int e(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.e(getPreTag() + tag, verifyMessage(msg));
        }
        return result;
    }

    public static int e(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.e(getPreTag() + tag, verifyMessage(msg), tr);
        }
        return result;
    }

    public static String getPreTag() {
        return mPreTag;
    }

    private static String verifyMessage(String msg) {
        return msg == null ? DEFAULT_MSG : msg;
    }

}
