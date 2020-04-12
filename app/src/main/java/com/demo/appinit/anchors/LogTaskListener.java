package com.demo.appinit.anchors;

import android.util.SparseArray;

import androidx.annotation.NonNull;

public class LogTaskListener implements TaskListener {

    @Override
    public void onStart(BaseTask task) {
        Logger.d(task.getId() + Constants.START_METHOD);
    }

    @Override
    public void onRunning(BaseTask task) {
        Logger.d(task.getId() + Constants.RUNNING_METHOD);
    }

    @Override
    public void onFinish(BaseTask task) {
        Logger.d(task.getId() + Constants.FINISH_METHOD);
        logTaskRuntimeInfoString(task);
    }

    @Override
    public void onRelease(BaseTask task) {
        Logger.d(task.getId() + Constants.RELEASE_METHOD);
    }

    private static void logTaskRuntimeInfoString(BaseTask task) {
        TaskRuntimeInfo taskRuntimeInfo = AnchorsRuntime.getTaskRuntimeInfo(task.getId());

        SparseArray<Long> map = taskRuntimeInfo.getStateTime();
        Long startTime = map.get(TaskState.START);
        Long runningTime = map.get(TaskState.RUNNING);
        Long finishedTime = map.get(TaskState.FINISHED);

        StringBuilder builder = new StringBuilder();
        builder.append(Constants.WRAPPED);
        builder.append(Constants.TASK_DETAIL_INFO_TAG);
        builder.append(Constants.WRAPPED);

        buildTaskInfoEdge(builder, taskRuntimeInfo);

        addTaskInfoLineString(builder, Constants.DEPENDENCIES, getDependenceInfo(taskRuntimeInfo), false);
        addTaskInfoLineString(builder, Constants.IS_ANCHOR, String.valueOf(taskRuntimeInfo.isAnchor()), false);
        addTaskInfoLineString(builder, Constants.THREAD_INFO, taskRuntimeInfo.getThreadName(), false);
        addTaskInfoLineString(builder, Constants.START_TIME, String.valueOf(startTime), true);
        addTaskInfoLineString(builder, Constants.START_UNTIL_RUNNING, String.valueOf(runningTime - startTime), true);
        addTaskInfoLineString(builder, Constants.RUNNING_CONSUME, String.valueOf(finishedTime - runningTime), true);
        addTaskInfoLineString(builder, Constants.FINISH_TIME, String.valueOf(finishedTime), false);

        buildTaskInfoEdge(builder, null);

        builder.append(Constants.WRAPPED);
        Logger.d(Constants.TASK_DETAIL_INFO_TAG, builder.toString());
        if (taskRuntimeInfo.isAnchor()) {
            Logger.d(Constants.ANCHORS_INFO_TAG, builder.toString());
        }
    }

    private static void addTaskInfoLineString(StringBuilder stringBuilder, String key, String time, boolean addUnit) {
        if (stringBuilder == null) {
            return;
        }
        stringBuilder.append(Constants.WRAPPED);
        stringBuilder.append(String.format(Constants.LINE_STRING_FORMAT, key, time));
        if (addUnit) {
            stringBuilder.append(Constants.MS_UNIT);
        }
    }

    private static void buildTaskInfoEdge(StringBuilder stringBuilder, TaskRuntimeInfo taskRuntimeInfo) {
        if (stringBuilder == null) {
            return;
        }
        stringBuilder.append(Constants.WRAPPED);
        stringBuilder.append(Constants.HALF_LINE_STRING);
        if (taskRuntimeInfo != null) {
            stringBuilder.append(taskRuntimeInfo.isProject() ? " project (" : " task (" + taskRuntimeInfo.getTaskId() + " ) ");
        }
        stringBuilder.append(Constants.HALF_LINE_STRING);
    }


    private static String getDependenceInfo(@NonNull TaskRuntimeInfo taskRuntimeInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : taskRuntimeInfo.getDependencies()) {
            stringBuilder.append(s).append(" ");
        }
        return stringBuilder.toString();
    }
}
