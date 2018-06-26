package com.example.dengchu.eglrtmpsender.AudioProcess;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;

import com.example.dengchu.eglrtmpsender.AVCapture.AudioProvider;
import com.example.dengchu.eglrtmpsender.EncodeThread.AudioEncodeThread;
import com.example.dengchu.eglrtmpsender.MediaManager.MediaConfig;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvDataCollecter;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dengchu on 2018/6/15.
 */

public class AudioProcessor{

    private AudioProvider mAudioProvider = null;
    private AudioEncoderer mAudioEncoder = null;

    private boolean mGLThreadFlag=false;
    private Thread mProcessThread = null;
    private final Object LOCK=new Object();

    public void setAudioRecord(AudioProvider record){
        mAudioProvider = record;
    }

    public void setAudioEncoder(AudioEncoderer encoder){
        mAudioEncoder = encoder;
    }

    public void start(){
        synchronized (LOCK){
            if(!mGLThreadFlag){
                mGLThreadFlag = true;
                mProcessThread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processRun();
                    }
                });
                mProcessThread.start();
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop(){
        synchronized (LOCK){
            if(mGLThreadFlag){
                mGLThreadFlag = false;

                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mAudioProvider.close();
            }
        }
    }


    public void processRun(){

        int bufferSize = mAudioProvider.open();
        byte[] audioBuffer = new byte[bufferSize];

        synchronized (LOCK){
            LOCK.notifyAll();
        }
        while (mGLThreadFlag) {
            int readSize = mAudioProvider.readpcm(audioBuffer,  audioBuffer.length);
            //to add filter
            mAudioEncoder.feedpcm(audioBuffer, readSize);
        }
        synchronized (LOCK){
            LOCK.notifyAll();
        }
    }
}
