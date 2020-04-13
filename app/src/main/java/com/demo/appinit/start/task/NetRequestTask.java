package com.demo.appinit.start.task;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;

import androidx.annotation.NonNull;

import com.demo.appinit.App;
import com.demo.appinit.anchors.BaseTask;

/**
 * @author 尉迟涛
 * create time : 2020/4/12 14:47
 * IdleHandler会在MessageQueue的next方法中的无限循环中执行，在执行完message之后
 * 如果当前循环没有 return msg，即没有执行message，就会执行IdleHandler
 * 即在主线程空闲的时候，会执行IdleHandler
 * 参考：https://www.jianshu.com/p/a1d945c4f5a6
 */
public class NetRequestTask extends BaseTask {

    private Handler handler = new Handler(Looper.getMainLooper());
    private long asyncTime;

    public NetRequestTask(@NonNull String id, long asyncTime) {
        super(id, true);
        this.asyncTime = asyncTime;
    }

    @Override
    protected void run(String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                App.getApp().getMainLooper().getQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        net();
                        //返回false就会被从mIdleHandlers中移除，不会被再次执行
                        return false;
                    }
                });
            } catch (Throwable ignore) {
            }
        } else {
            net();
        }
    }

    private void net() {
        try {
            Thread.sleep(asyncTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.e("SimulateNetWork", Thread.currentThread().getName() + " response");
            }
        });
    }

//    private static class SimulateNetWork extends AsyncTask<Long, Void, Void> {
//
//        /**
//         * 模拟网络请求
//         *
//         * @param longs 异步等待时间
//         */
//        @Override
//        protected Void doInBackground(Long... longs) {
//            try {
//                Thread.sleep(longs[0]);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            Log.e("SimulateNetWork", "response");
//        }
//    }


}
