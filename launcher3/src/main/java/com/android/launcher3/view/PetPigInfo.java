package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.animation.AnimationSet;

import com.android.launcher3.LauncherAnimUtils;
import com.android.launcher3.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by NineG on 2016/7/3.
 */
public class PetPigInfo extends PetInfo {
    public PetPigInfo(Context context) {
        super(context);
    }

    @Override
    protected void initSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mSizeMap.clear();
//        mSizeMap.put(State.Idle, new Size(208, 159));
        mSizeMap.put(State.Idle, new Size(pxFromDp(104, dm), pxFromDp(79.5f, dm)));
//        mSizeMap.put(State.Eat, new Size(128, 162));
        mSizeMap.put(State.Eat, new Size(pxFromDp(64, dm), pxFromDp(81, dm)));
//        mSizeMap.put(State.Sleep, new Size(228, 140));
        mSizeMap.put(State.Sleep, new Size(pxFromDp(114, dm), pxFromDp(70, dm)));
    }

    @Override
    protected void onStateChanged(State oldState, State newState) {

    }

    @Override
    PetAnimationInfo getPetAnimationInfo(Context context) {
        PetAnimationInfo info = null;
        if (mCurrentState == State.Idle) {
            info = new PetAnimationInfo(
                    new PetAnimationInfo.InfoSet(((BitmapDrawable) context.getResources().getDrawable(R.drawable.pet_pig_walk1)).getBitmap(), 1000),
                    new PetAnimationInfo.InfoSet(((BitmapDrawable) context.getResources().getDrawable(R.drawable.pet_pig_walk2)).getBitmap(), 1000)
            );
        }

        return info;
    }
}
