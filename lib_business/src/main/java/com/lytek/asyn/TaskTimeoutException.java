package com.lytek.asyn;

/**
 * author:Fred
 * date:2022/10/9 14:18
 * describe:task timeout exception
 */
public class TaskTimeoutException extends Exception {
    private Object mInput;

    public TaskTimeoutException(Object input) {
        super("the task execution timed out", null);
        mInput = input;
    }

    public Object getInput() {
        return mInput;
    }
}
