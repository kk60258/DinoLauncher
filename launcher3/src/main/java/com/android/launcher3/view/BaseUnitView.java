package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.android.launcher3.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NineG on 2016/5/16.
 */
public class BaseUnitView extends FrameLayout {
    private OnTouchListener mInterceptTouchListener;
    private static final String LOG_TAG = Logger.getLogTag(BaseUnitView.class);

    public BaseUnitView(Context context) {
        this(context, null);
    }

    public BaseUnitView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseUnitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnInterceptTouchListener(View.OnTouchListener listener) {
        mInterceptTouchListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // First we clear the tag to ensure that on every touch down we start with a fresh slate,
        // even in the case where we return early. Not clearing here was causing bugs whereby on
        // long-press we'd end up picking up an item from a previous drag operation.
        if (mInterceptTouchListener != null && mInterceptTouchListener.onTouch(this, ev)) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mInfo != null) {
            int measureWidth = mInfo.getCurrentWidth();
            int measureHeight = mInfo.getCurrentHeight();

            int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measureWidth, MeasureSpec.EXACTLY);
            int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY);
            setMeasuredDimension(newWidthMeasureSpec, newHeightMeasureSpec);
            Logger.d(LOG_TAG, "onMeasure w %s, h %s", measureWidth, measureHeight);

            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    child.measure(newWidthMeasureSpec, newHeightMeasureSpec);
                }
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            final View child = getChildAt(i);
//            if (child.getVisibility() != GONE) {
//                ParkViewHost.LayoutParams lp = (ParkViewHost.LayoutParams) child.getLayoutParams();
//                int childLeft = lp.x;
//                int childTop = lp.y;
//                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);
//            }
//        }
//    }

    protected BaseUnitInfo mInfo;

    public void setUnitInfo(BaseUnitInfo info) {
        mInfo = info;
        mInfo.setState(BaseUnitInfo.State.Idle);
    }

    public BaseUnitInfo getUnitInfo() {
        return mInfo;
    }

    void removeViewFromParent() {
        ViewParent vp = getParent();
        if (vp instanceof ViewGroup) {
            ((ViewGroup) vp).removeView(this);
        } else {
            Logger.d(LOG_TAG, "removeViewFromParent fail %s", this);
        }
    }
}
