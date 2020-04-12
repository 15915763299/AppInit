package com.demo.appinit.anchors;

public interface TaskListener {

    void onStart(BaseTask task);

    void onRunning(BaseTask task);

    void onFinish(BaseTask task);

    void onRelease(BaseTask task);
}
