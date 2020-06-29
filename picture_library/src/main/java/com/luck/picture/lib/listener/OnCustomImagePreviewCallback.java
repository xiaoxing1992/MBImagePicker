package com.luck.picture.lib.listener;

import android.content.Context;

import java.util.List;

/**
 * @Author: RenZhengWei
 * @CreateDate: 2020/6/29 16:29
 * @Description: describe：OnImagePreviewCallback
 */
public interface OnCustomImagePreviewCallback<T> {
    /**
     * Custom Preview Callback
     *
     * @param context
     * @param previewData
     * @param currentPosition
     */
    void onCustomPreviewCallback(Context context, List<T> previewData, int currentPosition);
}
