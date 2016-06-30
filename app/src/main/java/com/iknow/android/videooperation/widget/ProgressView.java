package com.iknow.android.videooperation.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Author：J.Chou
 * Date：  2016.06.30 09:33.
 * Email： who_know_me@163.com
 * Describe:
 */
public class ProgressView extends View {
    /** 进度条 */
    private Paint mProgressPaint;
    /** 回删 */
    private Paint mRemovePaint;
    /** 最长时长 */
    private int mMax;
    /** 进度*/
    private int mProgress;
    private boolean isRemove;
    public ProgressView(Context Context, AttributeSet Attr) {
        super(Context, Attr);
        init();
    }
    private void init() {
        mProgressPaint = new Paint();
        mRemovePaint = new Paint();
        setBackgroundColor(Color.TRANSPARENT);
        mProgressPaint.setColor(Color.GREEN);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mRemovePaint.setColor(Color.RED);
        mRemovePaint.setStyle(Paint.Style.FILL);;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        final int width = getMeasuredWidth(), height = getMeasuredHeight();
        int progressLength = (int) ((mProgress / (mMax * 1.0f)) * (width / 2));
        canvas.drawRect(progressLength, 0, width - progressLength, height, isRemove ? mRemovePaint : mProgressPaint);
        canvas.restore();
    }
    public void setMax(int max){
        this.mMax = max;
    }
    public void setProgress(int progress){
        this.mProgress = progress;
        postInvalidate();//刷新调用onDraw方法
    }
    public void setRemove(boolean isRemove){
        this.isRemove = isRemove;
    }
}
