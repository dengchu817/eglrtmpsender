package com.example.dengchu.eglrtmpsender.EncodeThread;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.EGLSurface;
import android.util.Log;

import com.example.dengchu.eglrtmpsender.BaseUtil.RenderBean;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvData;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvDataCollecter;
import com.example.dengchu.eglrtmpsender.Rtmp.Packager;
import com.example.dengchu.eglrtmpsender.Rtmp.RtmpSender;

import java.nio.ByteBuffer;

import static android.content.ContentValues.TAG;

/**
 * Created by dengchu on 2018/6/7.
 */

public class VideoEncodeThread extends Thread {
    private static final long WAIT_TIME = 10000;
    private MediaCodec.BufferInfo eInfo;
    private MediaCodec dstVideoEncoder;
    private FlvDataCollecter dataCollecter;
    private String TAG = "=====dc";

    public VideoEncodeThread(String name, MediaCodec encoder, FlvDataCollecter flvDataCollecter) {
        super(name);
        eInfo = new MediaCodec.BufferInfo();
        dstVideoEncoder = encoder;
        dataCollecter = flvDataCollecter;
    }

    private boolean shouldQuit = false;

    public void quit() {
        shouldQuit = true;
        this.interrupt();
    }

    public void onDrawEnd(EGLSurface surface, RenderBean bean){

    }

    @Override
    public void run() {
        while (!shouldQuit) {
            int eobIndex = dstVideoEncoder.dequeueOutputBuffer(eInfo, WAIT_TIME);
            switch (eobIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(TAG,"VideoSenderThread,MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
//                        LogTools.d("VideoSenderThread,MediaCodec.INFO_TRY_AGAIN_LATER");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG,"VideoSenderThread,MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:" +
                            dstVideoEncoder.getOutputFormat().toString());
                    sendAVCDecoderConfigurationRecord(0, dstVideoEncoder.getOutputFormat());
                    break;
                default:

                    /**
                     * we send sps pps already in INFO_OUTPUT_FORMAT_CHANGED
                     * so we ignore MediaCodec.BUFFER_FLAG_CODEC_CONFIG
                     */
                    if (eInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG && eInfo.size != 0) {
                        ByteBuffer realData = dstVideoEncoder.getOutputBuffers()[eobIndex];
                        realData.position(eInfo.offset + 4);
                        realData.limit(eInfo.offset + eInfo.size);
                        sendRealData((eInfo.presentationTimeUs / 1000), realData);
                    }
                    dstVideoEncoder.releaseOutputBuffer(eobIndex, false);
                    break;
            }
        }
        eInfo = null;
    }

    private void sendAVCDecoderConfigurationRecord(long tms, MediaFormat format) {
        byte[] AVCDecoderConfigurationRecord = Packager.H264Packager.generateAVCDecoderConfigurationRecord(format);
        int packetLen = Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                AVCDecoderConfigurationRecord.length;
        byte[] finalBuff = new byte[packetLen];
        Packager.FLVPackager.fillFlvVideoTag(finalBuff,
                0,
                true,
                true,
                AVCDecoderConfigurationRecord.length);
        System.arraycopy(AVCDecoderConfigurationRecord, 0,
                finalBuff, Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH, AVCDecoderConfigurationRecord.length);
        FlvData resFlvData = new FlvData();
        resFlvData.byteBuffer = finalBuff;
        resFlvData.size = finalBuff.length;
        resFlvData.dts = (int) tms;
        resFlvData.flvTagType = FlvData.FLV_RTMP_PACKET_TYPE_VIDEO;
        resFlvData.videoFrameType = FlvData.NALU_TYPE_IDR;
        dataCollecter.collectFlv(resFlvData, RtmpSender.FROM_VIDEO);
    }

    private void sendRealData(long tms, ByteBuffer realData) {
        int realDataLength = realData.remaining();
        int packetLen = Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                Packager.FLVPackager.NALU_HEADER_LENGTH +
                realDataLength;
        byte[] finalBuff = new byte[packetLen];
        realData.get(finalBuff, Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                        Packager.FLVPackager.NALU_HEADER_LENGTH,
                realDataLength);
        int frameType = finalBuff[Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                Packager.FLVPackager.NALU_HEADER_LENGTH] & 0x1F;
        Packager.FLVPackager.fillFlvVideoTag(finalBuff,
                0,
                false,
                frameType == 5,
                realDataLength);
        FlvData resFlvData = new FlvData();
        resFlvData.byteBuffer = finalBuff;
        resFlvData.size = finalBuff.length;
        resFlvData.dts = tms;
        resFlvData.flvTagType = FlvData.FLV_RTMP_PACKET_TYPE_VIDEO;
        resFlvData.videoFrameType = frameType;
        dataCollecter.collectFlv(resFlvData, RtmpSender.FROM_VIDEO);
    }
}