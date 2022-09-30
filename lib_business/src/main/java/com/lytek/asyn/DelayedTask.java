package com.lytek.asyn;

import android.os.SystemClock;

/**
 * author:Fred
 * date:2022/9/25 17:35
 * describe:<b>delayed task, delay running for specified time, delay time must be greater than 0<b/>
 */
public abstract class DelayedTask extends MiniTask {
    private static final String TAG = "DelayedTask";
    private final long mDelayedMillisecond;

    public DelayedTask(long delayedMillisecond) throws IllegalArgumentException {
        this(null, delayedMillisecond);
    }

    public DelayedTask(Object input, long delayedMillisecond) throws IllegalArgumentException {
        this(IThreadConstants.DEFAULT_THREAD, input, delayedMillisecond);
    }

    public DelayedTask(int threadType, long delayedMillisecond) throws IllegalArgumentException {
        this(threadType, null, delayedMillisecond);
    }

    public DelayedTask(int threadType, Object input, long delayedMillisecond)
            throws IllegalArgumentException {
        super(threadType, input);
        if (delayedMillisecond <= 0) {
            throw new IllegalArgumentException("delayedMillisecond must be greater than 0");
        }
        mDelayedMillisecond = delayedMillisecond;
    }

    @Override
    public Object doTask(Object input) {
        LogUtils.d(TAG, "doTask | DelayedTask is running in " + Thread.currentThread().getName());
        SystemClock.sleep(mDelayedMillisecond);
        return doDelayedTask(input);
    }

    public abstract Object doDelayedTask(Object input);
}
