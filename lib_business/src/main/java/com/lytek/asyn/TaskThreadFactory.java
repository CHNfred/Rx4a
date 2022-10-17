package com.lytek.asyn;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * author:Fred
 * date:2022/9/26 14:10
 * describe:thread factory
 */
public class TaskThreadFactory {
    private static final String TAG = "TaskThreadFactory";
    private static final String SINGLE_THREAD_NAME = "TaskSingleThread";
    private static final String MULTI_THREAD_NAME = "TaskMultiThread";
    private static final String LISTENER_THREAD_NAME = "TaskListenerThread";
    private static final String TIMEOUT_THREAD_NAME = "TaskTimeoutThread";
    private static Handler mMainThread;
    private ExecutorService mSingleThread;
    private ExecutorService mMultiThread;
    private ExecutorService mListenerThread;
    private ExecutorService mTimeoutThread;
    private int sSingleThreadNum;
    private int sMultiThreadNum;
    private int sListenerThreadNum;
    private int sTimeoutThreadNum;

    public static Handler getMainThread() {
        if (mMainThread == null) {
            synchronized (TaskThreadFactory.class) {
                if (mMainThread == null) {
                    mMainThread = UiHandler.getUIHandler();
                    LogUtils.d(TAG, "getMainThread | init mMainThread = " + mMainThread);
                }
            }
        }
        LogUtils.d(TAG, "getMainThread | get mMainThread = " + mMainThread);
        return mMainThread;
    }

    public ExecutorService getSingleThread() {
        if (mSingleThread == null) {
            synchronized (TaskThreadFactory.class) {
                if (mSingleThread == null) {
                    sSingleThreadNum = 0;
                    mSingleThread = createExecutor(0, SINGLE_THREAD_NAME, 1, 1,
                            null);
                    LogUtils.d(TAG, "getSingleThread | init mSingleThread = " + mSingleThread);
                }
            }
        }
        LogUtils.d(TAG, "getSingleThread | get mSingleThread = " + mSingleThread);
        return mSingleThread;
    }

    public ExecutorService getListenerThread() {
        if (mListenerThread == null) {
            synchronized (TaskThreadFactory.class) {
                if (mListenerThread == null) {
                    sListenerThreadNum = 0;
                    mListenerThread = createExecutor(1, LISTENER_THREAD_NAME, 1, 1,
                            null);
                    LogUtils.d(TAG, "getListenerThread | init mListenerThread = " + mListenerThread);
                }
            }
        }
        LogUtils.d(TAG, "getListenerThread | get mListenerThread = " + mListenerThread);
        return mListenerThread;
    }

    public ExecutorService getTimeoutThread() {
        if (mTimeoutThread == null) {
            synchronized (TaskThreadFactory.class) {
                if (mTimeoutThread == null) {
                    sTimeoutThreadNum = 0;
                    mTimeoutThread = createExecutor(2, TIMEOUT_THREAD_NAME, 1, Integer.MAX_VALUE,
                            null);
                    LogUtils.d(TAG, "getTimeoutThread | init mTimeoutThread = " + mTimeoutThread);
                }
            }
        }
        LogUtils.d(TAG, "getTimeoutThread | get mTimeoutThread = " + mTimeoutThread);
        return mTimeoutThread;
    }

    public ExecutorService getMultiThread() {
        if (mMultiThread == null) {
            synchronized (TaskThreadFactory.class) {
                if (mMultiThread == null) {
                    sMultiThreadNum = 0;
                    mMultiThread = createExecutor(3, MULTI_THREAD_NAME, 8, 16
                            , null);
                    LogUtils.d(TAG, "getMultiThread | init mMultiThread = " + mMultiThread);
                }
            }
        }
        LogUtils.d(TAG, "getMultiThread | get mMultiThread = " + mMultiThread);
        return mMultiThread;
    }

    private ExecutorService createExecutor(final int type, final String threadName,
                                           int corePoolSize,
                                           int maximumPoolSize,
                                           RejectedExecutionHandler executionHandler) {
        LogUtils.d(TAG, "createExecutor | type = " + type + ", threadName = " + threadName
                + ", corePoolSize = " + corePoolSize
                + ", maximumPoolSize = " + maximumPoolSize
                + ", executionHandler = " + executionHandler);
        ThreadPoolExecutor threadPoolExecutor = null;
        int threadTimeoutMin = 1;
        switch (type) {
            case 0: // single thread
                threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                        threadTimeoutMin, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
                        new ThreadFactory() {
                            @Override
                            public Thread newThread(Runnable r) {
                                String threadNum = String.valueOf(sSingleThreadNum++);
                                return new Thread(r, threadName + "_" + threadNum);
                            }
                        },
                        executionHandler == null ? new ThreadPoolExecutor.AbortPolicy() : executionHandler);
                break;
            case 1: // listener thread
                threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                        threadTimeoutMin, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
                        new ThreadFactory() {
                            @Override
                            public Thread newThread(Runnable r) {
                                String threadNum = String.valueOf(sListenerThreadNum++);
                                return new Thread(r, threadName + "_" + threadNum);
                            }
                        },
                        executionHandler == null ? new ThreadPoolExecutor.AbortPolicy() : executionHandler);
            case 2: // timeout thread
                threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                        threadTimeoutMin, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),
                        new ThreadFactory() {
                            @Override
                            public Thread newThread(Runnable r) {
                                String threadNum = String.valueOf(sTimeoutThreadNum++);
                                return new Thread(r, threadName + "_" + threadNum);
                            }
                        },
                        executionHandler == null ? new ThreadPoolExecutor.AbortPolicy() : executionHandler);
                break;
            case 3: // multi thread
                threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                        threadTimeoutMin, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
                        new ThreadFactory() {
                            @Override
                            public Thread newThread(Runnable r) {
                                String threadNum = String.valueOf(sMultiThreadNum++);
                                return new Thread(r, threadName + "_" + threadNum);
                            }
                        },
                        executionHandler == null ? new ThreadPoolExecutor.AbortPolicy() : executionHandler);
                break;
        }

        if (threadPoolExecutor != null) {
            try {
                threadPoolExecutor.allowCoreThreadTimeOut(true);
            } catch (Throwable throwable) {
                LogUtils.e(TAG, "[createExecutor]", throwable);
            }
        }
        return threadPoolExecutor;
    }

}
