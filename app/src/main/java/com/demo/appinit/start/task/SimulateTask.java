package com.demo.appinit.start.task;

import androidx.annotation.NonNull;

import com.demo.appinit.anchors.BaseTask;

/**
 * @author 尉迟涛
 * create time : 2020/4/12 13:56
 * description : 模拟启动任务
 */
public class SimulateTask extends BaseTask {

    private long exeTime;

    public SimulateTask(@NonNull String id, long exeTime, boolean isAsync) {
        super(id, isAsync);
        this.exeTime = exeTime;
    }

    @Override
    protected void run(String name) {
        try {
            Thread.sleep(exeTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
