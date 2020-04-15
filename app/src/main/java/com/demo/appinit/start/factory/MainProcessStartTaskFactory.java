package com.demo.appinit.start.factory;

import androidx.annotation.NonNull;

import com.demo.appinit.anchors.BaseTask;
import com.demo.appinit.anchors.Project;
import com.demo.appinit.anchors.TaskCreator;
import com.demo.appinit.start.StartTasks;
import com.demo.appinit.start.task.AwaitPermStartTask;
import com.demo.appinit.start.task.EmptyTask;
import com.demo.appinit.start.task.NetRequestTask;
import com.demo.appinit.start.task.SimulateTask;

/**
 * 主线程的启动任务构造工厂
 */
public class MainProcessStartTaskFactory extends Project.TaskFactory {
    public MainProcessStartTaskFactory() {
        super(new TaskCreator() {
            @NonNull
            @Override
            public BaseTask createTask(String taskName) {
                switch (taskName) {
                    case StartTasks.START_FIRST_OF_ALL:
                        return new SimulateTask(taskName, 120, false);
                    case StartTasks.START_CONFIG_PRELOAD:
                        return new SimulateTask(taskName, 50, false);
                    case StartTasks.START_TASK_1:
                        return new SimulateTask(taskName, 50, true);
                    case StartTasks.START_TASK_2:
                        return new SimulateTask(taskName, 50, false);
                    case StartTasks.START_TASK_3:
                        return new SimulateTask(taskName, 50, false);
                    case StartTasks.START_AWAIT_PERMISSION:
                        return new AwaitPermStartTask(taskName);
                    case StartTasks.START_SAVE_INFO_TO_STORAGE:
                        return new SimulateTask(taskName, 130, false);
                    case StartTasks.START_TASK_4:
                        return new SimulateTask(taskName, 50, false);
                    case StartTasks.START_TASK_5:
                        return new SimulateTask(taskName, 50, false);
                    case StartTasks.START_TASK_6:
                        return new SimulateTask(taskName, 50, true);
                    case StartTasks.START_NET_REQUEST:
                        return new NetRequestTask(taskName, 4000);
                    default:
                        return new EmptyTask(taskName);
                }

            }
        });
    }
}
