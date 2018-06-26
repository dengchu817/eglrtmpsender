package com.example.dengchu.eglrtmpsender.AVCapture;

import android.graphics.Point;
import android.graphics.SurfaceTexture;

/**
 * Created by dengchu on 2018/6/5.
 */

public interface ITextureProvider {

    /*
     *输出surfacetext
     *@return 视频流的宽高
     */
    Point open(SurfaceTexture surfaceTexture);

    /**
     * 关闭视频流数据源
     */
    void close();

    /**
     * 获取一帧数据
     * @return 是否最后一帧
     */
    boolean frame();

    /**
     * 获取当前帧时间戳
     * @return 时间戳
     */
    long getTimeStamp();

    /**
     * 视频流是否是横向的
     * @return true or false
     */
    boolean isLandscape();

}
