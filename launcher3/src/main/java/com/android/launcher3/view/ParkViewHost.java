package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import com.android.launcher3.DragController;
import com.android.launcher3.DragSource;
import com.android.launcher3.DragView;
import com.android.launcher3.DropTarget;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAnimUtils;
import com.android.launcher3.R;
import com.android.launcher3.util.Logger;

import java.util.Random;

/**
 * Created by NineG on 2016/5/15.
 */
public class ParkViewHost extends ViewGroup implements DropTarget, DragSource, View.OnTouchListener,
        DragController.DragListener, ViewGroup.OnHierarchyChangeListener, Insettable {
    private static final String LOG_TAG = Logger.getLogTag(ParkViewHost.class);

    private UnitManager mUnitManager;
    protected final Rect mInsets = new Rect();
    private DragController mDragController;
    private Launcher mLauncher;
    private BaseUnitViewDragHelper mDragHelper;

    public ParkViewHost(Context context) {
        this(context, null);
    }

    public ParkViewHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParkViewHost(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnHierarchyChangeListener(this);
    }

    /**
     * Handle measure / layout ++
     * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthSpecSize, heightSpecSize);
            }
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                ParkViewHost.LayoutParams lp = (ParkViewHost.LayoutParams) child.getLayoutParams();
                int childLeft = (int) lp.getX();
                int childTop = (int) lp.getY();
                Logger.d(LOG_TAG, "onLayout %s %s ,%s", childLeft, childTop, child);
                child.setTranslationX(0);
                child.setTranslationY(0);
                child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            }
        }
    }

    public void measureChild(View child, int maxWidth, int maxHeight) {
//        ParkViewHost.LayoutParams lp = (ParkViewHost.LayoutParams) child.getLayoutParams();
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
        int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        child.measure(childWidthMeasureSpec, childheightMeasureSpec);
    }
    /**
     * Handle measure / layout --
     * */

    /**
     * Handle LayoutParams ++
     * */

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    protected LayoutParams generateLayoutParams(BaseUnitInfo info) {
        return new LayoutParams(info);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        BaseUnitInfo info;
        public LayoutParams(BaseUnitInfo info) {
            super(info.getCurrentWidth(), info.getCurrentHeight());
            this.info = info;
        }

        private int x, y;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp);
        }

//        public void setWidth(int width) {
//            this.width = width;
//        }
//
//        public int getWidth() {
//            return width;
//        }
//
//        public void setHeight(int height) {
//            this.height = height;
//        }
//
//        public int getHeight() {
//            return height;
//        }
//
//        public void setX(int x) {
//            this.x = x;
//        }
//
//        public int getX() {
//            return x;
//        }
//
//        public void setY(int y) {
//            this.y = y;
//        }
//
//        public int getY() {
//            return y;
//        }

        public int getWidth() {
            return info.getCurrentWidth();
        }
        public int getHeight() {
            return info.getCurrentHeight();
        }

        public void setX(float x) {
            info.setX(x);
        }

        public float getX() {
            return info.getX();
        }

        public void setY(float y) {
            info.setY(y);
        }

        public float getY() {
            return info.getY();
        }
    }

    /**
     * Handle LayoutParams --
     * */

    /**
     * A drag has begun
     *
     * @param source     An object representing where the drag originated
     * @param info       The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
     *                   or {@link DragController#DRAG_ACTION_COPY}
     */
    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        if (source == this && info instanceof BaseUnitInfo) {
            mDragController.addDropTarget(this);
        }
        mUnitManager.notifyPauseByDragged();
    }

    /**
     * The drag has ended
     */
    @Override
    public void onDragEnd() {
        mDragController.removeDropTarget(this);
        mUnitManager.notifyResumeByDragged();
    }

    /**
     * @return whether items dragged from this source supports
     */
    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    /**
     * @return whether items dragged from this source supports 'App Info'
     */
    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    /**
     * @return whether items dragged from this source supports 'Delete' drop target (e.g. to remove
     * a shortcut.
     */
    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    /**
     * A callback specifically made back to the source after an item from this source has been flung
     * to be deleted on a DropTarget.  In such a situation, this method will be called after
     * onDropCompleted, and more importantly, after the fling animation has completed.
     */
    @Override
    public void onFlingToDeleteCompleted() {

    }

    /**
     * A callback made back to the source after an item from this source has been dropped on a
     * DropTarget.
     *
     * @param target
     * @param d
     * @param isFlingToDelete
     * @param success
     */
    @Override
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success) {

    }

    /**
     * Used to temporarily disable certain drop targets
     *
     * @return boolean specifying whether this drop target is currently enabled
     */
    @Override
    public boolean isDropEnabled(DragObject dragObject) {
        if (dragObject != null && dragObject.dragInfo instanceof BaseUnitInfo)
            return true;
        return false;
    }

    /**
     * Handle an object being dropped on the DropTarget
     *
     * @param dragObject
     */
    @Override
    public void onDrop(DragObject dragObject) {
        if (dragObject == null || !(dragObject.dragInfo instanceof BaseUnitInfo))
            return;
        dragObject.deferDragViewCleanupPostAnimation = false;
        BaseUnitInfo unitInfo = (BaseUnitInfo) dragObject.dragInfo;

        float[] coord = getDragViewLoc(dragObject.x, dragObject.y, dragObject.xOffset, dragObject.yOffset, null);
        unitInfo.setX(coord[0]);
        unitInfo.setY(coord[1]);
        unitInfo.onDragEnd();
        mUnitManager.notifyInfoChanged(getContext(), unitInfo);
    }

    @Override
    public void onDragEnter(DragObject dragObject) {

    }

    @Override
    public void onDragOver(DragObject dragObject) {

    }

    @Override
    public void onDragExit(DragObject dragObject) {

    }

    /**
     * Handle an object being dropped as a result of flinging to delete and will be called in place
     * of onDrop().  (This is only called on objects that are set as the DragController's
     * fling-to-delete target.
     *
     * @param dragObject
     * @param x
     * @param y
     * @param vec
     */
    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {

    }

    /**
     * Check if a drop action can occur at, or near, the requested location.
     * This will be called just before onDrop.
     *
     * @param dragObject@return True if the drop will be accepted, false otherwise.
     */
    @Override
    public boolean acceptDrop(DragObject dragObject) {
        return isDropEnabled(dragObject);
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        super.getHitRect(outRect);
//        mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, outRect);
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
//        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    @Override
    public void setInsets(Rect insets) {
        mInsets.set(insets);
    }

    /**
     * Called when a new child is added to a parent view.
     *
     * @param parent the view in which a child was added
     * @param child  the new child view added in the hierarchy
     */
    @Override
    public void onChildViewAdded(View parent, View child) {
        if (!(child instanceof BaseUnitView)) {
            throw new IllegalArgumentException("A ParkViewHost can only have BaseUnitView children.");
        }
        BaseUnitView cl = ((BaseUnitView) child);
        cl.setOnInterceptTouchListener(this);
        cl.setClickable(true);
        mUnitManager.addUnit(cl.getUnitInfo());

        if (cl instanceof PetView) {
            ((PetView) cl).startMovement();
        }
    }
    /**
     * Called when a child is removed from a parent view.
     *
     * @param parent the view from which the child was removed
     * @param child  the child removed from the hierarchy
     */
    @Override
    public void onChildViewRemoved(View parent, View child) {
        if (!(child instanceof BaseUnitView)) {
            throw new IllegalArgumentException("A ParkViewHost can only have BaseUnitView children.");
        }

        BaseUnitView cl = ((BaseUnitView) child);
        mUnitManager.remove(cl.getUnitInfo());
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public void setup(Launcher launcher, DragController dragController) {
        mLauncher = launcher;
        mDragController = dragController;
        mDragHelper = new BaseUnitViewDragHelper(mLauncher, mDragController, this);
    }

    private OnLongClickListener mOnLongClickListener = null;

    public void setCustomizedOnLongClickListener(OnLongClickListener l) {
        mOnLongClickListener = l;
    }

    public boolean addInScreen(BaseUnitView child, BaseUnitInfo info, int x, int y) {
        if (child == null) {
            Logger.d(LOG_TAG, "addInScreen fail %s %s %s", info, x, y);
            return false;
        }

        Logger.d(LOG_TAG, "addInScreen %s %s %s", info, x, y);

        LayoutParams params = generateLayoutParams(info);
        params.setX(x);
        params.setY(y);
        child.setLayoutParams(params);
        child.setUnitInfo(info);
        child.setTag(info);
        child.setOnLongClickListener(mOnLongClickListener);
        addView(child);
        return true;
    }


    public boolean fireInScreen(BaseUnitView child, BaseUnitInfo info, int x, int y, Runnable actionAfterFire) {
        if (child == null) {
            Logger.d(LOG_TAG, "addInScreen %s %s %s", info, x, y);
            return false;
        }

        LayoutParams params = generateLayoutParams(info);
        params.setX(x - info.getCurrentWidth() / 2);
        params.setY(y - info.getCurrentHeight());
        child.setLayoutParams(params);
        child.setUnitInfo(info);
        child.setTag(info);
        child.setOnLongClickListener(mOnLongClickListener);
        child.setPivotX(0.5f*info.getCurrentWidth());
        child.setPivotY(0.5f*info.getCurrentHeight());
        addView(child);
        int[] tagetLoc = getTargetLoc(info.getCurrentWidth(), info.getCurrentHeight());
//        child.setX(params.x);
//        child.setY(params.y);
//        child.setScaleX(0.5f);
//        child.setScaleY(0.5f);

//        layout(getLeft(), getTop(), getRight(), getBottom());
        playFireAnimation(child, params.getX(), params.getY(), tagetLoc[0], tagetLoc[1], 1f, actionAfterFire);

        Logger.d(LOG_TAG, "fireInScreen %s %s %s %s, %s", params.getX(), params.getY(), tagetLoc[0], tagetLoc[1], child);
        return true;
    }

    Random mRandom = new Random();
    private int[] getTargetLoc(int offsetX, int offsetY) {
        int x = mRandom.nextInt(getChildPossibleLocX()[1] - offsetX);
        int y = mRandom.nextInt(getChildPossibleLocY()[1] - offsetY);

        return new int[]{x, y};
    }

    public void playFireAnimation(final View targetView, final float initX, final float initY, final float targetX, final float targetY, final float initScale, final Runnable actionAfterFire) {
        Keyframe kfTranslationX0 = Keyframe.ofFloat(0, initX);
        Keyframe kfTranslationX100 = Keyframe.ofFloat(100, targetX);

        Keyframe kfTranslationY0 = Keyframe.ofFloat(0, initY);
        Keyframe kfTranslationY100 = Keyframe.ofFloat(100, targetY);

        Keyframe kfScale0 = Keyframe.ofFloat(0, initScale);
        Keyframe kfScale50 = Keyframe.ofFloat(0.5f, 3f);
        Keyframe kfScale100 = Keyframe.ofFloat(1f, 1f);

        ValueAnimator fireAnimator = LauncherAnimUtils.ofPropertyValuesHolder(targetView,
                PropertyValuesHolder.ofKeyframe("x", kfTranslationX0, kfTranslationX100),
                PropertyValuesHolder.ofKeyframe("y", kfTranslationY0, kfTranslationY100),
                PropertyValuesHolder.ofKeyframe("scaleX", kfScale0, kfScale50, kfScale100),
                PropertyValuesHolder.ofKeyframe("scaleY", kfScale0, kfScale50, kfScale100));

        fireAnimator.setInterpolator(new DecelerateAccelerateInterpolator(2f));
        fireAnimator.setDuration(1000);
        fireAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Logger.d(LOG_TAG, "playFireAnimation end %s, %s", targetX, targetY);
                LayoutParams params = (LayoutParams) targetView.getLayoutParams();
                params.setX(targetX);
                params.setY(targetY);
                mUnitManager.notifyNewItemFired(targetView.getContext(), ((BaseUnitView) targetView).getUnitInfo());

                if (actionAfterFire != null)
                    actionAfterFire.run();
            }
        });
        fireAnimator.start();
    }

    public class DecelerateAccelerateInterpolator implements Interpolator {
        private float mFactor = 1.0f;

        public DecelerateAccelerateInterpolator() {
        }

        public DecelerateAccelerateInterpolator(float factor) {
            mFactor = factor;
        }

        public float getInterpolation(float x) {
            float result;
            if (x < 0.5) {
                result = (float) (1.0f - Math.pow((1.0f - 2 * x), 2 * mFactor)) / 2;
            } else {
                result = (float) Math.pow((x - 0.5) * 2, 2 * mFactor) / 2 + 0.5f;
            }
            return result;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        Logger.d(LOG_TAG, "onInterceptTouEvent this %s, %b", this, result);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        Logger.d(LOG_TAG, "onTouchEvent this %s, %b", this, result);
        return result;
    }

    public void setUnitManager(UnitManager unitManager) {
        mUnitManager = unitManager;
    }

    public void getViewRectRelativeToSelf(View v, Rect r) {
        int[] loc = new int[2];
        getLocationInWindow(loc);
        int x = loc[0];
        int y = loc[1];

        v.getLocationInWindow(loc);
        int vX = loc[0];
        int vY = loc[1];

        int left = vX - x;
        int top = vY - y;

        r.set(left, top, left + v.getMeasuredWidth(), top + v.getMeasuredHeight());
    }

    int mHotseatHeight;
    public void setHotseatHeight(int hotseatHeight) {
        mHotseatHeight = hotseatHeight;
    }

    public int[] getChildPossibleLocX() {
        int[] result = new int[2];
        result[0] = 0;
        result[1] = getMeasuredWidth();
        return result;
    }

    public int[] getChildPossibleLocY() {
        int[] result = new int[2];
        result[0] = 0;
        result[1] = getMeasuredHeight() - mHotseatHeight;
        return result;
    }


    public void startDrag(BaseUnitView child) {
        mDragHelper.startDrag(child);
    }

    // This is used to compute the visual x,y of the dragView. This point is then
    // used to visualize drop locations and determine where to drop an item.
    //This is modified from Worksapce.java
    private float[] getDragViewLoc(int x, int y, int xOffset, int yOffset, float[] recycle) {
        float res[];
        if (recycle == null) {
            res = new float[2];
        } else {
            res = recycle;
        }

        // First off, the drag view has been shifted in a way that is not represented in the
        // x and y values or the x/yOffsets. Here we account for that shift.
        x += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetX);
        y += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY);

        // These represent the visual top and left of drag view if a dragRect was provided.
        // If a dragRect was not provided, then they correspond to the actual view left and
        // top, as the dragRect is in that case taken to be the entire dragView.
        // R.dimen.dragViewOffsetY.
        int left = x - xOffset;
        int top = y - yOffset;

        res[0] = left;
        res[1] = top;

        return res;
    }
}
