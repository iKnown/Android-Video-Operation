package com.iknow.android.videooperation;

import android.app.Application;

import iknow.android.video.compressor.FileUtils;

/**
 * Author：J.Chou
 * Date：  2016.07.12 11:38.
 * Email： who_know_me@163.com
 * Describe:
 */
public class VOApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FileUtils.createApplicationFolder();
    }
}
