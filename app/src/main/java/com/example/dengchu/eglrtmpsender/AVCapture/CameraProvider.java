package com.example.dengchu.eglrtmpsender.AVCapture;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.example.dengchu.eglrtmpsender.MediaManager.MediaConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by dengchu on 2018/6/5.
 */

public class CameraProvider implements ITextureProvider {
    private Camera mCamera = null;
    private int cameraId = 1;
    private Semaphore mFrameSem = null;
    private MediaConfig mConfig;
    private String Tag = getClass().getSimpleName();

    public CameraProvider(MediaConfig config){
        mConfig = config;
    }
    @Override
    public Point open(SurfaceTexture surface){//相机预览高宽默认
        final Point size=new Point();
        try {
            mFrameSem=new Semaphore(0);
            mCamera=Camera.open(cameraId);

            //相机的高宽需要旋转90度
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFrameRate(mConfig.mVideo.frameRate);
            List<Camera.Size> sizes =  parameters.getSupportedPictureSizes();
            Collections.sort(sizes, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size lhs, Camera.Size rhs) {
                    if ((lhs.width * lhs.height) > (rhs.width * rhs.height)) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            for(Camera.Size size1 : sizes){
                if (size1.height >= mConfig.mVideo.camerawidth && size1.width >= mConfig.mVideo.cameraheight){
                    mConfig.mVideo.camerawidth = size1.height;
                    mConfig.mVideo.cameraheight = size1.width;
                    Log.e(Tag,"=====dc camera preview size width:"+mConfig.mVideo.camerawidth+", height:"+mConfig.mVideo.cameraheight);

                    break;
                }
            }
            parameters.setPreviewSize(mConfig.mVideo.cameraheight, mConfig.mVideo.camerawidth);
            mCamera.setParameters(parameters);

            mCamera.setPreviewTexture(surface);
            surface.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    mFrameSem.drainPermits();
                    mFrameSem.release();
                }
            });
            Camera.Size s=mCamera.getParameters().getPreviewSize();
            mCamera.startPreview();
            size.x=s.height;
            size.y=s.width;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    @Override
    public  void close(){

        mFrameSem.drainPermits();
        mFrameSem.release();

        mCamera.stopPreview();
        mCamera.release();
        mCamera=null;
    }

    @Override
    public boolean frame(){
        try {
            mFrameSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public long getTimeStamp(){
        long TimeStamp = -1;
        return TimeStamp;
    }

    @Override
    public boolean isLandscape(){
        return true;
    }
}
