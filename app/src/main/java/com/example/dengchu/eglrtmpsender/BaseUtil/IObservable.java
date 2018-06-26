package com.example.dengchu.eglrtmpsender.BaseUtil;

/**
 * Created by wuwang on 2017/10/20.
 */

public interface IObservable<Type> {

    void addObserver(IObserver<Type> observer);

    void notify(Type type);

}
