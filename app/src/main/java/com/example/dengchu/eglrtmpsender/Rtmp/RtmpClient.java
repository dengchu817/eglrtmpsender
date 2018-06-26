package com.example.dengchu.eglrtmpsender.Rtmp;

/**
 * Created by dengchu on 2018/6/7.
 */

public class RtmpClient {

    static {
        System.loadLibrary("resrtmp");
    }

    /**
     * @param url
     * @param isPublishMode
     * @return rtmpPointer ,pointer to native rtmp struct
     */
    public static native long open(String url, boolean isPublishMode);

    public static native int read(long rtmpPointer, byte[] data, int offset, int size);

    public static native int write(long rtmpPointer, byte[] data, int size, int type, int ts);

    public static native int close(long rtmpPointer);

    public static native String getIpAddr(long rtmpPointer);

}
