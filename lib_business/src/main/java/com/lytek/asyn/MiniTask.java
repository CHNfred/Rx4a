package com.lytek.asyn;

import java.util.concurrent.Future;

/**
 * author:Fred
 * date:2022/9/23 16:20
 * describe:abstract Task, basic task module
 */
public abstract class MiniTask {
    private static final String TAG = "MiniTask";
    private MiniTask mListenerTask;
    private Object mInput;
    private Object mOutput;
    private boolean mIsCancel;
    private Future<?> mListenerFuture;
    private final int mThreadType;

    public MiniTask() {
        this(null);
    }

    public MiniTask(Object input) {
        this(IThreadConstants.DEFAULT_THREAD, input);
    }

    public MiniTask(int threadType, Object input) {
        mThreadType = threadType;
        mInput = input;
        mIsCancel = false;
    }

    public Object getInput() {
        return mInput;
    }

    public void setInput(Object input) {
        mInput = input;
    }

    public Object getOutput() {
        return mOutput;
    }

    public void setOutput(Object output) {
        mOutput = output;
    }

    public int getThreadType() {
        return mThreadType;
    }

    public boolean isCancel() {
        return mIsCancel;
    }

    public void setCancel(boolean cancel) {
        mIsCancel = cancel;
        if (cancel && mListenerFuture != null) {
            mListenerFuture.cancel(true);
        }
    }

    public void notifyResult(Object input) {
        MiniTask task = mListenerTask;
        if (task == null) {
            LogUtils.d(TAG, "notifyResult | because ListenerTask is null, so return");
            return;
        }

        switch (task.mThreadType) {
            case IThreadConstants.MAIN_THREAD:
                LogUtils.d(TAG, "notifyResult | exec in main thread");
                new TaskWrapper<>(
                        new TaskInfo(task, input),
                        new TaskWrapper.ITaskWrapperListener<TaskInfo>() {
                            @Override
                            public void invoke(final TaskInfo data) {
                                TaskThreadFactory.getMainThread().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            data.mTask.doTask(data.mInput);
                                        } catch (Throwable throwable) {
                                            LogUtils.e(TAG, "[notifyResult]", throwable);
                                        }
                                    }
                                });
                            }
                        }
                ).invoke();
                break;
            case IThreadConstants.SUB_THREAD:
                LogUtils.d(TAG, "notifyResult | exec in sub thread");
                new TaskWrapper<>(
                        new TaskInfo(task, input),
                        new TaskWrapper.ITaskWrapperListener<TaskInfo>() {
                            @Override
                            public void invoke(final TaskInfo data) {
                                mListenerFuture = TaskThreadFactory.getListenerThread().submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            data.mTask.doTask(data.mInput);
                                        } catch (Throwable throwable) {
                                            LogUtils.e(TAG, "[notifyResult]", throwable);
                                        }
                                    }
                                });
                            }
                        }
                ).invoke();
                break;
            default:
                LogUtils.d(TAG, "notifyResult | exec in default thread");
                try {
                    task.doTask(input);
                } catch (Throwable throwable) {
                    LogUtils.e(TAG, "[notifyResult]", throwable);
                }
                break;
        }

    }

    public abstract Object doTask(Object input);

    void setListenerTask(MiniTask task) {
        mListenerTask = task;
    }

    static class TaskInfo {
        MiniTask mTask;
        Object mInput;

        TaskInfo(MiniTask task, Object input) {
            mTask = task;
            mInput = input;
        }
    }
}
