package com.demo.appinit.start.task;

import android.util.Log;

import com.blankj.rxbus.RxBus;
import com.demo.appinit.App;
import com.demo.appinit.RxBusTags;
import com.demo.appinit.anchors.BaseTask;

import java.util.concurrent.CountDownLatch;

/**
 * 等待动态授权的启动任务，这样可以阻塞初始化 sdk 的启动链
 * 从而在申请到后继续初始化需要相应权限的 sdk 的初始化
 */
public class AwaitPermStartTask extends BaseTask {

    private CountDownLatch awaitLatch;

    public AwaitPermStartTask(String id) {
        super(id, true);
    }

    @Override
    protected void run(String name) {
        Log.e("AwaitPermStartTask", "start");
        // 已经有权限可以结束了
        if (App.getApp().checkPermission()) {
            return;
        }
        awaitLatch = new CountDownLatch(1);
        RxBus.getDefault().subscribe(this, RxBusTags.SDK_PERM_GRANTED_CONTINUE, new RxBus.Callback<Boolean>() {
            @Override
            public void onEvent(Boolean isContinue) {
                Log.e("AwaitPermStartTask", "isContinue: " + isContinue);
                if (!isContinue) {
                    AwaitPermStartTask.this.cleanBehind();
                }
                if (awaitLatch != null) {
                    awaitLatch.countDown();
                }
            }
        });
        try {
            awaitLatch.await();
        } catch (Exception ignore) {

        }
        Log.e("AwaitPermStartTask", "end");
    }
}
