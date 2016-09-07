package com.android.launcher3.view;

import android.content.Context;
import android.graphics.drawable.Drawable;

import junit.framework.Assert;

/**
 * Created by NineG on 2016/7/3.
 */
public abstract class FoodInfo extends BaseUnitInfo {

    public FoodInfo(Context context) {
        super(context);
    }

    public interface FoodInfoChangedObserver extends OnInfoChangedObserver {
        boolean beEaten(PetInfo petInfo, long progress);
    }

    abstract public Drawable getFoodAsset(Context context);

    @Override
    public BaseUnitView generateView(Context context) {
        return new FoodView(context);
    }

    @Override
    public boolean notifyNewInfo(Context context, BaseUnitInfo newInfo) {
        return false;
    }

    @Override
    public boolean notifyInfoChanged(Context context, BaseUnitInfo infoChanged) {
        return false;
    }

    public void beEaten(PetInfo petInfo, long progress) {
        Assert.assertTrue(mInfoChangedObserver instanceof FoodInfoChangedObserver);
        ((FoodInfoChangedObserver) mInfoChangedObserver).beEaten(petInfo, progress);
    }

    public void onPauseAction() {}
    public void onResumeAction() {}
}
