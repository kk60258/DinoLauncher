package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by NineG on 2016/7/3.
 */
public abstract class FoodInfo extends BaseUnitInfo {

    public FoodInfo(Context context) {
        super(context);
    }

    public interface OnInfoChangedObserver {
        boolean beEaten(PetInfo petInfo, long progress);
    }

    private OnInfoChangedObserver mOnInfoChangedObserver= null;

    public void setOnInfoChangedObserver(OnInfoChangedObserver observer) {
        mOnInfoChangedObserver = observer;
    }

    public void clearOnInfoChangedObserver() {
        mOnInfoChangedObserver = null;
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

    public void beEaten(PetInfo petInfo, long progress) {
        if (mOnInfoChangedObserver != null)
            mOnInfoChangedObserver.beEaten(petInfo, progress);
    }
}
