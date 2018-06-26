package com.example.dengchu.eglrtmpsender.eglutil;

import com.example.dengchu.eglrtmpsender.BaseUtil.IObservable;
import com.example.dengchu.eglrtmpsender.BaseUtil.IObserver;

import java.util.ArrayList;

/**
 * Created by dengchu on 2018/6/5.
 */

public class Observable<Type> implements IObservable<Type> {
    private ArrayList<IObserver<Type>> mTemp;
    @Override
    public void addObserver(IObserver<Type> observer) {
        if(mTemp==null){
            mTemp=new ArrayList<>();
        }
        mTemp.add(observer);
    }

    @Override
    public void notify(Type type) {
        for (IObserver<Type> t:mTemp){
            t.onCall(type);
        }
    }

    public void clear(){
        if(mTemp!=null){
            mTemp.clear();
            mTemp=null;
        }
    }
}
