package com.demo.appinit.anchors;

import androidx.annotation.NonNull;

public interface TaskCreator {

    @NonNull
    BaseTask createTask(String taskName);

}
