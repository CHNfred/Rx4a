package com.lytek.asyn;

import android.os.Handler;
import android.os.Looper;

public class UiHandler {
    private final Handler mUIHandler;

    private UiHandler() {
        mUIHandler = new Handler(Looper.getMainLooper());
    }

    private static class SingletonHolder {
        static UiHandler sRunner = new UiHandler();
    }

    public static synchronized Handler getUIHandler() {
        return SingletonHolder.sRunner.mUIHandler;
    }
}
