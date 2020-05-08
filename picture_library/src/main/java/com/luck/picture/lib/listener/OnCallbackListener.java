package com.luck.picture.lib.listener;

/**
 * @Author: RenZhengWei
 * @CreateDate: 2020/5/8 12:10
 * @Description: OnCallbackListener
 */
public interface OnCallbackListener<T> {
    /**
     * @param data
     */
    void onCall(T data);
}
