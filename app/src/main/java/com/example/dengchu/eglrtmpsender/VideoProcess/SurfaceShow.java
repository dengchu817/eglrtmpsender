package com.example.dengchu.eglrtmpsender.VideoProcess;

import android.opengl.EGL14;
import android.opengl.GLES20;

import com.example.dengchu.eglrtmpsender.BaseUtil.RenderBean;
import com.example.dengchu.eglrtmpsender.Filter.BaseFilter;
import com.example.dengchu.eglrtmpsender.BaseUtil.IObserver;
import com.example.dengchu.eglrtmpsender.Filter.LazyFilter;
import com.example.dengchu.eglrtmpsender.eglutil.MatrixUtils;
import android.opengl.EGLSurface;

import static android.opengl.EGL14.EGL_WIDTH;

/**
 * Created by dengchu on 2018/6/6.
 */

public class SurfaceShow implements IObserver<RenderBean> {

    private EGLSurface mEGLSurface;
    private boolean isShow=false;
    private BaseFilter mFilter;
    private Object mSurface;
    private int mWidth, mHeight;
    private int mMatrixType= MatrixUtils.TYPE_CENTERINSIDE;

    public void setPreviewSize(int width,int height){
        this.mWidth=width;
        this.mHeight=height;
    }

    public void setSurface(Object surface){
        this.mSurface=surface;
    }

    /**
     * 设置矩阵变换类型
     * @param type 变换类型，{@link MatrixUtils#TYPE_FITXY},{@link MatrixUtils#TYPE_FITSTART},{@link MatrixUtils#TYPE_CENTERCROP},{@link MatrixUtils#TYPE_CENTERINSIDE}或{@link MatrixUtils#TYPE_FITEND}
     */
    public void setMatrixType(int type){
        this.mMatrixType=type;
    }

    public void open(){
        isShow=true;
    }

    public void close(){
        isShow=false;
    }

    @Override
    public void onCall(RenderBean rb) {
        if(rb.endFlag&&mEGLSurface!=null){
            rb.egl.destroySurface(mEGLSurface);
            mEGLSurface=null;
        }else if(isShow&&mSurface!=null){
            if(mEGLSurface==null){
                mEGLSurface=rb.egl.createWindowSurface(mSurface);
                mFilter=new LazyFilter();
                mFilter.create();
                mFilter.sizeChanged(rb.sourceWidth, rb.sourceHeight);
                MatrixUtils.getMatrix(mFilter.getVertexMatrix(),mMatrixType,rb.sourceWidth,rb.sourceHeight,
                        mWidth, mHeight);
                MatrixUtils.flip(mFilter.getVertexMatrix(),true,false);
            }
            rb.egl.makeCurrent(mEGLSurface);
            GLES20.glViewport(0,0,mWidth,mHeight);
            int[] width = new int[1];
            EGL14.eglQuerySurface(rb.egl.getDisplay(), mEGLSurface, EGL_WIDTH, width, 0);
            mFilter.draw(rb.textureId);
            rb.egl.swapBuffers(mEGLSurface);
        }
    }
}
