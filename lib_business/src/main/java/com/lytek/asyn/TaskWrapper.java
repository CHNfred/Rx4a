package com.lytek.asyn;

/**
 * author:Fred
 * date:2022/9/26 14:10
 * describe: task wrapper
 */
public class TaskWrapper<T> {
    private final T mData;
    private final ITaskWrapperListener<T> mTaskWrapperListener;

    public TaskWrapper(T data, ITaskWrapperListener<T> listener) {
        mData = data;
        mTaskWrapperListener = listener;
    }

    public void invoke() {
        if (mTaskWrapperListener != null) {
            mTaskWrapperListener.invoke(mData);
        }
    }

    public interface ITaskWrapperListener<T> {
        void invoke(T data);
    }
}
