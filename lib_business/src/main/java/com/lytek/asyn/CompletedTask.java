package com.lytek.asyn;

/**
 * author:Fred
 * date:2022/9/25 17:35
 * describe:<b>all completed task, called when all tasks are completed,
 * regardless of whether or not an error has occurred<b/>
 */
public abstract class CompletedTask extends MiniTask implements ICompletedListener {
    private static final String TAG = "CompletedTask";

    public CompletedTask() {
        this(null);
    }

    public CompletedTask(Object input) {
        this(IThreadConstants.DEFAULT_THREAD, input);
    }

    public CompletedTask(int threadType) {
        this(threadType, null);
    }

    public CompletedTask(int threadType, Object input) {
        super(threadType, input);
    }

    @Override
    public Object doTask(Object input) {
        LogUtils.d(TAG, "doTask | CompletedTask is running in " + Thread.currentThread().getName());
        onCompleted();
        return null;
    }
}
