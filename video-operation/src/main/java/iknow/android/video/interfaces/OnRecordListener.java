package iknow.android.video.interfaces;

import java.io.File;

/**
 * Author：J.Chou
 * Date：  2016.06.30 15:57.
 * Email： who_know_me@163.com
 * Describe:
 */
public interface OnRecordListener{
    void startRecord(onRecordFinishListener onRecordFinishListener);
    File stopRecord();
}
