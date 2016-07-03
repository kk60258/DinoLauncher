package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.android.launcher3.util.Logger;

/**
 * Created by NineG on 2016/7/3.
 */
public class FoodView extends BaseUnitView implements FoodInfo.OnInfoChangedObserver {
    private static final String LOG_TAG = Logger.getLogTag(PetView.class);

    public FoodView(Context context) {
        super(context);
    }

    public FoodView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoodView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setUnitInfo(BaseUnitInfo info) {
        clearOnInfoChangedObserver();
        super.setUnitInfo(info);
        registerInfoChangedObserver(info);

        if (mInfo instanceof FoodInfo) {
            Drawable b = ((FoodInfo) mInfo).getFoodAsset(this.getContext());
            setBackground(b);
        }
    }

    public void registerInfoChangedObserver(BaseUnitInfo info) {
        if (info instanceof FoodInfo) {
            ((FoodInfo) info).setOnInfoChangedObserver(this);
        } else {
            Logger.d(LOG_TAG, "invalid info %s", info);
        }
    }

    public void clearOnInfoChangedObserver() {
        if (mInfo instanceof FoodInfo) {
            ((FoodInfo) mInfo).clearOnInfoChangedObserver();
        } else {
            Logger.d(LOG_TAG, "invalid info %s", mInfo);
        }
    }

    @Override
    public boolean beEaten(PetInfo petInfo, long progress) {
        if (progress >= 1) {
            removeViewFromParent();
            return true;
        }
        return false;
    }
}
