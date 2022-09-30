package com.lytek.asyn;

/**
 * author:Fred
 * date:2022/9/23 17:27
 * describe:<b>task progress listener, for parallel task scenarios, {@link ParallelTask}<b/>
 */
public interface ITaskListener {

    void onProgress(Object input);

}
