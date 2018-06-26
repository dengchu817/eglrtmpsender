/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.dengchu.eglrtmpsender.VideoProcess;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;

import com.example.dengchu.eglrtmpsender.BaseUtil.IObserver;
import com.example.dengchu.eglrtmpsender.BaseUtil.IRender;
import com.example.dengchu.eglrtmpsender.AVCapture.ITextureProvider;
import com.example.dengchu.eglrtmpsender.BaseUtil.RenderBean;
import com.example.dengchu.eglrtmpsender.eglutil.EGLConfigAttrs;
import com.example.dengchu.eglrtmpsender.eglutil.EGLContextAttrs;
import com.example.dengchu.eglrtmpsender.eglutil.EglHelper;
import com.example.dengchu.eglrtmpsender.Filter.FrameBuffer;
import com.example.dengchu.eglrtmpsender.eglutil.GpuUtils;
import com.example.dengchu.eglrtmpsender.eglutil.Observable;

/**
 * VideoSurfaceProcessor 视频流图像处理类，以{@link ITextureProvider}作为视频流图像输入。通过设置{@link IObserver}
 * 来接收处理完毕的{@link RenderBean}，并做相应处理，诸如展示、编码等。
 *
 * @author wuwang
 * @version v1.0 2017:10:27 08:37
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class VideoSurfaceProcessor {

    private String TAG=getClass().getSimpleName();

    private boolean mGLThreadFlag=false;
    private Thread mGLThread = null;
    private WrapRenderer mRenderer = null;
    private Observable<RenderBean> observable = null;
    private final Object LOCK=new Object();
    private boolean bNeedSetRender = false;
    private IRender mRender = null;
    private ITextureProvider mProvider = null;

    public VideoSurfaceProcessor(){
        observable=new Observable<RenderBean>();
    }

    public void setRender(IRender render){
        mRender = render;
        bNeedSetRender = true;
    }

    public void setTextureProvider(ITextureProvider provider){
        this.mProvider=provider;
    }

    public void start(){
        synchronized (LOCK){
            if(!mGLThreadFlag){
                if(mProvider==null){
                    return;
                }
                mGLThreadFlag=true;
                mGLThread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glRun();
                    }
                });
                mGLThread.start();
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
                mGLThreadFlag=false;
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mProvider.close();
            }
        }
    }

    private void glRun(){
        EglHelper egl=new EglHelper();
        boolean ret=egl.createGLESWithSurface(new EGLConfigAttrs(),new EGLContextAttrs(),new SurfaceTexture(1));
        if(!ret){
            //todo 错误处理
            return;
        }
        int mInputSurfaceTextureId = GpuUtils.createTextureID(true);
        SurfaceTexture mInputSurfaceTexture = new SurfaceTexture(mInputSurfaceTextureId);

        Point size=mProvider.open(mInputSurfaceTexture);
        Log.d(TAG,"Provider Opened . data size (x,y)="+size.x+"/"+size.y);
        if(size.x<=0||size.y<=0){
            //todo 错误处理
            destroyGL(egl);
            synchronized (LOCK){
                LOCK.notifyAll();
            }
            return;
        }
        int mSourceWidth = size.x;
        int mSourceHeight = size.y;
        synchronized (LOCK){
            LOCK.notifyAll();
        }
        //要求数据源提供者必须同步返回数据大小
        if(mSourceWidth <=0|| mSourceHeight <=0){
            error(1,"video source return inaccurate size to SurfaceTextureActuator");
            return;
        }

        FrameBuffer sourceFrame=new FrameBuffer();

//        bNeedSetRender = false;
//        mRenderer = new WrapRenderer(mRender);
//        mRenderer.create();
//        mRenderer.sizeChanged(mSourceWidth, mSourceHeight);
//        mRenderer.setFlag(mProvider.isLandscape()?WrapRenderer.TYPE_CAMERA:WrapRenderer.TYPE_MOVE);

        //用于其他的回调
        RenderBean rb=new RenderBean();
        rb.egl=egl;
        rb.sourceWidth= mSourceWidth;
        rb.sourceHeight= mSourceHeight;
        rb.endFlag=false;
        rb.threadId= Thread.currentThread().getId();
        Log.d(TAG,"Processor While Loop Entry");
        //要求数据源必须同步填充SurfaceTexture，填充完成前等待
        while (!mProvider.frame()&&mGLThreadFlag){
            if (bNeedSetRender || mRenderer == null){
                bNeedSetRender = false;
                if (mRenderer != null)
                    mRenderer.destroy();
                mRenderer = new WrapRenderer(mRender);
                mRenderer.create();
                mRenderer.sizeChanged(mSourceWidth, mSourceHeight);
                mRenderer.setFlag(mProvider.isLandscape()?WrapRenderer.TYPE_CAMERA:WrapRenderer.TYPE_MOVE);
            }
            mInputSurfaceTexture.updateTexImage();
            //mInputSurfaceTexture.getTransformMatrix(mRenderer.getTextureMatrix());
            //Log.d(TAG,"timestamp:"+ mInputSurfaceTexture.getTimestamp());
            sourceFrame.bindFrameBuffer(mSourceWidth, mSourceHeight);
            GLES20.glViewport(0,0, mSourceWidth, mSourceHeight);
            mRenderer.draw(mInputSurfaceTextureId);
            sourceFrame.unBindFrameBuffer();

            rb.textureId=sourceFrame.getCacheTextureId();
            //接收数据源传入的时间戳
            rb.timeStamp = mProvider.getTimeStamp();
            rb.textureTime= mInputSurfaceTexture.getTimestamp();
            observable.notify(rb);
        }
        Log.d(TAG,"out of gl thread loop");
        synchronized (LOCK){
            rb.endFlag=true;
            observable.notify(rb);
            mRenderer.destroy();
            destroyGL(egl);
            LOCK.notifyAll();
            Log.d(TAG,"gl thread exit");
        }
    }

    private void destroyGL(EglHelper egl){
        mGLThreadFlag=false;
        EGL14.eglMakeCurrent(egl.getDisplay(), EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(egl.getDisplay(),egl.getDefaultContext());
        EGL14.eglTerminate(egl.getDisplay());
    }

    public void addObserver(IObserver<RenderBean> observer) {
        observable.addObserver(observer);
    }

    protected void error(int id,String msg) {

    }
}

