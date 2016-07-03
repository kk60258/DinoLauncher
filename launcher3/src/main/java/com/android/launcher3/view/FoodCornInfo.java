package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.android.launcher3.R;

/**
 * Created by NineG on 2016/7/3.
 */
public class FoodCornInfo extends FoodInfo {

    public FoodCornInfo(Context context) {
        super(context);
    }

    @Override
    protected void initSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mSizeMap.clear();
        mSizeMap.put(State.Idle, new Size(pxFromDp(41, dm), pxFromDp(38, dm)));
    }

    @Override
    protected void onStateChanged(State oldState, State newState) {

    }

    @Override
    public Drawable getFoodAsset(Context context) {
        return context.getResources().getDrawable(R.drawable.food_corn);
    }
}
