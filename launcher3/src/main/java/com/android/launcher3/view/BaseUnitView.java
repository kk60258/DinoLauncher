package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by NineG on 2016/5/16.
 */
public class BaseUnitView extends FrameLayout {
    private OnTouchListener mInterceptTouchListener;

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
}
