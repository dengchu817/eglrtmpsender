package com.example.dengchu.eglrtmpsender.MediaManager;

import com.example.dengchu.eglrtmpsender.BaseUtil.IRender;
import com.example.dengchu.eglrtmpsender.Rtmp.RtmpSender;

/**
 * Created by dengchu on 2018/6/7.
 */

public class SimpleStreamer {
    private MediaConfig mConfig;
    private VideoManager mVideoManager;
    private AudioManager mAudioManager;
    private RtmpSender mSender;


    public SimpleStreamer(MediaConfig config){
        mConfig = config;
        mVideoManager = new VideoManager(mConfig);
        mAudioManager = new AudioManager(mConfig);
        mSender = new RtmpSender();
    }

    public void setRender(IRender render){
        if (mVideoManager != null){
            mVideoManager.setRender(render);
        }
    }

    public void setPreviewSurface(Object surface){
        if (mVideoManager != null){
            mVideoManager.setPreviewSurface(surface);
        }
    }

    public void setPreviewSize(int width, int height){
        if (mVideoManager != null){
            mVideoManager.setPreviewSize(width, height);
        }
    }

    public void startPreview(){
        if (mVideoManager != null){
            mVideoManager.startPreview();
        }
    }

    public void stopPreview(){
        if (mVideoManager != null){
            mVideoManager.stopPreview();
        }
    }

    public void startRecord(){
        if (mVideoManager != null){
            mSender.prepare(mConfig);
            mSender.start();
            long starttime = System.currentTimeMillis();//音视频从同一点开始采集
            mAudioManager.startReord(mSender);
            mVideoManager.startRecord(mSender);
        }
    }

    public void stopRecord(){
        if (mVideoManager != null){
            mVideoManager.stopRecord();
            mAudioManager.stopRecord();
        }
    }
}
