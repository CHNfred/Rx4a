package com.lytek.asyn;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * author:Fred
 * date:2022/9/25 17:35
 * describe:<b>parallel task, gather the results of multiple subtasks, {@link Rx4a#wait}<b/>
 */
public class ParallelTask extends MiniTask {
    private static final String TAG = "ParallelTask";
    private final MiniTask[] mTasks;
    private final ExecutorService mMultiThread;
    private final CountDownLatch mCountDownLatch;
    private final Object[] mParallelOuts;
    private final Future<?>[] mFutures;

    public ParallelTask(MiniTask[] tasks) {
        this(null, tasks);
    }

    public ParallelTask(Object input, MiniTask[] tasks) {
        //set as sub thread to cancel the sub task group in time
        this(IThreadConstants.SUB_THREAD, input, tasks);
    }

    public ParallelTask(int threadType, MiniTask[] tasks) {
        //set as sub thread to cancel the sub task group in time
        this(threadType, null, tasks);
    }

    public ParallelTask(int threadType, Object input, MiniTask[] tasks)
            throws NullPointerException, IllegalArgumentException {
        super(threadType, input);

        if (tasks == null) {
            throw new NullPointerException("AbsTask[] is not null!");
        }

        if (tasks.length == 0) {
            throw new IllegalArgumentException("AbsTask[] length is empty!");
        }

        mTasks = tasks;
        mCountDownLatch = new CountDownLatch(tasks.length);
        mParallelOuts = new Object[tasks.length];
        mFutures = new Future[tasks.length];
        mMultiThread = TaskThreadFactory.getMultiThread();
    }

    @Override
    public void setCancel(boolean cancel) {
        super.setCancel(cancel);
        for (MiniTask task : mTasks) {
            task.setCancel(cancel);
        }

        if (!cancel) {
            return;
        }
        for (Future<?> future : mFutures) {
            if (future == null) {
                continue;
            }

            future.cancel(true);
        }
    }

    @Override
    void setListenerTask(MiniTask task) {
        super.setListenerTask(task);
        for (MiniTask miniTask : mTasks) {
            miniTask.setListenerTask(task);
        }
    }

    @Override
    public Object doTask(Object input) {
        LogUtils.d(TAG, "doTask | ParallelTask is running in " + Thread.currentThread().getName());
        for (int i = 0; i < mTasks.length; i++) {
            LogUtils.d(TAG, "doTask | run " + i + " task");
            final MiniTask task = mTasks[i];
            int threadType = task.getThreadType();
            switch (threadType) {
                case IThreadConstants.MAIN_THREAD:
                    handleMainThread(task, i);
                    break;
                case IThreadConstants.SUB_THREAD:
                    handleSubThread(task, i);
                    break;
                default:
                    try {
                        Object out = task.doTask(task.getInput());
                        mParallelOuts[i] = out;
                    } catch (Throwable throwable) {
                        LogUtils.e(TAG, "[doTask]", throwable);
                    } finally {
                        mCountDownLatch.countDown();
                    }
                    break;
            }
        }

        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            LogUtils.e(TAG, "[doTask]", e);
        }

        LogUtils.d(TAG, "doTask | ParallelTask is completed");
        return mParallelOuts;
    }

    private void handleMainThread(final MiniTask task, int index) {
        LogUtils.d(TAG, "handleMainThread | in main thread, index = " + index);
        new TaskWrapper<>(
                new TaskInfo(task, index),
                new TaskWrapper.ITaskWrapperListener<TaskInfo>() {
                    @Override
                    public void invoke(final TaskInfo data) {
                        TaskThreadFactory.getMainThread().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Object out = data.mTask.doTask(data.mTask.getInput());
                                    mParallelOuts[data.mIndex] = out;
                                } catch (Throwable throwable) {
                                    LogUtils.d(TAG, "[handleMainThread]", throwable);
                                } finally {
                                    mCountDownLatch.countDown();
                                }
                            }
                        });
                    }
                }).invoke();
    }

    private void handleSubThread(final MiniTask task, int index) {
        LogUtils.d(TAG, "handleSubThread | in sub thread, index = " + index);
        new TaskWrapper<>(
                new TaskInfo(task, index),
                new TaskWrapper.ITaskWrapperListener<TaskInfo>() {
                    @Override
                    public void invoke(final TaskInfo data) {
                        mFutures[data.mIndex] = mMultiThread.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Object out = data.mTask.doTask(data.mTask.getInput());
                                    mParallelOuts[data.mIndex] = out;
                                } catch (Throwable throwable) {
                                    LogUtils.e(TAG, "[handleSubThread]", throwable);
                                } finally {
                                    mCountDownLatch.countDown();
                                }
                            }
                        });
                    }
                }).invoke();
    }

    static class TaskInfo {
        MiniTask mTask;
        int mIndex;

        public TaskInfo(MiniTask task, int index) {
            mTask = task;
            mIndex = index;
        }
    }

}
