package com.demo.appinit.anchors;

import android.os.Trace;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * note and modify by 尉迟涛 on 2020/04/11
 * 做了一些修改，加了些注释
 */
public abstract class BaseTask implements Runnable, Comparable<BaseTask> {

    public static final int DEFAULT_PRIORITY = 0;

    /**
     * 初始参数：唯一标识
     */
    private String mId;
    /**
     * 初始参数：是否是异步存在，默认false，{@link AnchorsRuntime#executeTask(BaseTask)}
     */
    private boolean isAsyncTask;
    /**
     * 初始参数：优先级，默认{@link #DEFAULT_PRIORITY}
     */
    private int mPriority;
    /**
     * 初始参数：状态，默认{@link TaskState#IDLE}静止
     */
    @TaskState
    private int mState;

    /**
     * 开始执行时间
     */
    private long mExecuteTime;

    /**
     * 后置任务
     */
    private List<BaseTask> behindTasks = new ArrayList<>();
    /**
     * 前置条件
     */
    private volatile Set<BaseTask> dependTasks = new HashSet<>();

    /**
     * 监听器，实现接口{@link TaskListener}
     */
    private List<TaskListener> taskListeners = new ArrayList<>();
    /**
     * 日志打印，实现接口{@link TaskListener}
     */
    private TaskListener logTaskListener = new LogTaskListener();


    public BaseTask(@NonNull String id) {
        this(id, false);
    }

    public BaseTask(@NonNull String id, boolean async) {
        if (TextUtils.isEmpty(id)) {
            throw new IllegalArgumentException("task's mId can't be empty");
        }
        this.mId = id;
        this.isAsyncTask = async;
        this.mPriority = DEFAULT_PRIORITY;
        this.mState = TaskState.IDLE;
    }

    /**
     * 调用start启动当前task
     */
    protected synchronized void start() {
        if (mState != TaskState.IDLE) {
            throw new RuntimeException("can no run task " + getId() + " again!");
        }
        toStart();
        setExecuteTime(System.currentTimeMillis());
        AnchorsRuntime.executeTask(this);
    }

    @Override
    public void run() {
        if (AnchorsRuntime.debuggable()
            /* && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2*/) {
            /*
             * Trace.beginSection 与 Trace.endSection 之间的内容会一直被追踪
             * 参考：https://www.cnblogs.com/baiqiantao/p/7700511.html
             */
            Trace.beginSection(mId);
        }
        toRunning();
        run(mId);
        toFinish();
        notifyBehindTasks();
        release();
        if (AnchorsRuntime.debuggable()
            /* && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2*/) {
            Trace.endSection();
        }
    }

    protected abstract void run(String name);

    /**
     * 状态转换：start
     */
    private void toStart() {
        setState(TaskState.START);
        AnchorsRuntime.setStateInfo(this);

        if (AnchorsRuntime.debuggable()) {
            logTaskListener.onStart(this);
        }
        for (TaskListener listener : taskListeners) {
            listener.onStart(this);
        }
    }

    /**
     * 状态转换：running
     */
    private void toRunning() {
        setState(TaskState.RUNNING);
        AnchorsRuntime.setStateInfo(this);
        AnchorsRuntime.setThreadName(this, Thread.currentThread().getName());

        if (AnchorsRuntime.debuggable()) {
            logTaskListener.onRunning(this);
        }
        for (TaskListener listener : taskListeners) {
            listener.onRunning(this);
        }
    }

    /**
     * 状态转换：finish
     */
    private void toFinish() {
        setState(TaskState.FINISHED);
        AnchorsRuntime.setStateInfo(this);
        AnchorsRuntime.removeAnchorTask(mId);

        if (AnchorsRuntime.debuggable()) {
            logTaskListener.onFinish(this);
        }
        for (TaskListener listener : taskListeners) {
            listener.onFinish(this);
        }
    }

    /**
     * 后置触发, 和 {@link BaseTask#dependOn(BaseTask)} 方向相反，都可以设置依赖关系
     */
    protected void behindBy(@NonNull BaseTask task) {
        if (task != this) {
            if (task instanceof Project) {
                task = ((Project) task).getStartTask();
            }
            behindTasks.add(task);
            task.dependOn(this);
        }
    }

    protected void removeBehind(@NonNull BaseTask task) {
        if (task != this) {
            if (task instanceof Project) {
                task = ((Project) task).getStartTask();
            }
            behindTasks.remove(task);
            task.removeDependence(this);
        }
    }

    /**
     * 前置条件, 和 {@link BaseTask#behindBy(BaseTask)} 方向相反，都可以设置依赖关系
     * 1、dependTasks（依赖tasks）加入被依赖的task
     * 2、被依赖的task的behindTasks（后置tasks）加入当前task
     */
    public void dependOn(@NonNull BaseTask task) {
        if (task != this) {
            if (task instanceof Project) {
                task = ((Project) task).getEndTask();
            }
            dependTasks.add(task);
            //防止交叉依赖
            if (!task.behindTasks.contains(this)) {
                task.behindTasks.add(this);
            }
        }
    }

    /**
     * 移除依赖的时候会
     * 1、将当前task的dependTasks（依赖tasks）中的依赖移除
     * 2、将当前task从被移除的task的behindTasks（后置tasks）中移除
     */
    protected void removeDependence(@NonNull BaseTask task) {
        if (task != this) {
            if (task instanceof Project) {
                task = ((Project) task).getEndTask();
            }
            dependTasks.remove(task);
            task.behindTasks.remove(this);
        }
    }


    /**
     * 通知后置者自己已经完成了
     */
    private void notifyBehindTasks() {
        if (this instanceof LockableTask) {
            if (!((LockableTask) this).successToUnlock()) {
                return;
            }
        }

        if (!behindTasks.isEmpty()) {
            if (behindTasks.size() > 1) {
                // 排序（锚点task会被前置）
                Collections.sort(behindTasks, AnchorsRuntime.getTaskComparator());
            }

            //遍历记下来的任务，通知它们说存在的前置已经完成
            for (BaseTask task : behindTasks) {
                task.dependTaskFinish(this);
            }
        }
    }


    /**
     * 依赖的任务已经完成
     * 比如 B -> A (B 依赖 A), A 完成之后调用该方法通知 B "A依赖已经完成了"
     * 当且仅当 B 的所有依赖都已经完成了, B 开始执行
     */
    private synchronized void dependTaskFinish(BaseTask dependTask) {
        // 如果本身没有依赖就不用在这里执行
        if (dependTasks.isEmpty()) {
            return;
        }
        dependTasks.remove(dependTask);

        //所有前置任务都已经完成了
        if (dependTasks.isEmpty()) {
            start();
        }
    }

    void release() {
        setState(TaskState.RELEASE);
        AnchorsRuntime.setStateInfo(this);
        AnchorsRuntime.getTaskRuntimeInfo(mId).clearTask();
        dependTasks.clear();
        behindTasks.clear();
        if (AnchorsRuntime.debuggable()) {
            logTaskListener.onRelease(this);
            logTaskListener = null;
        }
        for (TaskListener listener : taskListeners) {
            listener.onRelease(this);
        }
        taskListeners.clear();
    }

    public void cleanBehind(){
        behindTasks.clear();
    }


    public String getId() {
        return mId;
    }

    public long getExecuteTime() {
        return mExecuteTime;
    }

    protected void setExecuteTime(long mExecuteTime) {
        this.mExecuteTime = mExecuteTime;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
    }

    public int getPriority() {
        return mPriority;
    }

    public boolean isAsyncTask() {
        return isAsyncTask;
    }

    public int getState() {
        return mState;
    }

    protected void setState(@TaskState int state) {
        this.mState = state;
    }

    public void addTaskListener(TaskListener taskListener) {
        if (taskListener != null && !taskListeners.contains(taskListener)) {
            taskListeners.add(taskListener);
        }
    }

    public Set<BaseTask> getDependTasks() {
        return dependTasks;
    }

    public List<BaseTask> getBehindTasks() {
        return behindTasks;
    }

    public Set<String> getDependTaskName() {
        Set<String> result = new HashSet<>();
        for (BaseTask task : dependTasks) {
            result.add(task.mId);
        }
        return result;
    }

    public void removeDepend(BaseTask originTask) {
        dependTasks.remove(originTask);
    }

    public void updateBehind(BaseTask updateTask, BaseTask originTask) {
        behindTasks.remove(originTask);
        behindTasks.add(updateTask);
    }

    @Override
    public int compareTo(@NonNull BaseTask o) {
        return Utils.compareTask(this, o);
    }

    /**
     * 使用Set需要重写hashCode()与equals()方法
     * 参考：https://blog.csdn.net/xiaohuo0930/article/details/91177978
     * 参考：https://www.cnblogs.com/zhaosq/p/10063610.html
     * Set中加入对象的时候
     * 1、判断Set中对象与加入对象的hashCode是否相同
     * 2、如果相同再调用equals判断是否相同
     * 如果不同才会加入
     */
    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseTask) {
            return mId.equals(((BaseTask) obj).mId);
        }
        return false;
    }
}
