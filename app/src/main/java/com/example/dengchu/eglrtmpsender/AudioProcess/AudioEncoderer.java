package com.example.dengchu.eglrtmpsender.AudioProcess;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;

import com.example.dengchu.eglrtmpsender.EncodeThread.AudioEncodeThread;
import com.example.dengchu.eglrtmpsender.MediaManager.MediaConfig;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvDataCollecter;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dengchu on 2018/6/19.
 */

public class AudioEncoderer {
    private AudioEncodeThread mAudioEncodeThread = null;
    private MediaCodec dstAudioEncoder;
    private MediaFormat dstAudioFormat;
    private boolean isEncodeStart = false;
    private MediaConfig mConfig;
    private long starttime = -1;

    public AudioEncoderer(MediaConfig config){
        mConfig = config;
    }

    public void startencode(FlvDataCollecter flvDataCollecter){

        dstAudioFormat = new MediaFormat();
        dstAudioFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        dstAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        dstAudioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, mConfig.mAudio.sampleRate);
        dstAudioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mConfig.mAudio.channelCount);
        dstAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, mConfig.mAudio.bitrate);
        dstAudioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 8820);

        if (dstAudioEncoder == null) {
            try {
                dstAudioEncoder = MediaCodec.createEncoderByType(dstAudioFormat.getString(MediaFormat.KEY_MIME));
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        dstAudioEncoder.configure(dstAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        dstAudioEncoder.start();

        mAudioEncodeThread = new AudioEncodeThread("AudioSenderThread", dstAudioEncoder, flvDataCollecter);
        mAudioEncodeThread.start();
        isEncodeStart = true;
    }

    public void stopencode(){
        isEncodeStart = false;
        if (mAudioEncodeThread != null) {
            mAudioEncodeThread.quit();
            try {
                mAudioEncodeThread.join();
            } catch (InterruptedException e) {

            }
        }
        mAudioEncodeThread = null;

        if (dstAudioEncoder != null) {
            dstAudioEncoder.stop();
            dstAudioEncoder.release();
        }
        dstAudioEncoder = null;

    }

    public void feedpcm(byte[] buffer, int length){
        if (isEncodeStart) {
            long nowTimeMs = System.currentTimeMillis();
            if (starttime == -1)
                starttime = nowTimeMs;
            int eibIndex = dstAudioEncoder.dequeueInputBuffer(-1);
            if (eibIndex >= 0) {
                ByteBuffer dstAudioEncoderIBuffer = dstAudioEncoder.getInputBuffers()[eibIndex];
                dstAudioEncoderIBuffer.position(0);
                dstAudioEncoderIBuffer.put(buffer, 0, length);
                dstAudioEncoder.queueInputBuffer(eibIndex, 0, length, (nowTimeMs-starttime)*1000, 0);
            } else {
                Log.e("=====dc ", "dstAudioEncoder.dequeueInputBuffer(-1)<0");
            }
        }
    }
}
