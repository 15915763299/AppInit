package com.demo.appinit.start.task;

import android.util.Log;

import com.demo.appinit.anchors.BaseTask;

/**
 * 默认的空实现的 task，避免空异常
 */
public class EmptyTask extends BaseTask {

    public EmptyTask(String id) {
        super(id);
    }

    @Override
    protected void run(String name) {
        Log.e("EmptyTask", name);
    }
}
