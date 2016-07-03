package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.animation.AnimationSet;

import junit.framework.Assert;

import java.util.List;

/**
 * Created by NineG on 2016/7/3.
 */
public abstract class PetInfo extends BaseUnitInfo {
    public PetInfo(Context context) {
        super(context);
    }

    public interface OnInfoChangedObserver {
        boolean onHungry();
        boolean onEat(FoodInfo foodInfo);
    }

    private OnInfoChangedObserver mOnInfoChangedObserver= null;

    public void setOnInfoChangedObserver(OnInfoChangedObserver observer) {
        mOnInfoChangedObserver = observer;
    }

    public void clearOnInfoChangedObserver() {
        mOnInfoChangedObserver = null;
    }

    boolean hungry(FoodInfo foodInfo, PetInfo petInfo) {
        return true;
    }

    boolean eat(FoodInfo foodeInfo) {
        //playAnimation
        Assert.assertNotNull(mOnInfoChangedObserver);

        boolean result = mOnInfoChangedObserver.onEat(foodeInfo);
        return result;
    }

    abstract PetAnimationInfo getPetAnimationInfo(Context context);
}
