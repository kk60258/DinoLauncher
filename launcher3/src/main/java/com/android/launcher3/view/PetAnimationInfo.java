package com.android.launcher3.view;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NineG on 2016/7/3.
 */
public class PetAnimationInfo {
    protected List<InfoSet> mList = new ArrayList<InfoSet>();
    private boolean mReady = true;

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

    public void next() {
        mCurrentIndex ++;
        if (mCurrentIndex >= size())
            mCurrentIndex = 0;
    }

    public int size() {
        return mList.size();
    }

    public boolean isReady () {
        return mReady;
    }
}
