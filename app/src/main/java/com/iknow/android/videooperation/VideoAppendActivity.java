package com.iknow.android.videooperation;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author：J.Chou
 * Date：  2016.06.26 21:47.
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoAppendActivity extends FragmentActivity implements View.OnClickListener, SurfaceHolder.Callback  {

    MediaRecorder mRecorder;
    SurfaceHolder holder;
    boolean recording = false;
    int filename=0;
    int timeCount=0;
    TextView tv_time;
    Timer T;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initRecorder();
        setContentView(R.layout.activity_main);

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.activity_camera_surface);
        tv_time=(TextView)findViewById(R.id.activity_camear_tv);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
    }

    private void initRecorder() {
        mRecorder = new MediaRecorder();
        mRecorder.reset();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mRecorder.setProfile(cpHigh);
        mRecorder.setOutputFile("/sdcard/" + filename + ".mp4");
        mRecorder.setMaxDuration(1800000); // 30 minutes
        mRecorder.setMaxFileSize(500000000); // Approximately 500 megabytes

    }

    private void prepareRecorder() {
        mRecorder.setPreviewDisplay(holder.getSurface());
        try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    public void onClick(View v) {

        if (recording) {
            T.cancel();
            mRecorder.stop();
            recording = false;
            filename++;
            // Let's initRecorder so we can record again
            initRecorder();
            prepareRecorder();
        } else {
            recording = true;
            mRecorder.start();
            T=new Timer();
            T.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_time.setText("count=" + timeCount);
                            timeCount++;
                        }
                    });
                }
            }, 0, 1000);
        }
    }


    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            mRecorder.stop();
            recording = false;
        }
        mRecorder.release();
        finish();
    }

    public void start(View view) throws IOException {
        appendVideo2();

    }

    //使用异步线程处理拼接任务
    public void appendVideo2(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {

                super.onPreExecute();
                Toast.makeText(getApplicationContext(),"start",Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //开始拼接
                    Movie[] inMovies = new Movie[filename];
                    for(int i=0;i<filename;i++){
                        inMovies[i] = MovieCreator.build("/sdcard/" + i + ".mp4");
                    }

                    List<Track> videoTracks = new LinkedList<Track>();
                    List<Track> audioTracks = new LinkedList<Track>();
                    for (Movie m : inMovies) {
                        for (Track t : m.getTracks()) {
                            if (t.getHandler().equals("soun")) {
                                audioTracks.add(t);
                            }
                            if (t.getHandler().equals("vide")) {
                                videoTracks.add(t);
                            }
                        }
                    }

                    Movie result = new Movie();

                    if (audioTracks.size() > 0) {
                        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                    }
                    if (videoTracks.size() > 0) {
                        result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                    }

                    BasicContainer out = (BasicContainer) new DefaultMp4Builder().build(result);

                    FileChannel fc = new RandomAccessFile(String.format(Environment.getExternalStorageDirectory() + "/append.mp4"),"rw").getChannel();
                    out.writeContainer(fc);
                    fc.close();
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(),"end",Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}
