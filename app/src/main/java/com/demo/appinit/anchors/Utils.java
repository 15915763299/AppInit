package com.demo.appinit.anchors;

import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.List;

public class Utils {

    public static void insertAfterTask(@NonNull BaseTask insert, @NonNull BaseTask targetTask){
        List<BaseTask> taskBehinds = targetTask.getBehindTasks();
        for(BaseTask behind: taskBehinds){
            behind.removeDepend(targetTask);
            insert.behindBy(behind);
        }
        targetTask.getBehindTasks().clear();
        insert.dependOn(targetTask);
    }

    /**
     * 比较两个 task
     * {@link BaseTask#getPriority()} 值高的，优先级高
     * {@link BaseTask#getExecuteTime()} 添加到队列的时间最早，优先级越高
     */
    public static int compareTask(@NonNull BaseTask task, @NonNull BaseTask o) {
        if (task.getPriority() < o.getPriority()) {
            return 1;
        }
        if (task.getPriority() > o.getPriority()) {
            return -1;
        }
//        if (task.getExecuteTime() < o.getExecuteTime()) {
//            return -1;
//        }
//        if (task.getExecuteTime() > o.getExecuteTime()) {
//            return 1;
//        }
//        return 0;
        return Long.compare(task.getExecuteTime(), o.getExecuteTime());
    }


    public static void assertMainThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("AnchorsManager#start should be invoke on MainThread!");
        }
    }
}
