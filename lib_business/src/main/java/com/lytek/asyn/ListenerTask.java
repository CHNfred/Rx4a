package com.lytek.asyn;

/**
 * author:Fred
 * date:2022/9/25 17:35
 * describe:<b>listener task, for parallel task scenarios, {@link ParallelTask}<b/>
 */
public abstract class ListenerTask extends MiniTask implements ITaskListener {
    private static final String TAG = "ListenerTask";

    public ListenerTask() {
        this(null);
    }

    public ListenerTask(Object input) {
        this(IThreadConstants.DEFAULT_THREAD, input);
    }

    public ListenerTask(int threadType) {
        this(threadType, null);
    }

    public ListenerTask(int threadType, Object input) {
        super(threadType, input);
    }

    @Override
    public Object doTask(Object input) {
        LogUtils.d(TAG, "doTask | ListenerTask is running in " + Thread.currentThread().getName());
        onProgress(input);
        return null;
    }
}
