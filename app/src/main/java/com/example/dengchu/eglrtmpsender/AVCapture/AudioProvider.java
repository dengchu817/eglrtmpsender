package com.example.dengchu.eglrtmpsender.AVCapture;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.dengchu.eglrtmpsender.MediaManager.MediaConfig;

/**
 * Created by dengchu on 2018/6/15.
 */

public class AudioProvider {
    private AudioRecord audioRecord = null;
    private MediaConfig mConfig = null;
    public AudioProvider(MediaConfig config){
        mConfig = config;
    }

    public int open(){
        int minBufferSize = AudioRecord.getMinBufferSize(mConfig.mAudio.sampleRate, mConfig.mAudio.channelConfig, mConfig.mAudio.audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mConfig.mAudio.sampleRate,
                mConfig.mAudio.channelConfig,
                mConfig.mAudio.audioFormat,
                minBufferSize * 2);
        audioRecord.startRecording();
        return minBufferSize*2;
    }

    public int readpcm(byte[] buffer, int size){
        return audioRecord.read(buffer,0, size);
    }

    public void close(){
        audioRecord.stop();
    }
}
