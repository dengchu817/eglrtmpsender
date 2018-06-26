package com.example.dengchu.eglrtmpsender.MediaManager;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.dengchu.eglrtmpsender.AVCapture.AudioProvider;
import com.example.dengchu.eglrtmpsender.AudioProcess.AudioEncoderer;
import com.example.dengchu.eglrtmpsender.AudioProcess.AudioProcessor;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvData;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvDataCollecter;

/**
 * Created by dengchu on 2018/6/15.
 */

public class AudioManager {
    private AudioProvider mAudioProvider = null;
    private AudioProcessor mPcmProcessor;
    private AudioEncoderer mEncoder;
    private MediaConfig mConfig = null;

    public AudioManager(MediaConfig config){
        mConfig = config;
        mAudioProvider = new AudioProvider(config);
        mEncoder = new AudioEncoderer(mConfig);
        mPcmProcessor = new AudioProcessor();
        mPcmProcessor.setAudioRecord(mAudioProvider);
        mPcmProcessor.setAudioEncoder(mEncoder);
        mPcmProcessor.start();
    }

    public void startReord(FlvDataCollecter flvDataCollecter){
        mEncoder.startencode(flvDataCollecter);
    }

    public void stopRecord(){
        mEncoder.stopencode();
    }
}
