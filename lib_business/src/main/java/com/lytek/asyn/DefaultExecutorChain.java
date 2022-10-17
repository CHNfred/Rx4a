package com.lytek.asyn;

import java.util.concurrent.Future;

/**
 * author:Fred
 * date:2022/9/23 15:02
 * describe:<b>actuator chain, responsible for assembling individual tasks into chains<b/>
 */
class DefaultExecutorChain {
    private static final String TAG = "DefaultExecutorChain";
    private DefaultExecutorChain mNextDefaultExecutorChain;
    private MiniTask mCatchErrorTask;
    private MiniTask mCompletedTask;
    private final MiniTask mCurTask;
    private MiniTask mNextTask;
    private boolean mIsCancel;
    private Future<?> mCurFuture;
    private Future<?> mNextFuture;
    private Future<?> mCacheErrorFuture;
    private Future<?> mCompletedFuture;

    DefaultExecutorChain(MiniTask curTask) {
        mIsCancel = false;
        mCurTask = curTask;
    }

    void setNextTask(MiniTask task) {
        mNextTask = task;
    }

    MiniTask getNextTask() {
        return mNextTask;
    }

    public DefaultExecutorChain getNextDefaultExecutorChain() {
        return mNextDefaultExecutorChain;
    }

    public void setNextDefaultExecutorChain(DefaultExecutorChain nextDefaultExecutorChain) {
        mNextDefaultExecutorChain = nextDefaultExecutorChain;
    }

    public void setCatchErrorTask(MiniTask catchErrorTask) {
        if (catchErrorTask == null) {
            return;
        }

        mCatchErrorTask = catchErrorTask;
    }

    public void setCompletedTask(MiniTask completedTask) {
        if (completedTask == null) {
            return;
        }

        mCompletedTask = completedTask;
    }

    public void addListenerTask(MiniTask listenerTask) {
        if (listenerTask == null) {
            return;
        }

        if (mCurTask != null) {
            mCurTask.addListenerTask(listenerTask);
        }

        if (mNextTask != null) {
            mNextTask.addListenerTask(listenerTask);
        }
    }

    public void removeListenerTask(MiniTask listenerTask) {
        if (listenerTask == null) {
            return;
        }

        if (mCurTask != null) {
            mCurTask.removeListener(listenerTask);
        }

        if (mNextTask != null) {
            mNextTask.removeListener(listenerTask);
        }
    }

    public void clearListenerTask() {
        if (mCurTask != null) {
            mCurTask.clearListener();
        }

        if (mNextTask != null) {
            mNextTask.clearListener();
        }
    }

    void cancel() {
        mIsCancel = true;
        if (mCurTask != null) {
            mCurTask.setCancel(true);
        }
        if (mNextTask != null) {
            mNextTask.setCancel(true);
        }
        if (mCurFuture != null) {
            mCurFuture.cancel(true);
        }
        if (mNextFuture != null) {
            mNextFuture.cancel(true);
        }
        if (mCacheErrorFuture != null) {
            mCacheErrorFuture.cancel(true);
        }
        if (mCompletedFuture != null) {
            mCompletedFuture.cancel(true);
        }
    }

    void exec() {
        LogUtils.d(TAG, "exec");
        execCurTask();
    }

    private void execCurTask() {
        if (mCurTask == null) {
            LogUtils.d(TAG, "execCurTask | mCurTask == null, so return");
            return;
        }

        if (mIsCancel) {
            LogUtils.d(TAG, "execCurTask | is canceled, so return");
            return;
        }

        int curThreadType = mCurTask.getThreadType();
        switch (curThreadType) {
            case IThreadConstants.MAIN_THREAD:
                LogUtils.d(TAG, "execCurTask | exec in main thread");
                new TaskWrapper<>(
                        new ChainInfo(mCurTask),
                        new TaskWrapper.ITaskWrapperListener<ChainInfo>() {
                            @Override
                            public void invoke(final ChainInfo data) {
                                TaskThreadFactory.getMainThread().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Object input1 = data.mTask.getInput();
                                            Object output1 = data.mTask.doTask(input1);
                                            execNextTask(output1);
                                        } catch (Throwable throwable) {
                                            LogUtils.e(TAG, "[execCurTask]", throwable);
                                            handleCatchError(throwable);
                                        }
                                    }
                                });
                            }
                        }).invoke();
                break;
            case IThreadConstants.SUB_THREAD:
                LogUtils.d(TAG, "execCurTask | exec in sub thread");
                new TaskWrapper<>(
                        new ChainInfo(mCurTask),
                        new TaskWrapper.ITaskWrapperListener<ChainInfo>() {
                            @Override
                            public void invoke(final ChainInfo data) {
                                mCurFuture = data.mTask.getTaskThreadFactory().getSingleThread().submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Object input1 = data.mTask.getInput();
                                            Object output1 = data.mTask.doTask(input1);
                                            execNextTask(output1);
                                        } catch (Throwable throwable) {
                                            LogUtils.e(TAG, "[execCurTask]", throwable);
                                            handleCatchError(throwable);
                                        }
                                    }
                                });
                            }
                        }).invoke();

                break;
            default:
                LogUtils.d(TAG, "execCurTask | exec in default thread");
                try {
                    Object input1 = mCurTask.getInput();
                    Object output1 = mCurTask.doTask(input1);
                    execNextTask(output1);
                } catch (Throwable throwable) {
                    LogUtils.e(TAG, "[execCurTask]", throwable);
                    handleCatchError(throwable);
                }
                break;
        }
    }

    private void execNextTask(final Object output1) {
        if (mNextTask == null) {
            LogUtils.d(TAG, "execNextTask | mNextTask == null, so return");
            handleCompleted();
            return;
        }

        if (mIsCancel) {
            LogUtils.d(TAG, "execNextTask | is canceled, so return");
            return;
        }

        int nextThreadType = mNextTask.getThreadType();
        mNextTask.setInput(output1);
        switch (nextThreadType) {
            case IThreadConstants.MAIN_THREAD:
                LogUtils.d(TAG, "execNextTask | exec in main thread");
                new TaskWrapper<>(
                        new ChainInfo(mNextTask),
                        new TaskWrapper.ITaskWrapperListener<ChainInfo>() {
                            @Override
                            public void invoke(final ChainInfo data) {
                                TaskThreadFactory.getMainThread().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Object output2 = data.mTask.doTask(output1);
                                            data.mTask.setOutput(output2);
                                            handleNextDefaultExecutorChain(output2);
                                        } catch (Throwable throwable) {
                                            LogUtils.e(TAG, "[execNextTask]", throwable);
                                            handleCatchError(throwable);
                                        }
                                    }
                                });
                            }
                        }).invoke();
                break;
            case IThreadConstants.SUB_THREAD:
                LogUtils.d(TAG, "execNextTask | exec in sub thread");
                new TaskWrapper<>(
                        new ChainInfo(mNextTask),
                        new TaskWrapper.ITaskWrapperListener<ChainInfo>() {
                            @Override
                            public void invoke(final ChainInfo data) {
                                mNextFuture = data.mTask.getTaskThreadFactory().getSingleThread().submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Object output2 = data.mTask.doTask(output1);
                                            data.mTask.setOutput(output2);
                                            handleNextDefaultExecutorChain(output2);
                                        } catch (Throwable throwable) {
                                            LogUtils.e(TAG, "[execNextTask]", throwable);
                                            handleCatchError(throwable);
                                        }
                                    }
                                });
                            }
                        }
                ).invoke();
                break;
            default:
                LogUtils.d(TAG, "execNextTask | exec in default thread");
                try {
                    mNextTask.setInput(output1);
                    Object output2 = mNextTask.doTask(output1);
                    mNextTask.setOutput(output2);
                    handleNextDefaultExecutorChain(output2);
                } catch (Throwable throwable) {
                    LogUtils.e(TAG, "[execNextTask]", throwable);
                    handleCatchError(throwable);
                }
                break;
        }
    }

    private void handleCatchError(final Throwable throwable) {
        if (mCatchErrorTask == null) {
            LogUtils.d(TAG, "handleCatchError | mCatchErrorTask == null, so return");
            handleCompleted();
            return;
        }

        int errorThreadType = mCatchErrorTask.getThreadType();
        switch (errorThreadType) {
            case IThreadConstants.MAIN_THREAD:
                LogUtils.d(TAG, "handleCatchError | exec in main thread");
                new TaskWrapper<>(
                        new ChainInfo(mCatchErrorTask),
                        new TaskWrapper.ITaskWrapperListener<ChainInfo>() {
                            @Override
                            public void invoke(ChainInfo data) {
                                TaskThreadFactory.getMainThread().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            mCatchErrorTask.doTask(throwable);
                                        } catch (Throwable error) {
                                            LogUtils.e(TAG, "[handleCatchError]", error);
                                        }
                                        handleCompleted();
                                    }
                                });
                            }
                        }).invoke();
                break;
            case IThreadConstants.SUB_THREAD:
                LogUtils.d(TAG, "handleCatchError | exec in sub thread");
                new TaskWrapper<>(
                        new ChainInfo(mCatchErrorTask),
                        new TaskWrapper.ITaskWrapperListener<ChainInfo>() {
                            @Override
                            public void invoke(ChainInfo data) {
                                mCacheErrorFuture = data.mTask.getTaskThreadFactory().getSingleThread().submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            mCatchErrorTask.doTask(throwable);
                                        } catch (Throwable error) {
                                            LogUtils.e(TAG, "[handleCatchError]", error);
                                        }
                                        handleCompleted();
                                    }
                                });
                            }
                        }).invoke();
                break;
            default:
                LogUtils.d(TAG, "handleCatchError | exec in default thread");
                try {
                    mCatchErrorTask.doTask(throwable);
                } catch (Throwable error) {
                    LogUtils.e(TAG, "[handleCatchError]", error);
                }
                handleCompleted();
                break;
        }
    }

    private void handleNextDefaultExecutorChain(Object output) {
        if (mIsCancel) {
            LogUtils.d(TAG, "handleNextDefaultExecutorChain | is canceled, so return");
            return;
        }

        if (mNextDefaultExecutorChain != null) {
            mNextDefaultExecutorChain.mCurTask.setInput(output);
            mNextDefaultExecutorChain.exec();
        } else {
            handleCompleted();
        }
    }

    private void handleCompleted() {
        if (mCompletedTask == null) {
            LogUtils.d(TAG, "handleCompleted | mCompletedTask == null, so return");
            return;
        }

        int completedThreadType = mCompletedTask.getThreadType();
        switch (completedThreadType) {
            case IThreadConstants.MAIN_THREAD:
                LogUtils.d(TAG, "handleCompleted | exec in main thread");
                new TaskWrapper<>(
                        new ChainInfo(mCompletedTask),
                        new TaskWrapper.ITaskWrapperListener<ChainInfo>() {
                            @Override
                            public void invoke(ChainInfo data) {
                                TaskThreadFactory.getMainThread().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            mCompletedTask.doTask(null);
                                        } catch (Throwable error) {
                                            LogUtils.e(TAG, "[handleCatchError]", error);
                                        }
                                    }
                                });
                            }
                        }).invoke();
                break;
            case IThreadConstants.SUB_THREAD:
                LogUtils.d(TAG, "handleCompleted | exec in sub thread");
                new TaskWrapper<>(
                        new ChainInfo(mCompletedTask),
                        new TaskWrapper.ITaskWrapperListener<ChainInfo>() {
                            @Override
                            public void invoke(ChainInfo data) {
                                mCompletedFuture = data.mTask.getTaskThreadFactory().getSingleThread().submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            mCompletedTask.doTask(null);
                                        } catch (Throwable error) {
                                            LogUtils.e(TAG, "[handleCatchError]", error);
                                        }
                                    }
                                });
                            }
                        }).invoke();
                break;
            default:
                LogUtils.d(TAG, "handleCompleted | exec in default thread");
                try {
                    mCompletedTask.doTask(null);
                } catch (Throwable throwable) {
                    LogUtils.e(TAG, "[handleCatchError]", throwable);
                }
                break;
        }
    }

    static class ChainInfo {
        MiniTask mTask;

        ChainInfo(MiniTask task) {
            mTask = task;
        }
    }
}
