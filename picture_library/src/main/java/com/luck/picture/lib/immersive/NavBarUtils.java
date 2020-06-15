package com.luck.picture.lib.immersive;

import android.os.Build;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author：luck
 * @date：2019-11-25 20:58
 * @describe：NavBar工具类
 */
public class NavBarUtils {
    /**
     * 动态设置 NavBar 色值
     *
     * @param activity
     * @param color
     */
    public static void setNavBarColor(@NonNull final AppCompatActivity activity, @ColorInt final int color) {
        setNavBarColor(activity.getWindow(), color);
    }

    public static void setNavBarColor(@NonNull final Window window, @ColorInt final int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color);
        }
    }
}
