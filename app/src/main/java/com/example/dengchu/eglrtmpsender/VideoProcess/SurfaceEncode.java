package com.example.dengchu.eglrtmpsender.VideoProcess;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.example.dengchu.eglrtmpsender.BaseUtil.CodecUtil;
import com.example.dengchu.eglrtmpsender.EncodeThread.VideoEncodeThread;
import com.example.dengchu.eglrtmpsender.Filter.BaseFilter;
import com.example.dengchu.eglrtmpsender.Filter.LazyFilter;
import com.example.dengchu.eglrtmpsender.BaseUtil.IObserver;
import com.example.dengchu.eglrtmpsender.MediaManager.MediaConfig;
import com.example.dengchu.eglrtmpsender.BaseUtil.RenderBean;
import com.example.dengchu.eglrtmpsender.Rtmp.FlvDataCollecter;
import com.example.dengchu.eglrtmpsender.eglutil.MatrixUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dengchu on 2018/6/6.
 */

public class SurfaceEncode  implements IObserver<RenderBean> {
    private String TAG = "=====dc";

    private EGLSurface mShowSurface;
    private BaseFilter mFilter;
    private Surface mSurface;
    private int mWidth;
    private int mHeight;
    private int mMatrixType= MatrixUtils.TYPE_CENTERCROP;
    private MediaConfig mConfig;
    private MediaCodec mVideoEncoder;
    private VideoEncodeThread mVideoEncodeThread ;
    private boolean isEncodeStarted=false;
    private long starttime = -1;
    public SurfaceEncode(MediaConfig config){
        mConfig = config;
    }

    public void setMatrixType(int type){
        this.mMatrixType=type;
    }

    public void start(FlvDataCollecter flvDataCollecter){
        Log.e(TAG,"openVideoEncoder open-->");
        MediaConfig.Video videoconfig = mConfig.mVideo;
        if(mVideoEncoder==null && videoconfig != null){
            try {
                MediaFormat format=MediaFormat.createVideoFormat(videoconfig.mime, videoconfig.encodewidth, videoconfig.encodeheight);
                format.setInteger(MediaFormat.KEY_BIT_RATE, videoconfig.bitrate);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, videoconfig.frameRate);
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoconfig.iframe);
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

                mVideoEncoder= MediaCodec.createEncoderByType(videoconfig.mime);
                mVideoEncoder.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
                mSurface = mVideoEncoder.createInputSurface();
                mWidth = videoconfig.encodewidth;
                mHeight = videoconfig.encodeheight;
                mVideoEncoder.start();
                isEncodeStarted=true;
                mVideoEncodeThread = new VideoEncodeThread("VideoSenderThread", mVideoEncoder, flvDataCollecter);
                mVideoEncodeThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(){
        Log.e("=====dc","close VideoEncoder");
        isEncodeStarted=false;

        if (mVideoEncodeThread != null) {
            mVideoEncodeThread.quit();
            try {
                mVideoEncodeThread.join();
            } catch (InterruptedException e) {

            }
        }
        mVideoEncodeThread = null;

        if(mVideoEncoder!=null){
            mVideoEncoder.stop();
            mVideoEncoder.release();
        }
        mVideoEncoder=null;
    }

    @Override
    public void onCall(RenderBean rb) {
        if(rb.endFlag&&mShowSurface!=null){
            rb.egl.destroySurface(mShowSurface);
            mShowSurface=null;
        }else if(isEncodeStarted&&mSurface!=null){
            if(mShowSurface==null){
                mShowSurface=rb.egl.createWindowSurface(mSurface);
                mFilter=new LazyFilter();
                mFilter.create();
                mFilter.sizeChanged(rb.sourceWidth, rb.sourceHeight);
                MatrixUtils.getMatrix(mFilter.getVertexMatrix(),mMatrixType,rb.sourceWidth,rb.sourceHeight,
                        mWidth,mHeight);
                MatrixUtils.flip(mFilter.getVertexMatrix(),true,false);
            }
            rb.egl.makeCurrent(mShowSurface);
            GLES20.glViewport(0,0,mWidth,mHeight);
            mFilter.draw(rb.textureId);
            onDrawEnd(mShowSurface, rb);
            rb.egl.swapBuffers(mShowSurface);
        }
    }

    public void onDrawEnd(EGLSurface surface, RenderBean bean){
        if(bean.timeStamp!=-1){
            bean.egl.setPresentationTime(surface,bean.timeStamp*1000);
        }else{
            if (starttime == -1)
                starttime = bean.textureTime;
            bean.egl.setPresentationTime(surface,bean.textureTime-starttime);
        }
    }
}
