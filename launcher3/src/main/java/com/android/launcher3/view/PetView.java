package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;

import com.android.launcher3.Alarm;
import com.android.launcher3.OnAlarmListener;
import com.android.launcher3.util.Logger;

import java.util.List;

/**
 * Created by NineG on 2016/7/3.
 */
public class PetView extends BaseUnitView implements PetInfo.OnInfoChangedObserver, OnAlarmListener {
    private static final String LOG_TAG = Logger.getLogTag(PetView.class);
    protected PetAnimationInfo mPetAnimationInfo;

    public PetView(Context context) {
        super(context);
    }

    public PetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setUnitInfo(BaseUnitInfo info) {
        clearOnInfoChangedObserver(mInfo);
        super.setUnitInfo(info);

        if (info instanceof PetInfo) {
            PetInfo petInfo = (PetInfo) info;
            mPetAnimationInfo = petInfo.getPetAnimationInfo(this.getContext());
            registerInfoChangedObserver(petInfo);
        } else {
            Logger.d(LOG_TAG, "invalid info %s", info);
        }
    }

    public void registerInfoChangedObserver(PetInfo petInfo) {
        petInfo.setOnInfoChangedObserver(this);
    }

    public void clearOnInfoChangedObserver(BaseUnitInfo info) {
        if (info instanceof PetInfo) {
            ((PetInfo) info).clearOnInfoChangedObserver();
        } else {
            Logger.d(LOG_TAG, "invalid info %s", info);
        }
    }

    @Override
    public boolean onHungry() {
        return false;
    }

    @Override
    public boolean onEat(FoodInfo foodInfo) {
        return false;
    }

    Paint mPaint = new Paint();
    Paint debugPaint1;
    Paint debugPaint2;
    long mLastDrawTime;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Logger.d(LOG_TAG, "dispatchDraw %s", this);
        if (mPetAnimationInfo != null && mPetAnimationInfo.isReady()) {
            PetAnimationInfo.InfoSet set = mPetAnimationInfo.getCurrentSet();
            long duration = set.duration;
            long timelapsed = System.currentTimeMillis() - mLastDrawTime;
            Logger.d(LOG_TAG, "set timelapsed %s, duration", timelapsed, duration);
            if (timelapsed > duration) {
                timelapsed = 0;
                mPetAnimationInfo.next();
                set = mPetAnimationInfo.getCurrentSet();
                mLastDrawTime = System.currentTimeMillis();
            }
            Logger.d(LOG_TAG, "set bitmap %s/%s", set.bitmap.getWidth(), set.bitmap.getHeight());
            canvas.drawBitmap(set.bitmap, set.left, set.top, mPaint);
            scheduleNextInvalidate(duration - timelapsed);
        }
        super.dispatchDraw(canvas);
        if (debugPaint1 == null) {
            debugPaint1 = new Paint();
            debugPaint1.setColor(0xffcccc00);
        }

        if (getClipBounds() != null) {
            Logger.d(LOG_TAG, "draw clip bound %s", this);
            canvas.drawRect(getClipBounds(), debugPaint1);
        }

        if (debugPaint2 == null) {
            debugPaint2 = new Paint();
            debugPaint2.setColor(0xff00cccc);
        }

        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), debugPaint2);
    }

    private Alarm mAlarm;

    private void scheduleNextInvalidate(long delay) {
        if (mAlarm == null) {
            mAlarm = new Alarm();
            mAlarm.setOnAlarmListener(this);
        }
        mAlarm.setAlarm(delay);
    }

    @Override
    public void onAlarm(Alarm alarm) {
        invalidate();
    }
}
