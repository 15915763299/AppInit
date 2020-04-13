package com.demo.appinit.anchors;


import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ？
 */
public class AnchorsManager {

    private volatile static AnchorsManager sInstance = null;
    private boolean debuggable = false;
    private Set<String> anchorTaskIds = new HashSet<>();

    public static synchronized AnchorsManager getInstance() {
        if (sInstance == null) {
            synchronized (AnchorsManager.class) {
                if (sInstance == null) {
                    sInstance = new AnchorsManager();
                }
            }
        }
        return sInstance;
    }

    public AnchorsManager debuggable(boolean debuggable) {
        this.debuggable = debuggable;
        return this;
    }

    /**
     * 扩展支持 https://github.com/YummyLau/Anchors/issues/7 暂停机制
     * 调用前须知：
     * 1. 请充分理解 anchor 的作用并明白，为何 application sleep 频繁等待代码块执行的原因
     * 2. 如果调用 requestBlockWhenFinish 则意味着任务链在 task 执行完毕之后会进入等待阶段，如果此时等待的 task 在[初始节点，Anchors]链中则可能导致界面卡主
     * 3. 在调用 requestBlockWhenFinish 设置等待任务的前提下务必保证 anchors 已经解锁 或者 任务链上没有 anchors。
     *
     * @param task block目标task
     */
    @Nullable
    public LockableAnchor requestBlockWhenFinish(BaseTask task) {
        return requestBlockWhenFinishInner(task);
    }

    @Nullable
    LockableAnchor requestBlockWhenFinishInner(BaseTask task) {
        if (task != null && !TextUtils.isEmpty(task.getId())) {
            LockableAnchor lockableAnchor = new LockableAnchor(AnchorsRuntime.getHandler());
            LockableTask lockableTask = new LockableTask(task, lockableAnchor);
            Utils.insertAfterTask(lockableTask, task);
            return lockableAnchor;
        }
        return null;
    }

    public AnchorsManager addAnchor(String taskId) {
        if (!TextUtils.isEmpty(taskId)) {
            anchorTaskIds.add(taskId);
        }
        return this;
    }

    public AnchorsManager addAnchors(String... taskIds) {
        if (taskIds != null && taskIds.length > 0) {
            anchorTaskIds.addAll(Arrays.asList(taskIds));
        }
        return this;
    }

    private void syncConfigInfoToRuntime() {
        AnchorsRuntime.clear();
        AnchorsRuntime.openDebug(debuggable);
        AnchorsRuntime.addAnchorTasks(anchorTaskIds);
        debuggable = false;
        anchorTaskIds.clear();
    }

    @MainThread
    public synchronized void start(@NonNull BaseTask task) {
        //Utils.assertMainThread();
        //if (task == null) {
        //    throw new RuntimeException("can no run a task that was null !");
        //}
        syncConfigInfoToRuntime();
        BaseTask startTask = task;
        if (startTask instanceof Project) {
            startTask = ((Project) task).getStartTask();
        }
        AnchorsRuntime.traversalDependenciesAndInit(startTask);
        boolean logEnd = logStartWithAnchorsInfo();

        //如果是锚点task，会被记录到AnchorsRuntime中
        startTask.start();
        //运行所有锚点task
        while (AnchorsRuntime.hasAnchorTasks()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (AnchorsRuntime.hasRunTasks()) {
                AnchorsRuntime.tryRunBlockRunnable();
            }
        }
        if (logEnd) {
            logEndWithAnchorsInfo();
        }
    }

    /**
     * 打印锚点信息
     *
     * @return
     */
    private static boolean logStartWithAnchorsInfo() {
        if (!AnchorsRuntime.debuggable()) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        boolean hasAnchorTask = AnchorsRuntime.hasAnchorTasks();
        if (hasAnchorTask) {
            stringBuilder.append(Constants.HAS_ANCHOR);
            stringBuilder.append("( ");
            for (String taskId : AnchorsRuntime.getAnchorTasks()) {
                stringBuilder.append("\"" + taskId + "\" ");
            }
            stringBuilder.append(")");
        } else {
            stringBuilder.append(Constants.NO_ANCHOR);
        }
        Logger.d(Constants.ANCHORS_INFO_TAG, stringBuilder.toString());
        return hasAnchorTask;
    }

    /**
     * 打印锚点信息
     */
    private static void logEndWithAnchorsInfo() {
        if (!AnchorsRuntime.debuggable()) {
            return;
        }
        Logger.d(Constants.ANCHORS_INFO_TAG, Constants.ANCHOR_RELEASE);
    }
}
