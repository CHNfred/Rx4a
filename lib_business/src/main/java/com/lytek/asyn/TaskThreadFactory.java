package com.lytek.asyn;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
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
    private static Handler mMainThread;
    private static ExecutorService mSingleThread;
    private static ExecutorService mMultiThread;
    private static ExecutorService mListenerThread;
    private static int sSingleThreadNum;
    private static int sMultiThreadNum;
    private static int sListenerThreadNum;

    public static Handler getMainThread() {
        if (mMainThread == null) {
            mMainThread = UiHandler.getUIHandler();
            LogUtils.d(TAG, "getMainThread | init mMainThread = " + mMainThread);
        }

        LogUtils.d(TAG, "getMainThread | get mMainThread = " + mMainThread);
        return mMainThread;
    }

    public static ExecutorService getSingleThread() {
        if (mSingleThread == null) {
            sSingleThreadNum = 0;
            mSingleThread = createExecutor(0, SINGLE_THREAD_NAME, 1, 1,
                    Integer.MAX_VALUE, null);
            LogUtils.d(TAG, "getSingleThread | init mSingleThread = " + mSingleThread);
        }
        LogUtils.d(TAG, "getSingleThread | get mSingleThread = " + mSingleThread);
        return mSingleThread;
    }

    public static ExecutorService getListenerThread() {
        if (mListenerThread == null) {
            sListenerThreadNum = 0;
            mListenerThread = createExecutor(1, LISTENER_THREAD_NAME, 1, 1,
                    Integer.MAX_VALUE, null);
            LogUtils.d(TAG, "getListenerThread | init mListenerThread = " + mListenerThread);
        }
        LogUtils.d(TAG, "getListenerThread | get mListenerThread = " + mListenerThread);
        return mListenerThread;
    }

    public static ExecutorService getMultiThread() {
        if (mMultiThread == null) {
            sMultiThreadNum = 0;
            mMultiThread = createExecutor(2, MULTI_THREAD_NAME, 8, 16,
                    Integer.MAX_VALUE, null);
            LogUtils.d(TAG, "getMultiThread | init mMultiThread = " + mMultiThread);
        }
        LogUtils.d(TAG, "getMultiThread | get mMultiThread = " + mMultiThread);
        return mMultiThread;
    }

    private static ExecutorService createExecutor(final int type, final String threadName,
                                                  int corePoolSize,
                                                  int maximumPoolSize,
                                                  int queueCapacity,
                                                  RejectedExecutionHandler executionHandler) {
        LogUtils.d(TAG, "createExecutor | type = " + type + ", threadName = " + threadName
                + ", corePoolSize = " + corePoolSize
                + ", maximumPoolSize = " + maximumPoolSize
                + ", queueCapacity = " + queueCapacity
                + ", executionHandler = " + executionHandler);
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(queueCapacity),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        String threadNum = "";
                        switch (type) {
                            case 0:
                                threadNum = String.valueOf(sSingleThreadNum++);
                                break;
                            case 1:
                                threadNum = String.valueOf(sListenerThreadNum++);
                                break;
                            case 2:
                                threadNum = String.valueOf(sMultiThreadNum++);
                                break;
                        }
                        final String finalThreadNum = threadNum;
                        return new Thread(r, threadName + "_" + finalThreadNum);
                    }
                },
                executionHandler == null ? new ThreadPoolExecutor.AbortPolicy() : executionHandler);
    }

}
