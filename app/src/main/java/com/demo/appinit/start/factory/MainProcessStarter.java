package com.demo.appinit.start.factory;

import com.demo.appinit.anchors.AnchorsManager;
import com.demo.appinit.anchors.BaseTask;
import com.demo.appinit.anchors.Project;
import com.demo.appinit.start.StartTasks;

/**
 * 主进程的启动 task 静态方法
 */
public class MainProcessStarter {

    public static void start(boolean isAllPermissionGranted) {
        MainProcessStartTaskFactory factory = new MainProcessStartTaskFactory();

        Project.Builder noPermissionProject = new Project.Builder("p1", factory)
                .add(StartTasks.START_FIRST_OF_ALL)
                // perLoad 在 firstOfAll 之后执行
                .add(StartTasks.START_CONFIG_PRELOAD).dependOn(StartTasks.START_FIRST_OF_ALL)
                // task1 在 preLoad 之后执行
                .add(StartTasks.START_TASK_1).dependOn(StartTasks.START_CONFIG_PRELOAD)
                .add(StartTasks.START_TASK_2).dependOn(StartTasks.START_CONFIG_PRELOAD)
                .add(StartTasks.START_TASK_3).dependOn(StartTasks.START_CONFIG_PRELOAD);
        BaseTask noPermissionTask = noPermissionProject.build();


        Project.Builder permissionProject = new Project.Builder("p2", factory)
                // 保存文件到本地
                .add(StartTasks.START_SAVE_INFO_TO_STORAGE)
                // 下面三个任务需要在保存信息之后执行
                .add(StartTasks.START_TASK_4).dependOn(StartTasks.START_SAVE_INFO_TO_STORAGE)
                .add(StartTasks.START_TASK_5).dependOn(StartTasks.START_SAVE_INFO_TO_STORAGE)
                .add(StartTasks.START_TASK_6).dependOn(StartTasks.START_SAVE_INFO_TO_STORAGE)
                // 向后台发送信息
                .add(StartTasks.START_NET_REQUEST);
        BaseTask permissionTask = permissionProject.build();

        AnchorsManager anchorsManager = AnchorsManager.getInstance().debuggable(true);
        // 没有权限的情况下，还是 permissionTask 组的依赖需要加入等待权限的确定任务，从而阻塞 sdk 初始化，等待授权
        if (!isAllPermissionGranted) {
            BaseTask awaitPermStartTask = factory.getTask(StartTasks.START_AWAIT_PERMISSION);
            permissionTask.dependOn(awaitPermStartTask);
            // 依赖permissionTask的task有两个，这里必须启动一个
            // anchorsManager.start(awaitPermStartTask);

            // anchorsManager同一时间只能运行一个（start一个）
            // sTaskRuntimeInfo是静态的，调用一次anchorsManager的start就会被清空一次，
            // 若此时第一个start没执行完成，又执行了第二个start，就会出现空指针异常

            // 只能改成一一依赖
            awaitPermStartTask.dependOn(noPermissionTask);
        } else {
            // 如果权限没有完全通过，依赖permissionTask的task有两个，上面必须启动一个
            // 否则awaitPermStartTask永远不会被启动
            permissionTask.dependOn(noPermissionTask);
        }

        anchorsManager
                .addAnchors(StartTasks.START_FIRST_OF_ALL, StartTasks.START_TASK_3)
                .start(noPermissionTask);
    }
}
