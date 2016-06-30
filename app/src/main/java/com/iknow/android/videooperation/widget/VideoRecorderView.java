package com.iknow.android.videooperation.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.iknow.android.videooperation.R;
import com.iknow.android.videooperation.utils.IRecordListener;
import com.iknow.android.videooperation.utils.IVideo;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author：J.Chou
 * Date：  2016.06.29 16:43.
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoRecorderView extends LinearLayout implements IVideo, OnErrorListener{

    private static final int SHOOT_TIME = 10;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ProgressView mProgressBar;

    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private Timer mTimer;// 计时器

    private int mWidth;// 视频分辨率宽度
    private int mHeight;// 视频分辨率高度
    private boolean isOpenCamera;// 是否一开始就打开摄像头
    private int mRecordMaxTime;// 一次拍摄最长时间
    private int mTimeCount;// 时间计数
    private File mVecordFile = null;// 文件

    public VideoRecorderView(Context context) {
        this(context, null);
    }

    public VideoRecorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VideoRecorderView, defStyle, 0);

        isOpenCamera = a.getBoolean(R.styleable.VideoRecorderView_is_open_camera, true);// 默认打开
        mRecordMaxTime = a.getInteger(R.styleable.VideoRecorderView_record_max_time, SHOOT_TIME);

        LayoutInflater.from(context).inflate(R.layout.video_recorder_view_layout, this);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceView.setZOrderOnTop(true);

        mProgressBar = (ProgressView) findViewById(R.id.progressBar);
        mProgressBar.setMax(mRecordMaxTime);// 设置进度条最大量

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new CustomCallBack());
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        a.recycle();
    }

    private void _initCamera() throws IOException {
        if (mCamera != null) {
            _freeCameraResource();
        }
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            _freeCameraResource();
        }
        if (mCamera == null)
            return;

        Parameters params = mCamera.getParameters();
        params.set("orientation", "portrait");
        params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setParameters(params);

        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewDisplay(mSurfaceHolder);
        mCamera.startPreview();
        mCamera.unlock();
    }

    private void initRecord() throws IOException {

        try {

            CamcorderProfile mProfile = CamcorderProfile.get( CamcorderProfile.QUALITY_HIGH );

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.reset();
            if (mCamera != null)
                mMediaRecorder.setCamera(mCamera);

            mMediaRecorder.setOnErrorListener(this);

            mMediaRecorder.setVideoSource(VideoSource.CAMERA);
            mMediaRecorder.setAudioSource(AudioSource.MIC);

            mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);
            //设置音频编码方式
            mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);
            //设置视频编码方式
            mMediaRecorder.setVideoEncoder(VideoEncoder.MPEG_4_SP);

            mMediaRecorder.setOrientationHint(90);

            //设置清晰度  重要
            mMediaRecorder.setVideoSize( mProfile.videoFrameWidth, mProfile.videoFrameHeight );
            mMediaRecorder.setVideoFrameRate( mProfile.videoFrameRate );
            mMediaRecorder.setVideoEncodingBitRate( mProfile.videoBitRate );
            mMediaRecorder.setAudioEncodingBitRate( mProfile.audioBitRate );
            mMediaRecorder.setAudioChannels( mProfile.audioChannels );
            mMediaRecorder.setAudioSamplingRate( mProfile.audioSampleRate );

            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());
            mMediaRecorder.prepare();
            mMediaRecorder.start();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startRecord(final IRecordListener onRecordFinishListener) {
        createRecordDir();
        try {
            if (!isOpenCamera)
                _initCamera();
            initRecord();
            mTimeCount = 0;
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    mTimeCount++;
                    mProgressBar.setProgress(mTimeCount);
                    if (mTimeCount == mRecordMaxTime) {
                        stopRecord();
                        if (onRecordFinishListener != null)
                            onRecordFinishListener.onRecordFinish();
                    }
                }
            }, 0, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopRecord() {
        _stopRecord();
        _releaseRecord();
        _freeCameraResource();
    }

    public void _stopRecord() {
        mProgressBar.setProgress(0);
        if (mTimer != null)
            mTimer.cancel();
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void _freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    private void createRecordDir() {
        File sampleDir = new File(Environment.getExternalStorageDirectory() + File.separator + "shortVideo/video/");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        File vecordDir = sampleDir;
        try {
            mVecordFile = File.createTempFile("recording", ".mp4", vecordDir);//mp4格式
        } catch (IOException e) {
        }
    }

    private void _releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
    }

    private class CustomCallBack implements Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!isOpenCamera)
                return;
            try {
                _initCamera();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (!isOpenCamera)
                return;
            _freeCameraResource();
        }

    }

    public int getTimeCount() {
        return mTimeCount;
    }

    /**
     * @return the mVecordFile
     */
    public File getmVecordFile() {
        return mVecordFile;
    }


    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
