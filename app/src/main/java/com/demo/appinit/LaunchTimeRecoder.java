package com.demo.appinit;

import android.util.Log;

/**
 * @author 尉迟涛
 * create time : 2020/4/13 20:52
 * description :
 */
public class LaunchTimeRecoder {

    private static long time;

    public static void startRecord() {
        time = System.currentTimeMillis();
    }

    public static void endRecord() {
        if (time > 0) {
            long cost = System.currentTimeMillis() - time;
            Log.e("LaunchTimeRecoder", "启动时间：" + cost);
            time = 0;
        }
    }
}
