package com.demo.appinit.activity;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.rxbus.RxBus;
import com.demo.appinit.R;
import com.demo.appinit.RxBusTags;
import com.demo.appinit.start.PermissionManager;
import com.demo.appinit.view.DialogChoiceOne;

import java.util.List;

/**
 * @author 尉迟涛
 * create time : 2020/4/12 16:36
 */
public class SplashActivity extends AppCompatActivity {

    private DialogChoiceOne dialogChoiceOne;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        requestPermission();
    }

    private void requestPermission() {
        List<String> notGrantedList = PermissionManager.getNotGrantedList();
        if (notGrantedList != null && notGrantedList.size() > 0) {
            String[] permissions = new String[notGrantedList.size()];
            PermissionManager.requestPermission(this, notGrantedList.toArray(permissions),
                    PermissionManager.PERMISSION_REQUEST_CODE);
        } else {
            MainActivity.start(SplashActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.PERMISSION_REQUEST_CODE) {
            boolean readStoragePermission = PermissionManager.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            boolean phoneStatePermission = PermissionManager.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

            boolean isAllGranted = readStoragePermission && phoneStatePermission;
            if (isAllGranted) {
                continueStartProcess(true);
                MainActivity.start(SplashActivity.this);
            } else {
                StringBuilder sb = new StringBuilder("权限");
                if (!readStoragePermission) {
                    sb.append("【读取外部存储】");
                }
                if (!phoneStatePermission) {
                    sb.append("【读取手机状态】");
                }
                sb.append("未获取");

                if (dialogChoiceOne == null) {
                    dialogChoiceOne = new DialogChoiceOne(this) {
                        @Override
                        public void click() {
                            dialogChoiceOne.dismiss();
                            continueStartProcess(false);
                            MainActivity.start(SplashActivity.this);
                        }
                    };
                }
                dialogChoiceOne.setInfo(sb.toString());
                dialogChoiceOne.show();
            }
        }
    }

    private void continueStartProcess(boolean isContinue) {
        //解除AwaitPermStartTask等待
        RxBus.getDefault().post(isContinue, RxBusTags.SDK_PERM_GRANTED_CONTINUE);
    }
}
