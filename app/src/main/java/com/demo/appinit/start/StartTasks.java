package com.demo.appinit.start;

/**
 * 所有 app 启动 Task 的常量名管理
 */
public class StartTasks {

    private static final String PREFIX = "START_";

    /**
     * 模拟5个启动任务
     */
    public static final String START_FIRST_OF_ALL = PREFIX + "FIRST_OF_ALL";
    public static final String START_CONFIG_PRELOAD = PREFIX + "CONFIG_PRELOAD";
    public static final String START_TASK_1 = PREFIX + "TASK_1";
    public static final String START_TASK_2 = PREFIX + "TASK_2";
    public static final String START_TASK_3 = PREFIX + "TASK_3";

    /**
     * 等待动态授权的 permission start task
     */
    public static final String START_AWAIT_PERMISSION = PREFIX + "START_AWAIT_PERMISSION";

    /**
     * 模拟5个启动任务
     */
    public static final String START_SAVE_INFO_TO_STORAGE = PREFIX + "SAVE_INFO_TO_STORAGE";
    public static final String START_TASK_4 = PREFIX + "TASK_4";
    public static final String START_TASK_5 = PREFIX + "TASK_5";
    public static final String START_TASK_6 = PREFIX + "TASK_6";
    public static final String START_NET_REQUEST = PREFIX + "NET_REQUEST";

}
