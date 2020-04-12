package com.demo.appinit.anchors;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({TaskState.IDLE, TaskState.RUNNING, TaskState.FINISHED, TaskState.START, TaskState.RELEASE})
public @interface TaskState {
    /**
     * 静止
     */
    int IDLE = 0;
    /**
     * 启动，可能需要等待调度
     */
    int START = 1;
    /**
     * 运行
     */
    int RUNNING = 2;
    /**
     * 运行结束
     */
    int FINISHED = 3;
    /**
     * 释放
     */
    int RELEASE = 4;
}
