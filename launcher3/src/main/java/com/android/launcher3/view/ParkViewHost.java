package com.android.launcher3.view;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.DragController;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget;
import com.android.launcher3.Insettable;
import com.android.launcher3.SpringLoadedDragController;
import com.android.launcher3.util.Logger;

/**
 * Created by NineG on 2016/5/15.
 */
public class ParkViewHost extends ViewGroup implements DropTarget, DragSource, View.OnTouchListener,
        DragController.DragListener, ViewGroup.OnHierarchyChangeListener, Insettable {
    private static final String LOG_TAG = Logger.getLogTag(ParkViewHost.class);

    protected final Rect mInsets = new Rect();
    private DragController mDragController;

    public ParkViewHost(Context context) {
        this(context, null);
    }

    public ParkViewHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParkViewHost(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
                int childLeft = lp.x;
                int childTop = lp.y;
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


    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int x, y;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
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

    }

    /**
     * The drag has ended
     */
    @Override
    public void onDragEnd() {

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
    public boolean isDropEnabled() {
        return false;
    }

    /**
     * Handle an object being dropped on the DropTarget
     *
     * @param dragObject
     */
    @Override
    public void onDrop(DragObject dragObject) {

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
        return false;
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {

    }

    @Override
    public void getLocationInDragLayer(int[] loc) {

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

    public void setup(DragController dragController) {
        mDragController = dragController;
    }

    private OnLongClickListener mOnLongClickListener = null;

    public void setCustomizedOnLongClickListener(OnLongClickListener l) {
        mOnLongClickListener = l;
    }

    public boolean addInScreen(BaseUnitView child, BaseUnitInfo info, int x, int y) {
        LayoutParams params = generateDefaultLayoutParams();
        params.x = x;
        params.y = y;
        child.setLayoutParams(params);
        child.setUnitInfo(info);
        child.setOnLongClickListener(mOnLongClickListener);
        addView(child);
        return true;
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

    private UnitManager mUnitManager = new UnitManager();
}
