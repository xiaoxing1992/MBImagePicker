package com.luck.picture.lib.listener;

import java.util.List;

/**
 * @Author: RenZhengWei
 * @CreateDate: 2020/4/21 17:58
 * @Description: Listener
 */
@Deprecated
public interface OnMediaLoadListener<T> {
    /**
     * 加载完成
     *
     * @param data
     */
    void loadComplete(List<T> data);

    /**
     * 异常
     */
    void loadMediaDataError();
}
