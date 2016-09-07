package com.android.launcher3.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.launcher3.util.Logger;

/**
 * Created by NineG on 2016/7/3.
 */
public class FoodView extends BaseUnitView implements FoodInfo.FoodInfoChangedObserver {
    private static final String LOG_TAG = Logger.getLogTag(FoodView.class);

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
        super.setUnitInfo(info);

        if (mInfo instanceof FoodInfo) {
            Drawable b = ((FoodInfo) mInfo).getFoodAsset(this.getContext());
            setBackground(b);
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
