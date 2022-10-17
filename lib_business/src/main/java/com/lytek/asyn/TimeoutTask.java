package com.lytek.asyn;

import java.util.concurrent.Future;

/**
 * author:Fred
 * date:2022/9/25 17:35
 * describe:<b>timeout task, the task execution timeout will throw an exception<b/>
 */
public abstract class TimeoutTask extends MiniTask {
    private static final String TAG = "TimeoutTask";
    private final long mTimeoutMillisecond;
    private Future<?> mTimeoutFuture;

    public TimeoutTask(long timeoutMillisecond) throws IllegalArgumentException {
        this(null, timeoutMillisecond);
    }

    public TimeoutTask(Object input, long timeoutMillisecond) throws IllegalArgumentException {
        this(IThreadConstants.DEFAULT_THREAD, input, timeoutMillisecond);
    }

    public TimeoutTask(int threadType, long timeoutMillisecond) throws IllegalArgumentException {
        this(threadType, null, timeoutMillisecond);
    }

    public TimeoutTask(int threadType, Object input, long timeoutMillisecond)
            throws IllegalArgumentException {
        super(threadType, input);
        if (timeoutMillisecond <= 0) {
            throw new IllegalArgumentException("timeoutMillisecond must be greater than 0");
        }
        mTimeoutMillisecond = timeoutMillisecond;
    }

    @Override
    public void setCancel(boolean cancel) {
        super.setCancel(cancel);
        if (mTimeoutFuture != null) {
            mTimeoutFuture.cancel(true);
        }
    }

    @Override
    public Object doTask(Object input) {
        LogUtils.d(TAG, "doTask | TimeoutTask is running in " + Thread.currentThread().getName());
        new TaskWrapper<TimeoutInfo>(new TimeoutInfo(input), new TaskWrapper.ITaskWrapperListener<TimeoutInfo>() {
            @Override
            public void invoke(final TimeoutInfo data) {
                mTimeoutFuture = getTaskThreadFactory().getTimeoutThread().submit(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.d(TAG, "doTask | begin timeout " + Thread.currentThread().getName());
                        try {
                            Thread.sleep(mTimeoutMillisecond);
                            LogUtils.d(TAG, "doTask | end timeout " + Thread.currentThread().getName());
                            notifyResult(new TaskTimeoutException(data.mObject));
                        } catch (InterruptedException e) {
                            //ignore
                        }
                    }
                });
            }
        }).invoke();

        Object output;
        try {
            output = execTask(input);
        } finally {
            mTimeoutFuture.cancel(true);
        }
        return output;
    }

    public abstract Object execTask(Object input);

    static class TimeoutInfo {
        Object mObject;

        TimeoutInfo(Object o) {
            mObject = o;
        }
    }
}
