package com.iknow.android.videooperation.utils;

/**
 * Author：J.Chou
 * Date：  2016.06.30 15:57.
 * Email： who_know_me@163.com
 * Describe:
 */
public interface IVideo {
    void startRecord(IRecordListener onRecordFinishListener);
    void stopRecord();
}
