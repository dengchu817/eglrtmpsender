package com.example.dengchu.eglrtmpsender.EncodeThread;

import android.media.MediaCodec;
import android.util.Log;

import com.example.dengchu.eglrtmpsender.Rtmp.FlvData;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvDataCollecter;
import com.example.dengchu.eglrtmpsender.Rtmp.Packager;
import com.example.dengchu.eglrtmpsender.Rtmp.RtmpSender;

import java.nio.ByteBuffer;

/**
 * Created by dengchu on 2018/6/15.
 */

public class AudioEncodeThread extends Thread {
    private static final long WAIT_TIME = 5000;//1ms;
    private MediaCodec.BufferInfo eInfo;
    private MediaCodec dstAudioEncoder;
    private FlvDataCollecter dataCollecter;
    private boolean shouldQuit = false;

    public AudioEncodeThread(String name, MediaCodec encoder, FlvDataCollecter flvDataCollecter){
        eInfo = new MediaCodec.BufferInfo();
        dstAudioEncoder= encoder;
        dataCollecter = flvDataCollecter;
    }

    public void quit(){
        shouldQuit = true;
        this.interrupt();
    }

    @Override
    public void run(){
        while (!shouldQuit) {
            int eobIndex = dstAudioEncoder.dequeueOutputBuffer(eInfo, WAIT_TIME);
            switch (eobIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.e("=====dc","AudioSenderThread,MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
//                        LogTools.d("AudioSenderThread,MediaCodec.INFO_TRY_AGAIN_LATER");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.e("=====dc","AudioSenderThread,MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:" +
                            dstAudioEncoder.getOutputFormat().toString());
                    ByteBuffer csd0 = dstAudioEncoder.getOutputFormat().getByteBuffer("csd-0");
                    sendAudioSpecificConfig(0, csd0);
                    break;
                default:
                    /**
                     * we send audio SpecificConfig already in INFO_OUTPUT_FORMAT_CHANGED
                     * so we ignore MediaCodec.BUFFER_FLAG_CODEC_CONFIG
                     */
                    if (eInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG && eInfo.size != 0) {
                        ByteBuffer realData = dstAudioEncoder.getOutputBuffers()[eobIndex];
                        realData.position(eInfo.offset);
                        realData.limit(eInfo.offset + eInfo.size);
                        sendRealData((eInfo.presentationTimeUs / 1000), realData);
                        Log.e("=====dc","AudioSenderThread,eInfo.presentationTimeUs / 1000:" + eInfo.presentationTimeUs / 1000);
                    }
                    dstAudioEncoder.releaseOutputBuffer(eobIndex, false);
                    break;
            }
        }
        eInfo = null;
    }

    private void sendAudioSpecificConfig(long tms, ByteBuffer realData) {
        int packetLen = Packager.FLVPackager.FLV_AUDIO_TAG_LENGTH +
                realData.remaining();
        byte[] finalBuff = new byte[packetLen];
        realData.get(finalBuff, Packager.FLVPackager.FLV_AUDIO_TAG_LENGTH,
                realData.remaining());
        Packager.FLVPackager.fillFlvAudioTag(finalBuff,
                0,
                true);
        FlvData resFlvData = new FlvData();
        resFlvData.byteBuffer = finalBuff;
        resFlvData.size = finalBuff.length;
        resFlvData.dts = (int) tms;
        resFlvData.flvTagType = FlvData.FLV_RTMP_PACKET_TYPE_AUDIO;
        dataCollecter.collectFlv(resFlvData, RtmpSender.FROM_AUDIO);
    }

    private void sendRealData(long tms, ByteBuffer realData) {
        int packetLen = Packager.FLVPackager.FLV_AUDIO_TAG_LENGTH +
                realData.remaining();
        byte[] finalBuff = new byte[packetLen];
        realData.get(finalBuff, Packager.FLVPackager.FLV_AUDIO_TAG_LENGTH,
                realData.remaining());
        Packager.FLVPackager.fillFlvAudioTag(finalBuff,
                0,
                false);
        FlvData resFlvData = new FlvData();
        resFlvData.byteBuffer = finalBuff;
        resFlvData.size = finalBuff.length;
        resFlvData.dts = tms;
        resFlvData.flvTagType = FlvData.FLV_RTMP_PACKET_TYPE_AUDIO;
        dataCollecter.collectFlv(resFlvData, RtmpSender.FROM_AUDIO);
    }
}
