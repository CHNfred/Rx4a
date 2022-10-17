package com.lytek.asyn;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

/**
 * author:Fred
 * date:2022/9/23 16:20
 * describe:abstract Task, basic task module
 */
public abstract class MiniTask {
    private static final String TAG = "MiniTask";
    private final CopyOnWriteArrayList<MiniTask> mListenerTask;
    private Object mInput;
    private Object mOutput;
    private boolean mIsCancel;
    private Future<?> mListenerFuture;
    private final int mThreadType;
    private TaskThreadFactory mTaskThreadFactory;

    public MiniTask() {
        this(null);
    }

    public MiniTask(Object input) {
        this(IThreadConstants.DEFAULT_THREAD, input);
    }

    public MiniTask(int threadType, Object input) {
        mListenerTask = new CopyOnWriteArrayList<>();
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
        if (mListenerTask == null || mListenerTask.size() == 0) {
            LogUtils.d(TAG, "notifyResult | because ListenerTask list is null or size == 0, so return");
            return;
        }

        for (MiniTask miniTask : mListenerTask) {
            if (miniTask == null) {
                continue;
            }

            switch (miniTask.mThreadType) {
                case IThreadConstants.MAIN_THREAD:
                    LogUtils.d(TAG, "notifyResult | exec in main thread");
                    new TaskWrapper<>(
                            new TaskInfo(miniTask, input),
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
                            new TaskInfo(miniTask, input),
                            new TaskWrapper.ITaskWrapperListener<TaskInfo>() {
                                @Override
                                public void invoke(final TaskInfo data) {
                                    mListenerFuture = mTaskThreadFactory.getListenerThread().submit(new Runnable() {
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
                        miniTask.doTask(input);
                    } catch (Throwable throwable) {
                        LogUtils.e(TAG, "[notifyResult]", throwable);
                    }
                    break;
            }
        }
    }

    public abstract Object doTask(Object input);

    void addListenerTask(MiniTask task) {
        if (task == null) {
            return;
        }

        if (!mListenerTask.contains(task)) {
            mListenerTask.add(task);
        }
        LogUtils.d(TAG, "addListenerTask | mListenerTask size = " + mListenerTask.size());
    }


    void removeListener(MiniTask miniTask) {
        if (miniTask == null) {
            return;
        }

        mListenerTask.remove(miniTask);
        LogUtils.d(TAG, "removeListener | mListenerTask size = " + mListenerTask.size());
    }

    void clearListener() {
        if (mListenerTask.size() > 0) {
            mListenerTask.clear();
        }
        LogUtils.d(TAG, "clearListener | mListenerTask size = " + mListenerTask.size());
    }

    void setTaskThreadFactory(TaskThreadFactory taskThreadFactory) {
        mTaskThreadFactory = taskThreadFactory;
    }

    TaskThreadFactory getTaskThreadFactory() {
        return mTaskThreadFactory;
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
