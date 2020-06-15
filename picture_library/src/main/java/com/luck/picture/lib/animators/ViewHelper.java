package com.luck.picture.lib.animators;

import androidx.core.view.ViewCompat;
import android.view.View;

/**
 * @Author: RenZhengWei
 * @CreateDate: 2020/5/7 16:10
 * @Description: ViewHelper
 */
public class ViewHelper {
    public static void clear(View v) {
        v.setAlpha(1);
        v.setScaleY(1);
        v.setScaleX(1);
        v.setTranslationY(0);
        v.setTranslationX(0);
        v.setRotation(0);
        v.setRotationY(0);
        v.setRotationX(0);
        v.setPivotY(v.getMeasuredHeight() / 2);
        v.setPivotX(v.getMeasuredWidth() / 2);
        ViewCompat.animate(v).setInterpolator(null).setStartDelay(0);
    }
}
