package com.demo.appinit.start;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 尉迟涛
 * create time : 2020/4/11 16:34
 */
public class PermissionManager {


    public static final int PERMISSION_REQUEST_CODE = 0x9001;
    private static List<String> notGrantedList;

    /**
     * 检查权限
     */
    public static boolean checkAndRecord(Context context, String permission) {
        boolean isGranted = checkSelfPermission(context, permission);
        if (!isGranted) {
            if (notGrantedList == null) {
                notGrantedList = new ArrayList<>(3);
            }
            notGrantedList.add(permission);
        }
        return isGranted;
    }

    public static List<String> getNotGrantedList() {
        return notGrantedList;
    }

    /**
     * 检查权限
     */
    public static boolean checkSelfPermission(Context context, String permission) {
        boolean isGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isGranted = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return isGranted;
    }

    /**
     * 请求权限
     */
    public static void requestPermission(Activity activity, String[] permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, permission, requestCode);
    }

//    private static final int DEFAULT_REQUEST_CODE = 0;
//    /**
//     * 存储
//     */
//    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 0x9002;
//    /**
//     * 定位
//     */
//    public static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 0x9006;
//    /**
//     * 相机
//     */
//    public static final int REQUEST_CODE_CAMERA = 0x9007;
//
//    /**
//     * 照相
//     */
//    public static void requestPermissionCamera(Activity activity) {
//        requestPermissionCamera(activity, DEFAULT_REQUEST_CODE);
//    }
//
//    /**
//     * 照相
//     */
//    public static void requestPermissionCamera(Activity activity, int requestCode) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (requestCode == DEFAULT_REQUEST_CODE) {
//                requestPermission(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
//            } else {
//                requestPermission(activity, new String[]{Manifest.permission.CAMERA}, requestCode);
//            }
//        }
//    }
//
//    /**
//     * 定位
//     */
//    public static void requestPermissionLocation(Activity activity) {
//        requestPermissionLocation(activity, DEFAULT_REQUEST_CODE);
//    }
//
//    /**
//     * 定位
//     */
//    public static void requestPermissionLocation(Activity activity, int requestCode) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (requestCode == DEFAULT_REQUEST_CODE) {
//                requestPermission(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ACCESS_COARSE_LOCATION);
//            } else {
//                requestPermission(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
//            }
//        }
//    }
//
//    /**
//     * 存储
//     */
//    public static void requestPermissionStorage(Activity activity) {
//        requestPermissionStorage(activity, DEFAULT_REQUEST_CODE);
//    }
//
//    /**
//     * 存储 兼容8.0同时申请读取和存储权限
//     */
//    public static void requestPermissionStorage(Activity activity, int requestCode) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (requestCode == DEFAULT_REQUEST_CODE) {
//                requestPermission(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
//            } else {
//                requestPermission(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
//            }
//        }
//    }


}
