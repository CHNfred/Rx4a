package com.lytek.asyn;

/**
 * author:Fred
 * date:2022/9/25 17:35
 * describe:<b>catch error task, called when an error occurs, later tasks will not be executed<b/>
 */
public abstract class CatchErrorTask extends MiniTask implements ICatchErrorListener {
    private static final String TAG = "CatchErrorTask";

    public CatchErrorTask() {
        this(null);
    }

    public CatchErrorTask(Object input) {
        this(IThreadConstants.DEFAULT_THREAD, input);
    }

    public CatchErrorTask(int threadType) {
        this(threadType, null);
    }

    public CatchErrorTask(int threadType, Object input) {
        super(threadType, input);
    }

    @Override
    public Object doTask(Object input) {
        LogUtils.d(TAG, "doTask | CatchErrorTask is running in " + Thread.currentThread().getName());
        onError((Throwable) input);
        return null;
    }
}
