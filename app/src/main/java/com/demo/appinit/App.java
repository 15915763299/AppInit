package com.demo.appinit;

import android.Manifest;
import android.app.Application;
import android.content.Context;

import com.demo.appinit.start.PermissionManager;
import com.demo.appinit.start.factory.MainProcessStarter;

/**
 * @author 尉迟涛
 * create time : 2020/4/11 08:52
 */
public class App extends Application {

    private static App app;

    public static App getApp() {
        return app;
    }

    @Override
    protected void attachBaseContext(Context base) {
        LaunchTimeRecoder.startRecord();
        super.attachBaseContext(base);
        app = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        boolean checkPermission = checkPermission();
        MainProcessStarter.start(checkPermission);
//        try {
//            Thread.sleep(600);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 检查必要的权限
     */
    public boolean checkPermission() {
        boolean readStoragePermission = PermissionManager.checkAndRecord(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean phoneStatePermission = PermissionManager.checkAndRecord(this, Manifest.permission.READ_PHONE_STATE);
        return readStoragePermission && phoneStatePermission;
    }
}
