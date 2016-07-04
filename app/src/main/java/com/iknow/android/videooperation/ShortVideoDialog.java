package com.iknow.android.videooperation;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.iknow.android.videooperation.utils.DeviceHelper;
import com.iknow.android.videooperation.interfaces.onRecordFinishListener;
import com.iknow.android.videooperation.interfaces.IShortVideo;
import com.iknow.android.videooperation.widget.VideoRecorderView;

/**
 * Author：J.Chou
 * Date：  2016.06.29 17:11.
 * Email： who_know_me@163.com
 * Describe:
 */
public class ShortVideoDialog{

    private static final double MIN_HOLD_TIME = 1.5;
    private VideoRecorderView mRecorderView;
    private Button mShootBtn;
    private boolean isFinish = true;
    private static Dialog mDialog;
    private Context mContext;
    private IShortVideo mCallBack;

    public ShortVideoDialog(final Context context) {
        this.mContext = context;
    }

    public ShortVideoDialog build(final IShortVideo iShortVideo){
        this.mCallBack = iShortVideo;
        mDialog = new Dialog(mContext,R.style.DialogStyle);
        View view = LayoutInflater.from(mContext).inflate(R.layout.short_video_activity,null);

        initDialog(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setContentView(view);
        mDialog.getWindow().setGravity(Gravity.BOTTOM);
        mDialog.getWindow().setWindowAnimations(R.style.dialogWindowAnim);
        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        lp.width = dm.widthPixels;
        lp.height = DeviceHelper.dip2px(450);
        return this;
    }

    public void show(){
        if(mDialog != null)
            mDialog.show();
    }

    private void initDialog(View v){
        final VideoRecorderView mRecorderView = (VideoRecorderView) v.findViewById(R.id.movieRecorderView);
        Button mShootBtn = (Button) v.findViewById(R.id.shoot_button);

        mShootBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mRecorderView.startRecord(new onRecordFinishListener() {

                        @Override
                        public void recordFinish() {
                            Looper.prepare();
                            Toast.makeText(mContext, "松开手再拍一个", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void recordError() {

                        }
                    });
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mRecorderView.getTimeCount() > MIN_HOLD_TIME) {
                        mDialog.dismiss();
                        mCallBack.getVideoFile(mRecorderView.stopRecord());
                    }
                    else {
                        Toast.makeText(mContext, "手指不要放开", Toast.LENGTH_SHORT).show();
                        mRecorderView.stopRecord();
                    }
                }
                return true;
            }
        });
    }
}
