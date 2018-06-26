package com.example.dengchu.eglrtmpsender;

/**
 * Created by dengchu on 2018/6/6.
 */

public class FilterChoose {
    private int index;
    private String name;

    public FilterChoose(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

