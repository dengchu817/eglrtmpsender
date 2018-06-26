package com.example.dengchu.eglrtmpsender.Rtmp;

import com.example.dengchu.eglrtmpsender.MediaManager.MediaConfig;

import java.util.ArrayList;

/**
 * Created by dengchu on 2018/6/7.
 */

public class FLvMetaData {
    private static final String Name = "onMetaData";
    private static final int ScriptData = 18;
    private static final byte[] TS_SID = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] ObjEndMarker = {0x00, 0x00, 0x09};
    private static final int EmptySize = 21;
    private ArrayList<byte[]> MetaData;
    private int DataSize;
    private int pointer;
    private byte[] MetaDataFrame;

    public FLvMetaData() {
        MetaData = new ArrayList<>();
        DataSize = 0;
    }

    public FLvMetaData(MediaConfig config) {
        this();
        //Audio
        //AAC
        int audioBitRate = config.mAudio.bitrate;
        int audioSampleRate = config.mAudio.sampleRate;
        int videofps = config.mVideo.frameRate;
        int videoWidth = config.mVideo.encodewidth;
        int videoHeight = config.mVideo.encodeheight;
        setProperty("audiocodecid", 10);
        switch (audioBitRate) {
            case 32 * 1024:
                setProperty("audiodatarate", 32);
                break;
            case 48 * 1024:
                setProperty("audiodatarate", 48);
                break;
            case 64 * 1024:
                setProperty("audiodatarate", 64);
                break;
        }

        switch (audioSampleRate) {
            case 44100:
                setProperty("audiosamplerate", 44100);
                break;
            default:
                break;
        }
        //Video
        //h264
        setProperty("videocodecid", 7);
        setProperty("framerate", videofps);
        setProperty("encodewidth", videoWidth);
        setProperty("encodeheight", videoHeight);
    }

    public void setProperty(String Key, int value) {
        addProperty(toFlvString(Key), (byte) 0, toFlvNum(value));
    }

    public void setProperty(String Key, String value) {
        addProperty(toFlvString(Key), (byte) 2, toFlvString(value));
    }

    private void addProperty(byte[] Key, byte datatype, byte[] data) {
        int Propertysize = Key.length + 1 + data.length;
        byte[] Property = new byte[Propertysize];

        System.arraycopy(Key, 0, Property, 0, Key.length);
        Property[Key.length] = datatype;
        System.arraycopy(data, 0, Property, Key.length + 1, data.length);

        MetaData.add(Property);
        DataSize += Propertysize;
    }

    public byte[] getMetaData() {
        MetaDataFrame = new byte[DataSize + EmptySize];
        pointer = 0;
        //SCRIPTDATA.name
        Addbyte(2);
        AddbyteArray(toFlvString(Name));
        //SCRIPTDATA.value ECMA array
        Addbyte(8);
        AddbyteArray(toUI(MetaData.size(), 4));
        for (byte[] Property : MetaData) {
            AddbyteArray(Property);
        }
        AddbyteArray(ObjEndMarker);
        return MetaDataFrame;
    }

    private void Addbyte(int value) {
        MetaDataFrame[pointer] = (byte) value;
        pointer++;
    }

    private void AddbyteArray(byte[] value) {
        System.arraycopy(value, 0, MetaDataFrame, pointer, value.length);
        pointer += value.length;
    }

    private byte[] toFlvString(String text) {
        byte[] FlvString = new byte[text.length() + 2];
        System.arraycopy(toUI(text.length(), 2), 0, FlvString, 0, 2);
        System.arraycopy(text.getBytes(), 0, FlvString, 2, text.length());
        return FlvString;
    }

    private byte[] toUI(long value, int bytes) {
        byte[] UI = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            UI[bytes - 1 - i] = (byte) (value >> (8 * i) & 0xff);
        }
        return UI;
    }

    private byte[] toFlvNum(double value) {
        long tmp = Double.doubleToLongBits(value);
        return toUI(tmp, 8);
    }
}

