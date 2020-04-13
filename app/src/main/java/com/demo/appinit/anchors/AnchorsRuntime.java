package com.demo.appinit.anchors;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Anchors 框架 runtime 信息管理
 * 包含：
 * 1、application 锚点管理
 * 2、application UIThreadTask 运行管理
 * 3、调试配置
 * 4、线程池配置
 * 5、运行时 Task 信息收集
 * @author ？
 */
class AnchorsRuntime {

    /**
     * 线程池
     */
    private static final InnerThreadPool S_POOL = new InnerThreadPool();
    /**
     * 所有 task 运行时信息，用于log
     */
    private static final Map<String, TaskRuntimeInfo> S_TASK_RUNTIME_INFO = new HashMap<>();

    /**
     * Task 比较逻辑
     */
    private final static Comparator<BaseTask> S_TASK_COMPARATOR = new Comparator<BaseTask>() {
        @Override
        public int compare(BaseTask lhs, BaseTask rhs) {
            return Utils.compareTask(lhs, rhs);
        }
    };

    /**
     * 设置锚点任务，当且仅当所有锚点任务都完成时, application 不在阻塞 UIThread
     */
    private static volatile Set<String> sAnchorTaskIds = new HashSet<>();
    /**
     * 如果存在锚点任务，则同步的任务在所有锚点任务都完成前，在 UIThread 上运行
     * ps: 后续解除锚点之后，所有UI线程上的 Task 都通过 handle 发送执行，不保证业务逻辑的同步。
     */
    private static volatile List<BaseTask> sRunBlockApplication = new ArrayList<>();


    /**
     * 调试信息
     */
    private static boolean sDebuggable = false;
    private static Handler sHandler = new Handler(Looper.getMainLooper());
    private static Set<BaseTask> sTraversalVisitor = new HashSet<>();

    /**
     * 同步使用handler发送至主线程，异步使用线程池
     * @param task 任务
     */
    static void executeTask(BaseTask task) {
        if (task.isAsyncTask()) {
            S_POOL.executeTask(task);
        } else {
            if (AnchorsRuntime.hasAnchorTasks()) {
                AnchorsRuntime.addRunTasks(task);
            } else {
                sHandler.post(task);
            }
        }
    }

    static void clear() {
        sDebuggable = false;
        sAnchorTaskIds.clear();
        sRunBlockApplication.clear();
        S_TASK_RUNTIME_INFO.clear();
        sTraversalVisitor.clear();
    }

    public static boolean debuggable() {
        return sDebuggable;
    }

    static void openDebug(boolean debug) {
        AnchorsRuntime.sDebuggable = debug;
    }

    static Comparator<BaseTask> getTaskComparator() {
        return S_TASK_COMPARATOR;
    }

    static Handler getHandler() {
        return sHandler;
    }


    static void addAnchorTasks(Set<String> ids) {
        if (ids != null && ids.size() > 0) {
            sAnchorTaskIds.addAll(ids);
        }
    }

    static void removeAnchorTask(String id) {
        if (!TextUtils.isEmpty(id)) {
            sAnchorTaskIds.remove(id);
        }
    }

    static boolean hasAnchorTasks() {
        return !sAnchorTaskIds.isEmpty();
    }

    static Set<String> getAnchorTasks() {
        return sAnchorTaskIds;
    }

    private static void addRunTasks(BaseTask task) {
        if (task != null && !sRunBlockApplication.contains(task)) {
            sRunBlockApplication.add(task);
        }
    }

    static void tryRunBlockRunnable() {
        if (!sRunBlockApplication.isEmpty()) {
            if (sRunBlockApplication.size() > 1) {
                Collections.sort(sRunBlockApplication, AnchorsRuntime.getTaskComparator());
            }
            Runnable runnable = sRunBlockApplication.remove(0);
            if (hasAnchorTasks()) {
                runnable.run();
            } else {
                sHandler.post(runnable);
                for (Runnable blockItem : sRunBlockApplication) {
                    sHandler.post(blockItem);
                }
                sRunBlockApplication.clear();
            }
        }
    }

    static boolean hasRunTasks() {
        return !sRunBlockApplication.isEmpty();
    }

    private static boolean hasTaskRuntimeInfo(String taskId) {
        return S_TASK_RUNTIME_INFO.get(taskId) != null;
    }

    @NonNull
    static TaskRuntimeInfo getTaskRuntimeInfo(@NonNull String taskId) {
        return S_TASK_RUNTIME_INFO.get(taskId);
    }

    static void setThreadName(@NonNull BaseTask task, String threadName) {
        TaskRuntimeInfo taskRuntimeInfo = S_TASK_RUNTIME_INFO.get(task.getId());
        if (taskRuntimeInfo != null) {
            taskRuntimeInfo.setThreadName(threadName);
        }
    }

    static void setStateInfo(@NonNull BaseTask task) {
        TaskRuntimeInfo taskRuntimeInfo = S_TASK_RUNTIME_INFO.get(task.getId());
        if (taskRuntimeInfo != null) {
            taskRuntimeInfo.setStateTime(task.getState(), System.currentTimeMillis());
        }
    }


    /**
     * 遍历依赖树并完成启动前的初始化
     * <p>
     * 1.获取依赖树最大深度
     * 2.遍历初始化运行时数据并打印log
     * 3.如果锚点不存在，则移除
     * 4.提升锚点链的优先级
     */
    static void traversalDependenciesAndInit(@NonNull BaseTask task) {
        //获取依赖树最大深度
        int maxDepth = getDependenciesMaxDepth(task, sTraversalVisitor);
        sTraversalVisitor.clear();
        BaseTask[] pathTasks = new BaseTask[maxDepth];
        //遍历初始化运行时数据并打印log
        traversalDependenciesPath(task, pathTasks, 0);

        //如果锚点不存在，则移除。存在则提升锚点链的优先级
        Iterator<String> iterator = sAnchorTaskIds.iterator();
        while (iterator.hasNext()) {
            String taskId = iterator.next();
            if (!hasTaskRuntimeInfo(taskId)) {
                Logger.w(Constants.ANCHORS_INFO_TAG, "anchor \"" + taskId + "\" no found !");
                iterator.remove();
            } else {
                TaskRuntimeInfo info = getTaskRuntimeInfo(taskId);
                traversalMaxTaskPriority(info.getTask());
            }
        }
    }

    /**
     * 递归向上设置优先级
     */
    private static void traversalMaxTaskPriority(BaseTask task) {
        if (task == null) {
            return;
        }
        task.setPriority(Integer.MAX_VALUE);
        for (BaseTask dependence : task.getDependTasks()) {
            traversalMaxTaskPriority(dependence);
        }
    }

    /**
     * 遍历依赖树
     * 1. 初始化 sTaskRuntimeInfo
     * 2. 判断锚点是否存在依赖树中
     */
    private static void traversalDependenciesPath(@NonNull BaseTask task, BaseTask[] pathTasks, int pathLen) {
        pathTasks[pathLen++] = task;
        //依赖路径到尽头了
        if (task.getBehindTasks().isEmpty()) {

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < pathLen; i++) {
                BaseTask pathItem = pathTasks[i];
                if (pathItem != null) {
                    if (hasTaskRuntimeInfo(pathItem.getId())) {
                        TaskRuntimeInfo taskRuntimeInfo = getTaskRuntimeInfo(pathItem.getId());
                        //不允许框架层存在两个相同id的task
                        if (!taskRuntimeInfo.isTaskInfo(pathItem)) {
                            throw new RuntimeException("Multiple different tasks are not allowed to contain the same id (" + pathItem.getId() + ")!");
                        }
                    } else {
                        //如果没有初始化则初始化runtimeInfo
                        TaskRuntimeInfo taskRuntimeInfo = new TaskRuntimeInfo(pathItem);
                        if (sAnchorTaskIds.contains(pathItem.getId())) {
                            taskRuntimeInfo.setAnchor(true);
                        }
                        S_TASK_RUNTIME_INFO.put(pathItem.getId(), taskRuntimeInfo);
                    }
                    if (sDebuggable) {
                        stringBuilder.append(i == 0 ? "" : " --> ").append(pathItem.getId());
                    }
                }
            }
            if (sDebuggable) {
                Logger.d(Constants.DEPENDENCE_TAG, stringBuilder.toString());
            }
        } else {
            for (BaseTask behindTask : task.getBehindTasks()) {
                traversalDependenciesPath(behindTask, pathTasks, pathLen);
            }
        }
    }

    /**
     * 获取依赖树的最大深度
     */
    private static int getDependenciesMaxDepth(@NonNull BaseTask task, Set<BaseTask> sTraversalVisitor) {
        //判断依赖路径是否存在异常，不允许存在回环的依赖
        int maxDepth = 0;
        if (!sTraversalVisitor.contains(task)) {
            sTraversalVisitor.add(task);
        } else {
            throw new RuntimeException("Do not allow dependency graphs to have a loopback！Related task'id is " + task.getId() + "!");
        }
        for (BaseTask behindTask : task.getBehindTasks()) {
            Set<BaseTask> newTasks = new HashSet<>(sTraversalVisitor);
            int depth = getDependenciesMaxDepth(behindTask, newTasks);
            if (depth >= maxDepth) {
                maxDepth = depth;
            }
        }
        maxDepth++;
        return maxDepth;
    }

    static class InnerThreadPool {

        private ExecutorService asyncThreadExecutor;

        InnerThreadPool() {
            final int cpuCount = Runtime.getRuntime().availableProcessors();
            /*
             * {@link android.os.AsyncTask}
             * 相对比 Anchors, AsyncTask 更强调的是在业务处理中，异步业务不应该使得cpu饱和，
             * 但是App启动场景中，时间比较短，可以尽可能使用更多的cpu资源。
             * 但是anchors支持锚点阻塞ui线程，后续可能还会有延迟的异步初始化任务，所以也不要完全饱和。
             */
            final int corPoolSize = Math.max(4, Math.min(cpuCount - 1, 8));
            final int maxPoolSize = cpuCount * 2 + 1;
            final int keepAliveSeconds = 30;

            BlockingQueue<Runnable> sPoolWorkQueue = new PriorityBlockingQueue<>(128);
            // 记录个数+定义线程名
            ThreadFactory sThreadFactory = new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r, "Anchors Thread #" + mCount.getAndIncrement());
                }
            };

            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    corPoolSize, maxPoolSize,
                    keepAliveSeconds, TimeUnit.SECONDS,
                    sPoolWorkQueue, sThreadFactory
            );
            threadPoolExecutor.allowCoreThreadTimeOut(true);
            asyncThreadExecutor = threadPoolExecutor;
        }

        void executeTask(Runnable runnable) {
            asyncThreadExecutor.execute(runnable);
        }
    }
}
