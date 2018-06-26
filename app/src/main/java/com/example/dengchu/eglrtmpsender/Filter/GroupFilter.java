package com.example.dengchu.eglrtmpsender.Filter;

import android.content.res.Resources;

import com.example.dengchu.eglrtmpsender.BaseUtil.IRender;

import java.util.Vector;

/**
 * Created by dengchu on 2018/6/6.
 */

public class GroupFilter implements IRender {
    private Vector<BaseFilter> mGroup;
    private ChooseFilter mChooseFilter;
    Resources mRe;
    public GroupFilter(Resources resource){
        mGroup=new Vector<>();
        mChooseFilter = new ChooseFilter(resource);
        addFilter(mChooseFilter);
    }

    public synchronized void addFilter(BaseFilter filter){
        mGroup.add(filter);
    }

    public ChooseFilter getChooseFilter(){
        return mChooseFilter;
    }

    public boolean removeFilter(BaseFilter filter){
        return mGroup.remove(filter);
    }

    @Override
    public void create() {

        for (BaseFilter filter : mGroup) {
            filter.create();
        }
    }

    @Override
    public void sizeChanged(int width, int height) {
        for (BaseFilter filter : mGroup) {
            filter.sizeChanged(width, height);
        }
    }

    @Override
    public void draw(int texture) {

        int tempTextureId = texture;
        BaseFilter filter = null;
        for (int i=0;i<mGroup.size();i++){
            filter=mGroup.get(i);
            if (i == mGroup.size()-1)
                filter.draw(tempTextureId);
            else
                tempTextureId=filter.drawToTexture(tempTextureId);
        }
    }

    public int drawToTexture(int texture) {

        int tempTextureId=texture;
        for (int i=0;i<mGroup.size();i++){
            BaseFilter filter=mGroup.get(i);
            tempTextureId=filter.drawToTexture(tempTextureId);
        }
        return tempTextureId;
    }

    @Override
    public void destroy() {
        for (BaseFilter filter : mGroup) {
            filter.destroy();
        }
    }
}
