package com.lytek.asyn.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lytek.asyn.CatchErrorTask;
import com.lytek.asyn.CompletedTask;
import com.lytek.asyn.DelayedTask;
import com.lytek.asyn.IThreadConstants;
import com.lytek.asyn.ListenerTask;
import com.lytek.asyn.LogUtils;
import com.lytek.asyn.MiniTask;
import com.lytek.asyn.ParallelTask;
import com.lytek.asyn.Rx4a;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private LinearLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = findViewById(R.id.container);
        Rx4a.config("Rx4a", true);

        addBtn("test", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
    }

    private void addBtn(String title, View.OnClickListener clickListener) {
        Button button = new Button(this);
        button.setText(title);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(clickListener);
        mContainer.addView(button);
    }

    void test() {

        Rx4a rx4a = new Rx4a().wait(new ParallelTask(new MiniTask[]{
                new MiniTask(IThreadConstants.SUB_THREAD, null) {
                    @Override
                    public Object doTask(Object input) {
                        LogUtils.d("Fred", "1, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel() + (2 / 0));
                        notifyResult("1");
                        return "1";
                    }
                },
                new MiniTask(IThreadConstants.SUB_THREAD, null) {
                    @Override
                    public Object doTask(Object input) {
                        SystemClock.sleep(2000);
                        LogUtils.d("Fred", "2, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel());
                        notifyResult("2");
                        return "2";
                    }
                },
                new MiniTask() {
                    @Override
                    public Object doTask(Object input) {
                        LogUtils.d("Fred", "3, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel());
                        return "3";
                    }
                },
                new MiniTask() {
                    @Override
                    public Object doTask(Object input) {
                        LogUtils.d("Fred", "4, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel());
                        return "4";
                    }
                },
                new MiniTask() {
                    @Override
                    public Object doTask(Object input) {
                        LogUtils.d("Fred", "5, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel());
                        return "5";
                    }
                },
                new MiniTask() {
                    @Override
                    public Object doTask(Object input) {
                        LogUtils.d("Fred", "6, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel());
                        return "6";
                    }
                },
                new MiniTask(IThreadConstants.SUB_THREAD, null) {
                    @Override
                    public Object doTask(Object input) {
                        LogUtils.d("Fred", "7, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel());
                        notifyResult("7");
                        return "7";
                    }
                },
                new MiniTask(IThreadConstants.SUB_THREAD, null) {
                    @Override
                    public Object doTask(Object input) {
                        LogUtils.d("Fred", "8, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel());
                        notifyResult("8");
                        return "8";
                    }
                },
                new MiniTask(IThreadConstants.SUB_THREAD, null) {
                    @Override
                    public Object doTask(Object input) {
                        LogUtils.d("Fred", "9, threadName = " + Thread.currentThread().getName()
                                + ", input = " + input + ", isCancel = " + isCancel());
                        notifyResult("9");
                        return "9";
                    }
                }
        })).map(new MiniTask() {
            @Override
            public Object doTask(Object input) {
                Object[] objects = (Object[]) input;
                Object[] temp = new Object[objects.length];
                for (int i = objects.length - 1, j = 0; i >= 0; i--, j++) {
                    temp[j] = objects[i];
                }
                return temp;
            }
        }).filter(new MiniTask() {
            @Override
            public Object doTask(Object input) {
                Object[] objects = (Object[]) input;
                List<Object> temp = new ArrayList<>();
                for (Object o : objects) {
                    if (o != null && Integer.parseInt(o.toString()) % 2 == 0) {
                        temp.add(o);
                    }
                }
                notifyResult("from Filter task");
                return temp.toArray();
            }
        }).then(new MiniTask(IThreadConstants.SUB_THREAD, null) {
            @Override
            public Object doTask(Object input) {
                Object[] objects = (Object[]) input;
                String result = "";
                for (Object o : objects) {
                    result += o;
                }
                LogUtils.d("Fred", "Summary Results, threadName = " + Thread.currentThread().getName()
                        + ", result = " + result + ", isCancel = " + isCancel());
                return null;
            }
        }).delayed(new DelayedTask(IThreadConstants.SUB_THREAD, null, 2000) {
            @Override
            public Object execTask(Object input) {
                notifyResult("from Delayed task");
                LogUtils.d("Fred", "DelayedTask, threadName = " + Thread.currentThread().getName()
                        + ", input = " + input + ", isCancel = " + isCancel());
                return null;
            }
        }).then(new MiniTask() {
            @Override
            public Object doTask(Object input) {
                int a = 3;
                LogUtils.d("Fred", "error = " + (2 / (a - 3)));
                return null;
            }
        }).then(new MiniTask() {
            @Override
            public Object doTask(Object input) {
                LogUtils.d("Fred", "See if it can be implemented, threadName = " + Thread.currentThread().getName()
                        + ", input = " + input + ", isCancel = " + isCancel());
                return null;
            }
        }).listener(new ListenerTask(IThreadConstants.SUB_THREAD, null) {
            @Override
            public void onProgress(Object input) {
                LogUtils.e("Fred", "onProgress | threadName = " + Thread.currentThread().getName()
                        + ", input = " + input + ", isCancel = " + isCancel());
            }
        }).catchError(new CatchErrorTask() {
            @Override
            public void onError(Throwable throwable) {
                LogUtils.d("Fred", "occur error, threadName = " + Thread.currentThread().getName()
                        + ", isCancel = " + isCancel() + ", " + throwable.getMessage());
            }
        }).whenComplete(new CompletedTask(IThreadConstants.MAIN_THREAD, null) {
            @Override
            public void onCompleted() {
                LogUtils.d("Fred", "all completed, threadName = " + Thread.currentThread().getName()
                        + ", isCancel = " + isCancel());
            }
        });
        rx4a.start();
//        rx4a.cancel();
    }
}