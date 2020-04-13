package com.demo.appinit.start.task;

import android.util.Log;

import androidx.annotation.NonNull;

import com.demo.appinit.anchors.BaseTask;

/**
 * 默认的空实现的 task，避免空异常
 */
public class EmptyTask extends BaseTask {

    public EmptyTask(String id) {
        super(id);
    }

    public EmptyTask(@NonNull String id, boolean async) {
        super(id, async);
    }

    @Override
    protected void run(String name) {
        Log.e("EmptyTask", name);
    }
}
