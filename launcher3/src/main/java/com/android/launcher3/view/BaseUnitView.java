package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.android.launcher3.util.Logger;

import junit.framework.Assert;

/**
 * Created by NineG on 2016/5/16.
 */
public class BaseUnitView extends FrameLayout implements BaseUnitInfo.OnInfoChangedObserver {
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

    private void registerInfoChangedObserver(BaseUnitInfo info) {
        if (info != null) {
            info.setInfoChangedObserver(this);
        } else {
            Logger.d(LOG_TAG, "invalid info %s", info);
        }
    }

    private void clearOnInfoChangedObserver() {
        if (mInfo != null) {
            mInfo.clearOnInfoChangedObserver();
        } else {
            Logger.d(LOG_TAG, "invalid info %s", mInfo);
        }
    }

    public void setUnitInfo(BaseUnitInfo info) {
        clearOnInfoChangedObserver();
        setTag(info);
        mInfo = info;
        mInfo.setState(BaseUnitInfo.State.Idle);
        registerInfoChangedObserver(info);
    }

    @Override
    public void setTag(final Object tag) {
        Assert.assertTrue(tag instanceof BaseUnitInfo);
        super.setTag(tag);
    }

    public BaseUnitInfo getUnitInfo() {
        mInfo = (BaseUnitInfo) getTag();
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


    /**
     * Sets the visual x position of this view, in pixels. This is equivalent to setting the
     * {@link #setTranslationX(float) translationX} property to be the difference between
     * the x value passed in and the current {@link #getLeft() left} property.
     *
     * @param x The visual x position of this view, in pixels.
     */
    @Override
    public void setX(float x) {
        super.setX(x);
        mInfo.setX(x);
//        Logger.d(LOG_TAG, "setX %s, %s", x, getTranslationX());
    }

    /**
     * Sets the visual y position of this view, in pixels. This is equivalent to setting the
     * {@link #setTranslationY(float) translationY} property to be the difference between
     * the y value passed in and the current {@link #getTop() top} property.
     *
     * @param y The visual y position of this view, in pixels.
     */
    @Override
    public void setY(float y) {
        super.setY(y);
        mInfo.setY(y);
//        Logger.d(LOG_TAG, "setY %s, %s", y, getTranslationY());
    }

    /**
     * The visual x position of this view, in pixels. This is equivalent to the
     * {@link #setTranslationX(float) translationX} property plus the current
     * {@link #getLeft() left} property.
     *
     * @return The visual x position of this view, in pixels.
     */
    @Override
    public float getX() {
        return super.getX();
//        return mInfo.getX();
    }

    /**
     * The visual y position of this view, in pixels. This is equivalent to the
     * {@link #setTranslationY(float) translationY} property plus the current
     * {@link #getTop() top} property.
     *
     * @return The visual y position of this view, in pixels.
     */
    @Override
    public float getY() {
        return super.getY();
//        return mInfo.getY();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        setTranslationX(0);
//        setTranslationY(0);
        Logger.d(LOG_TAG, "onLayout %s, %s", getX(), getY());
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDragStart() {
        this.setAlpha(0.5f);
    }

    @Override
    public void onDragEnd() {
        this.setAlpha(1.0f);
        setX(mInfo.getX());
        setY(mInfo.getY());
    }
}


