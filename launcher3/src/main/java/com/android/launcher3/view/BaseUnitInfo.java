package com.android.launcher3.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by NineG on 2016/7/3.
 */
public abstract class BaseUnitInfo {

    String id;

    protected float x, y;

    public void setX(float x) {
        this.x = x;
    }

    public float getX() {
        return x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getY() {
        return y;
    }

    protected State mCurrentState = State.Idle;

    enum State {
        Idle("idle"), Eat("eat"), Sleep("sleep"), Move("move");
        private int nextX, nextY;
        private int duration;
        private final String key;
        State(String key) {
            this.key = key;
        }
    }


    class Size {
        int width;
        int height;
        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    protected  HashMap<State, Size> mSizeMap = new HashMap<State, Size>();

    public BaseUnitInfo(Context context) {
        this(context, UUID.randomUUID().toString());
    }

    public BaseUnitInfo(Context context, String id) {
        this.id = id;
        initSize(context);
    }

    public int getCurrentWidth() {
        return mSizeMap.get(mCurrentState).width;
    }

    public int getCurrentHeight() {
        return mSizeMap.get(mCurrentState).height;
    }

    public void setState (State state) {
        onStateChanged(mCurrentState, state);
        mCurrentState = state;
    }

    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BaseUnitInfo) {
            return TextUtils.equals(((BaseUnitInfo) o).id, this.id);
        }
        return super.equals(o);
    }

    public interface OnInfoChangedObserver {
        void onPause();
        void onResume();
        void onDragStart();
        void onDragEnd();
    }

    protected OnInfoChangedObserver mInfoChangedObserver= null;

    public void setInfoChangedObserver(OnInfoChangedObserver observer) {
        mInfoChangedObserver = observer;
    }

    public void clearOnInfoChangedObserver() {
        mInfoChangedObserver = null;
    }

    public void onDragStart() {
        Assert.assertNotNull(mInfoChangedObserver);
        mInfoChangedObserver.onDragStart();
    }

    public void onDragEnd() {
        Assert.assertNotNull(mInfoChangedObserver);
        mInfoChangedObserver.onDragEnd();
    }

    public void onPauseAction() {
        Assert.assertNotNull(mInfoChangedObserver);
        mInfoChangedObserver.onPause();
    }

    public void onResumeAction() {
        Assert.assertNotNull(mInfoChangedObserver);
        mInfoChangedObserver.onResume();
    }

    abstract  protected void initSize(Context context);
    abstract  protected void onStateChanged(State oldState, State newState);
    abstract  public BaseUnitView generateView(Context context);
    abstract  public boolean notifyNewInfo(Context context, BaseUnitInfo newInfo);
    abstract  public boolean notifyInfoChanged(Context context, BaseUnitInfo infoChanged);
}
