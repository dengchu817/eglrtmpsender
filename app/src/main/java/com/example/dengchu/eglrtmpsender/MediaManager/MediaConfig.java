package com.example.dengchu.eglrtmpsender.MediaManager;

import android.media.AudioFormat;

/**
 * Created by dengchu on 2018/6/6.
 */

public class MediaConfig {

    public Video mVideo=new Video();
    public Audio mAudio=new Audio();

    public String mUrl = "rtmp://192.168.17.60/live/dc";

    public class Video{
        public String mime="video/avc";
        public int camerawidth = 720;
        public int cameraheight =1280;

        public int encodewidth =720;
        public int encodeheight =1280;
        public int frameRate=15;
        public int iframe=1;
        public int bitrate=1177600;
        public int colorFormat;
    }

    public class  Audio{
        public String mime="audio/aac";
        public int sampleRate=44100;
        public int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        public int channelCount = 1;
        public int bitrate=128000;
        public int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    }

}

