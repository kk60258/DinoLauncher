package com.android.launcher3.view;

import android.graphics.Bitmap;

import com.android.launcher3.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NineG on 2016/7/3.
 */
public class PetAnimationInfo {
    private static final String LOG_TAG = Logger.getLogTag(PetAnimationInfo.class);

    protected List<InfoSet> mList = new ArrayList<InfoSet>();
    private boolean mReady = true;
    private boolean horizontalReverse;

    private int mCurrentIndex = 0;

    public PetAnimationInfo(InfoSet... sets) {
        int length = sets.length;
        for(int i = 0; i <length; ++i) {
            mList.add(sets[i]);
        }
    }

    public static class InfoSet {
        Bitmap bitmap;
        int left, top;
        long duration;

        public InfoSet(Bitmap bitmap, long duration) {
            this.bitmap = bitmap;
            this.duration = duration;
        }
    }

    public InfoSet getCurrentSet() {
        return mList.get(mCurrentIndex);
    }

    long mLastDrawTime;

    //@return time to next frame
    public long next() {
        long timelapsed = System.currentTimeMillis() - mLastDrawTime;
        PetAnimationInfo.InfoSet set = getCurrentSet();
        Logger.d(LOG_TAG, "next timelapsed %s, duration %s", timelapsed, set.duration);

        if (set.duration < 0)
            return -1;

        long diff = timelapsed - set.duration;
        if (diff > 0) {
            mCurrentIndex++;
            if (mCurrentIndex >= size())
                mCurrentIndex = 0;

            mLastDrawTime = System.currentTimeMillis();
            set = getCurrentSet();
            return set.duration;
        }
        return diff;
    }

    public int size() {
        return mList.size();
    }

    public boolean isReady() {
        return mReady;
    }

    public void setHorizontalReverse(boolean reverse) {
        this.horizontalReverse = reverse;
    }

    public boolean isHorizontalReverse() {
        return horizontalReverse;
    }
}
