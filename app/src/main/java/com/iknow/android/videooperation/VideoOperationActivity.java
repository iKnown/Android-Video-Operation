package com.iknow.android.videooperation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.MediaController;
import android.widget.Toast;

import com.iknow.android.videooperation.databinding.OperationLayoutBinding;
import com.iknow.android.videooperation.utils.DeviceHelper;
import com.iknow.android.videooperation.interfaces.IShortVideo;
import com.iknow.android.videooperation.utils.FileUtils;

import java.io.File;

public class VideoOperationActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
  private static final String BUNDLE_LENGTH = "BUNDLE_VIDEO_LENGTH";
  private static final String BUNDLE_SIZE = "BUNDLE_VIDEO_SIZE";
  private static final String BUNDLE_QUALITY = "BUNDLE_VIDEO_QUALITY";
  private static final String BUNDLE_COMPRESS = "BUNDLE_VIDEO_COMPRESS";

  private static final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
  private static final int REQUEST_STORAGE_PERMISSION = 123;
  private static final int ACTION_RECORD_VIDEO = 3;
  private static final int ACTION_CHOOSER_VIDEO = 4;
  private static final int ACTION_TRIM_VIDEO = 5;

  private MediaController mediaController;
  private String videoFile;
  private int mMaxLength = 15;
  private int mQuality = 1;
  private long mMaxSize = 30; //In Mb
  private static final long K = 1024;
  private boolean mCompress = false;
  private OperationLayoutBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding =  DataBindingUtil.setContentView(this,R.layout.operation_layout);

    DeviceHelper.init(this);

    binding.fab.setOnClickListener(this);
    binding.appendVideo.setOnClickListener(this);
    binding.shortVideo.setOnClickListener(this);
    binding.videoEdit.setOnClickListener(this);
    initVideoView();
  }

  private void initVideoView(){
    mediaController = new MediaController(this,false);
    binding.videoView.setMediaController(mediaController);
    mediaController.setMediaPlayer(binding.videoView);
  }

  @Override
  public void onClick(View view) {
    if(view.getId() == binding.fab.getId())
      record();
    else if(view.getId() == binding.videoEdit.getId())
      trimVideo();
    else if(view.getId() == binding.appendVideo.getId()){
      chooseVideo();
    }else if(view.getId() == binding.shortVideo.getId())
      new ShortVideoDialog(VideoOperationActivity.this).build(new IShortVideo() {
        @Override
        public void getVideoFile(File f) {
          if(f != null){
              binding.ad.setVisibility(View.GONE);

            binding.videoView.setVideoPath(f.getAbsolutePath());
            binding.videoView.requestFocus();
            binding.videoView.start();
          }
        }
      }).show();
  }

  private void record(){
    Intent record = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    record.putExtra(MediaStore.EXTRA_DURATION_LIMIT, mMaxLength);
    record.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, mQuality );
    record.putExtra(MediaStore.EXTRA_SIZE_LIMIT, mMaxSize * K * K);
    if(record.resolveActivity(getPackageManager())!=null){
      startActivityForResult(record, ACTION_RECORD_VIDEO);
    }else{

    }
  }

  private void trimVideo(){
    Intent intent = new Intent();
    intent.setTypeAndNormalize("video/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(intent, ACTION_TRIM_VIDEO);
  }

  private void chooseVideo() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("video/*");
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

    try {
      startActivityForResult(intent, ACTION_CHOOSER_VIDEO);
    } catch (android.content.ActivityNotFoundException ex) {
      Toast.makeText(this, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(BUNDLE_LENGTH, mMaxLength);
    outState.putLong(BUNDLE_SIZE, mMaxSize);
    outState.putInt(BUNDLE_QUALITY, mQuality);
    outState.putBoolean(BUNDLE_COMPRESS, mCompress);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    if (savedInstanceState!=null) {
      mMaxLength = savedInstanceState.getInt(BUNDLE_LENGTH, mMaxLength);
      mMaxSize = savedInstanceState.getLong(BUNDLE_SIZE, mMaxSize);
      mQuality = savedInstanceState.getInt(BUNDLE_QUALITY, mQuality);
      mCompress = savedInstanceState.getBoolean(BUNDLE_COMPRESS, mCompress);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == RESULT_OK && data !=null){
      if (requestCode == ACTION_RECORD_VIDEO) {
        Uri uri = data.getData();
        videoFile = getPath(VideoOperationActivity.this,uri);
        File f = new File(videoFile);
        binding.videoView.setVideoPath(f.getAbsolutePath());
        binding.videoView.requestFocus();
        binding.videoView.start();

      }else if(requestCode == ACTION_CHOOSER_VIDEO){
        Uri uri = data.getData();
      }else if(requestCode == ACTION_TRIM_VIDEO){
        final Uri selectedUri = data.getData();
        if (selectedUri != null) {
          Intent intent = new Intent(this, VideoTrimmerActivity.class);
          intent.putExtra("EXTRA_VIDEO_PATH", FileUtils.getPath(this, selectedUri));
          startActivity(intent);
        } else {
          Toast.makeText(VideoOperationActivity.this, "Cannot retrieve selected video", Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
    if(!hasPermission()){
      requestPermission();
    }
    mCompress = b;
  }


  private boolean hasPermission(){
    return ActivityCompat.checkSelfPermission(this, PERMISSION) == PackageManager.PERMISSION_GRANTED;
  }

  private void requestPermission(){
    ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, REQUEST_STORAGE_PERMISSION);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if(requestCode== REQUEST_STORAGE_PERMISSION){
      // Check if the only required permission has been granted
      if (!(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
      }
    }else{
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  public static String getPath(Context context, Uri uri) {

    if ("content".equalsIgnoreCase(uri.getScheme())) {
      String[] projection = { "_data" };
      Cursor cursor = null;

      try {
        cursor = context.getContentResolver().query(uri, projection,null, null, null);
        int column_index = cursor.getColumnIndexOrThrow("_data");
        if (cursor.moveToFirst()) {
          return cursor.getString(column_index);
        }
      } catch (Exception e) {
        // Eat it
      }
    }

    else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }

    return null;
  }
}
