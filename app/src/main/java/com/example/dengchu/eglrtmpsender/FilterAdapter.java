package com.example.dengchu.eglrtmpsender;

import com.example.dengchu.eglrtmpsender.WheelView.WheelView;

import java.util.List;

/**
 * Created by dengchu on 2018/6/6.
 */

public class FilterAdapter extends WheelView.WheelAdapter {

    private List<FilterChoose> strs;

    public FilterAdapter(List<FilterChoose> filterChooses) {
        strs = filterChooses;
    }

    @Override
    protected int getItemCount() {
        return strs.size();
    }

    @Override
    protected String getItem(int index) {
        return strs.get(index).getName();
    }
}

