package com.lytek.asyn;

/**
 * author:Fred
 * date:2022/9/23 14:47
 * describe:<b>The lightest Java asynchronous chained call library,
 * the goal is to say goodbye to the use of threads and thread pools<b/>
 */
public class Rx4a {
    private static final String TAG = "Rx4a";
    private static final String PREFIX = "Rx4a_";
    private DefaultExecutorChain mFirstExecutorChain;
    private DefaultExecutorChain mNextExecutorChain;

    static {
        LogUtils.setPreTag(PREFIX);
    }

    public static void config(boolean isDebug) {
        config(PREFIX, isDebug);
    }

    public static void config(String tagPrefix, boolean isDebug) {
        tagPrefix = tagPrefix.endsWith("_") ? tagPrefix : tagPrefix + "_";
        LogUtils.setPreTag(tagPrefix);
        LogUtils.setDebugLogging(isDebug);
    }

    public Rx4a then(MiniTask task) {
        LogUtils.d(TAG, "then");
        return addDefaultTask(task);
    }

    public Rx4a delayed(DelayedTask task) {
        LogUtils.d(TAG, "delayed");
        return addDefaultTask(task);
    }

    public Rx4a whenComplete(CompletedTask task) {
        LogUtils.d(TAG, "whenComplete");
        doCompletedTask(task);
        return this;
    }

    public Rx4a wait(ParallelTask task) {
        LogUtils.d(TAG, "wait");
        return addDefaultTask(task);
    }

    public Rx4a filter(MiniTask task) {
        LogUtils.d(TAG, "filter");
        return addDefaultTask(task);
    }

    public Rx4a map(MiniTask task) {
        LogUtils.d(TAG, "map");
        return addDefaultTask(task);
    }

    public Rx4a listener(ListenerTask task) {
        LogUtils.d(TAG, "listener");
        doListenerTask(task);
        return this;
    }

    public Rx4a cacheError(CatchErrorTask task) {
        LogUtils.d(TAG, "cacheError");
        doCacheErrorTask(task);
        return this;
    }

    public void start() {
        if (mFirstExecutorChain == null) {
            LogUtils.d(TAG, "start | mFirstExecutorChain == null, so return");
            return;
        }

        mFirstExecutorChain.exec();
    }

    public void cancel() {
        LogUtils.d(TAG, "cancel");
        doCancel();
    }

    private Rx4a addDefaultTask(MiniTask task) {
        if (task == null) {
            LogUtils.d(TAG, "addDefaultTask | task == null, so return");
            return this;
        }

        if (mFirstExecutorChain == null) {
            LogUtils.d(TAG, "addDefaultTask | because mFirstExecutorChain == null, " +
                    "so set mFirstExecutorChain to mCurExecutorChain");
            mFirstExecutorChain = new DefaultExecutorChain(task);
            mNextExecutorChain = mFirstExecutorChain;
            return this;
        }

        if (mNextExecutorChain.getNextTask() == null) {
            LogUtils.d(TAG, "addDefaultTask | because mFirstExecutorChain != null, " +
                    "and mCurExecutorChain's NextTask is null, " +
                    "so set the task to mCurExecutorChain's NextTask");
            mNextExecutorChain.setNextTask(task);
        } else {
            LogUtils.d(TAG, "addDefaultTask | because mFirstExecutorChain != null, " +
                    "and mCurExecutorChain's NextTask isn't null, " +
                    "so new DefaultExecutorChain and set task to it");
            DefaultExecutorChain tempDefaultExecutorChain = new DefaultExecutorChain(task);
            mNextExecutorChain.setNextDefaultExecutorChain(tempDefaultExecutorChain);
            mNextExecutorChain = tempDefaultExecutorChain;
        }

        return this;
    }

    private void doCancel() {
        if (mFirstExecutorChain == null) {
            LogUtils.d(TAG, "doCancel | mFirstExecutorChain == null, so return");
            return;
        }
        mFirstExecutorChain.cancel();

        DefaultExecutorChain nextChain = mFirstExecutorChain.getNextDefaultExecutorChain();
        while (nextChain != null) {
            nextChain.cancel();
            nextChain = nextChain.getNextDefaultExecutorChain();
        }

        LogUtils.d(TAG, "doCancel | cancel completed");
    }

    private void doCompletedTask(CompletedTask task) {
        if (mFirstExecutorChain == null) {
            LogUtils.d(TAG, "doCompletedTask | mFirstExecutorChain == null, so return");
            return;
        }
        mFirstExecutorChain.setCompletedTask(task);

        DefaultExecutorChain nextChain = mFirstExecutorChain.getNextDefaultExecutorChain();
        while (nextChain != null) {
            nextChain.setCompletedTask(task);
            nextChain = nextChain.getNextDefaultExecutorChain();
        }
        LogUtils.d(TAG, "doCompletedTask | completed");
    }

    private void doCacheErrorTask(CatchErrorTask task) {
        if (mFirstExecutorChain == null) {
            LogUtils.d(TAG, "doCacheErrorTask | mFirstExecutorChain == null, so return");
            return;
        }
        mFirstExecutorChain.setCatchErrorTask(task);

        DefaultExecutorChain nextChain = mFirstExecutorChain.getNextDefaultExecutorChain();
        while (nextChain != null) {
            nextChain.setCatchErrorTask(task);
            nextChain = nextChain.getNextDefaultExecutorChain();
        }
        LogUtils.d(TAG, "doCacheErrorTask | completed");
    }

    private void doListenerTask(ListenerTask task) {
        if (mFirstExecutorChain == null) {
            LogUtils.d(TAG, "doListenerTask | mFirstExecutorChain == null, so return");
            return;
        }
        mFirstExecutorChain.setListenerTask(task);

        DefaultExecutorChain nextChain = mFirstExecutorChain.getNextDefaultExecutorChain();
        while (nextChain != null) {
            nextChain.setListenerTask(task);
            nextChain = nextChain.getNextDefaultExecutorChain();
        }
        LogUtils.d(TAG, "doListenerTask | completed");
    }
}
