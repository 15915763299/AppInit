package com.demo.appinit.anchors;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Project extends BaseTask {

    private BaseTask endTask;
    private BaseTask startTask;

    private Project(@NonNull String id) {
        super(id);
    }

    @NonNull
    public BaseTask getStartTask() {
        return startTask;
    }

    @NonNull
    public BaseTask getEndTask() {
        return endTask;
    }

    @Override
    protected void behindBy(@NonNull BaseTask task) {
        endTask.behindBy(task);
    }

    @Override
    public void dependOn(@NonNull BaseTask task) {
        startTask.dependOn(task);
    }

    @Override
    protected void removeBehind(@NonNull BaseTask task) {
        endTask.removeBehind(task);
    }

    @Override
    protected void removeDependence(@NonNull BaseTask task) {
        startTask.removeDependence(task);
    }

    @Override
    protected synchronized void start() {
        startTask.start();
    }


    @Override
    public void run(String name) {
        //不需要处理
    }

    @Override
    void release() {
        super.release();
        endTask = null;
        startTask = null;
    }

    /**
     * project将加入的task整合为一一依赖，避免了环的出现，或者交叉依赖
     * 当出现 project 内 task 循环依赖时，循环依赖会自动断开。
     */
    public static class Builder {

        private BaseTask mCurrentAddTask;
        private BaseTask mFinishTask;
        private BaseTask mStartTask;
        private boolean mCurrentTaskShouldDependOnStartTask;
        private Project mProject;
        private TaskFactory mFactory;
        /**
         * 默认project优先级为project内所有task的优先级，如果没有设置则取 max(project内所有task的)
         */
        private int mPriority;

        public Builder(@NonNull String projectName, @NonNull TaskFactory taskFactory) {
            this.mCurrentAddTask = null;
            this.mCurrentTaskShouldDependOnStartTask = false;
            this.mProject = new Project(projectName);
            long criticalTime = System.currentTimeMillis();
            this.mStartTask = new CriticalTask(projectName + "_start(" + criticalTime + ")");
            this.mFinishTask = new CriticalTask(projectName + "_end(" + criticalTime + ")");
            this.mFactory = taskFactory;
        }

        /**
         * 添加task
         */
        public Builder add(String taskName) {
            BaseTask task = mFactory.getTask(taskName);
            if (task.getPriority() > mPriority) {
                mPriority = task.getPriority();
            }
            return add(task);
            // return add(mFactory.getTask(taskName));
        }

        public Builder add(BaseTask task) {
            if (mCurrentTaskShouldDependOnStartTask && mCurrentAddTask != null) {
                //作为mStartTask的后置task
                mStartTask.behindBy(mCurrentAddTask);
            }
            mCurrentAddTask = task;
            mCurrentTaskShouldDependOnStartTask = true;
            mCurrentAddTask.behindBy(mFinishTask);
            return this;
        }

        public Builder dependOn(String taskName) {
            return dependOn(mFactory.getTask(taskName));
        }

        /**
         * project中的依赖
         * 1、将当前task放到被依赖的task的后面
         * 2、mFinishTask移除依赖
         *
         */
        public Builder dependOn(BaseTask task) {
            task.behindBy(mCurrentAddTask);
            mFinishTask.removeDependence(task);
            mCurrentTaskShouldDependOnStartTask = false;
            return this;
        }

        public Builder dependOn(String... names) {
            if (names != null && names.length > 0) {
                for (String name : names) {
                    BaseTask task = mFactory.getTask(name);
                    task.behindBy(mCurrentAddTask);
                    mFinishTask.removeDependence(task);
                }
                mCurrentTaskShouldDependOnStartTask = false;
            }
            return Builder.this;
        }

        public Project build() {
            if (mCurrentAddTask != null) {
                if (mCurrentTaskShouldDependOnStartTask) {
                    mStartTask.behindBy(mCurrentAddTask);
                }
            } else {
                mStartTask.behindBy(mFinishTask);
            }
            mStartTask.setPriority(mPriority);
            mFinishTask.setPriority(mPriority);
            mProject.startTask = mStartTask;
            mProject.endTask = mFinishTask;
            return mProject;
        }
    }

    /**
     * task工厂，需要自定义TaskCreator
     */
    public static class TaskFactory {
        private Map<String, BaseTask> mCacheTask;
        private TaskCreator mTaskCreator;

        public TaskFactory(TaskCreator creator) {
            mTaskCreator = creator;
            mCacheTask = new HashMap<>();
        }

        @NonNull
        public synchronized BaseTask getTask(String taskId) {
            BaseTask task = mCacheTask.get(taskId);
            if (task != null) {
                return task;
            }
            // 创建 task
            task = mTaskCreator.createTask(taskId);
            mCacheTask.put(taskId, task);
            return task;
        }
    }

    /**
     * 作为临界节点，标识 project 的开始和结束。
     */
    private static class CriticalTask extends BaseTask {

        CriticalTask(String name) {
            super(name);
        }

        @Override
        public void run(String name) {
            Log.e("CriticalTask", name);
        }
    }
}
