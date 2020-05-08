package com.luck.picture.lib.listener;

import java.util.List;

/**
 * @Author: RenZhengWei
 * @CreateDate: 2020/5/7 16:22
 * @Description: OnQueryDataResultListener
 */
public interface OnQueryDataResultListener<T> {
    /**
     * Query to complete The callback listener
     *
     * @param data        The data source
     * @param currentPage The page number
     * @param isHasMore   Is there more
     */
    void onComplete(List<T> data, int currentPage, boolean isHasMore);
}
