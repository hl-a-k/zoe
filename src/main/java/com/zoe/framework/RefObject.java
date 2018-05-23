package com.zoe.framework;

/**
 * 对应C#中的ref语法
 * Created by caizhicong on 2016/2/17.
 */
public final class RefObject<T> {
    public T value;

    public RefObject(T refValue) {
        value = refValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public static <T> RefObject<T> New(T refValue) {
        return new RefObject<>(refValue);
    }
}
