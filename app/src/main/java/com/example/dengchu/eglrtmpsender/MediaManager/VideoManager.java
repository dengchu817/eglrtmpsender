package com.example.dengchu.eglrtmpsender.MediaManager;

import com.example.dengchu.eglrtmpsender.BaseUtil.IRender;
import com.example.dengchu.eglrtmpsender.VideoProcess.SurfaceEncode;
import com.example.dengchu.eglrtmpsender.VideoProcess.SurfaceShow;
import com.example.dengchu.eglrtmpsender.VideoProcess.VideoSurfaceProcessor;
import com.example.dengchu.eglrtmpsender.AVCapture.CameraProvider;
import com.example.dengchu.eglrtmpsender.AVCapture.ITextureProvider;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvDataCollecter;

/**
 * Created by dengchu on 2018/6/6.
 */

public class VideoManager {
    private VideoSurfaceProcessor mTextureProcessor;//filter
    private ITextureProvider mCameraProvider;//数据来源
    private SurfaceShow mShower;//显示
    private SurfaceEncode mEncoder;//编码
    public VideoManager(MediaConfig config){
        //相机
        mCameraProvider=new CameraProvider(config);
        //用于预览图像
        mShower=new SurfaceShow();
        //用于编码
        mEncoder = new SurfaceEncode(config);
        //用于处理视频图像
        mTextureProcessor=new VideoSurfaceProcessor();
        mTextureProcessor.setTextureProvider(mCameraProvider);
        mTextureProcessor.addObserver(mShower);
        mTextureProcessor.addObserver(mEncoder);
        mTextureProcessor.start();
    }
    //======================VideoSurfaceProcessor===================
    public void setRender(IRender render){
        if (mTextureProcessor != null){
            mTextureProcessor.setRender(render);
        }
    }

    public void stop(){
        if (mTextureProcessor != null){
            mTextureProcessor.stop();
        }
    }

    //===========================priview============================
    public void setPreviewSurface(Object surface){
        mShower.setSurface(surface);
    }

    public void setPreviewSize(int width,int height){
        mShower.setPreviewSize(width,height);
    }

    public void startPreview(){
        mShower.open();
    }

    public void stopPreview(){
        mShower.close();
    }

    //=============================Record============================

    public void startRecord(FlvDataCollecter flvDataCollecter){
        mEncoder.start(flvDataCollecter);
    }

    public void stopRecord(){
        mEncoder.close();
    }
}
