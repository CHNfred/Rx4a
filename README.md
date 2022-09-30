# Rx4a
The lightest Java asynchronous chained call library, the goal is to say goodbye to the use of threads and thread pools


###Description

* This type of task can be arranged and combined arbitrarily
* It needs to be placed at the end of all work task queues, and monitor the status of connected tasks

1. then：Default behavior, connecting multiple tasks
2. filter：Filter and output the results of the current task
3. map：Convert the results of the current task to another object
4. delayed：Delay the task for a specified time
5. wait：Wait for the task execution in the subtask group to complete
6. listener：Listen for event notifications in concurrent tasks
7. catchError：Listen to the task execution error event, and the subsequent task is not executing
8. whenComplete：Listen to all task completion events





### Note

1. he output result of the previous task is used as the request input of the next task
2. If the current task does not specify a thread, it will be executed in the thread of the previous task
3. The thread set by the previous task will be valid for the next task unless the thread is reset by the next task

Usage：

```
Rx4a rx4a = new Rx4a().wait(new ParallelTask(new MiniTask[]{
    new MiniTask(IThreadConstants.SUB_THREAD, input){
        @Override
        public Object doTask(Object input) {
            //do something
            notifyResult("1");
            return output;
        }
    },
    new MiniTask(){
        @Override
        public Object doTask(Object input) {
            //do something
            notifyResult("2");
            return output;
        }
    }
})).map(
    new MiniTask(){
        @Override
        public Object doTask(Object input) {
            //do something
            notifyResult("3");
            return output;
        }
    }
).filter(
    new MiniTask(){
        @Override
        public Object doTask(Object input) {
            //do something
            notifyResult("3");
            return output;
        }
    }
).then(
    new MiniTask(){
        @Override
        public Object doTask(Object input) {
            //do something
            return output;
        }
    }
).delayed(
    new MiniTask(IThreadConstants.SUB_THREAD, null, 2000){
        @Override
        public Object doDelayedTask(Object input) {
            //do something
            return output;
        }
    }
).listener(
    new ListenerTask(){
        @Override
        public void onProgress(Object input) {
            //do something
        }
    }
).catchError(
    new CatchErrorTask(){
        @Override
        public void onError(Throwable throwable) {
            //do something
        }
    }
).whenCompleted(
    new CompletedTask(){
        @Override
        public void onCompleted() {
            //do something
            LogUtils.d("test", "all completed, threadName = " +             Thread.currentThread().getName() + ", isCancel = " +             isCancel());
        }
    }
);
rx4a.start();
//rx4a.cancel();
```
![Overview](https://github.com/CHNfred/Rx4a/blob/main/docs/Overview.bmp)

