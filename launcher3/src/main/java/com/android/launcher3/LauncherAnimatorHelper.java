package com.android.launcher3;

import android.animation.Animator;
import android.animation.ObjectAnimator;

/**
 * Created by NineG on 2016/7/9.
 *
 * Help to access FirstFrameAnimatorHelper in order to setTarget(View) after creation.
 */
public class LauncherAnimatorHelper {
    private Animator mAnimator;
    private FirstFrameAnimatorHelper mFirstFrameHelper;
    public LauncherAnimatorHelper(Animator animator, FirstFrameAnimatorHelper firstFrameHelper) {
        this.mAnimator =animator;
        this.mFirstFrameHelper = firstFrameHelper;
    }

    public Animator getAnimator() {
        return mAnimator;
    }

    public FirstFrameAnimatorHelper getFirstFrameAnimatorHelper() {
        return mFirstFrameHelper;
    }
}
