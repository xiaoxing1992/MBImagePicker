package com.luck.picture.lib.animators;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @Author: RenZhengWei
 * @CreateDate: 2020/5/7 16:09
 * @Description: SlideInBottomAnimationAdapter
 */
public class SlideInBottomAnimationAdapter extends BaseAnimationAdapter {

    public SlideInBottomAnimationAdapter(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    protected Animator[] getAnimators(View view) {
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0)
        };
    }
}
