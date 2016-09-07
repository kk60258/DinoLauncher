package com.android.launcher3.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DragController;
import com.android.launcher3.DragSource;
import com.android.launcher3.DragView;
import com.android.launcher3.FolderIcon;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.ShortcutAndWidgetContainer;
import com.android.launcher3.Workspace;
import com.android.launcher3.util.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by NineG on 2016/7/31.
 */
public class BaseUnitViewDragHelper {
    private static final String LOG_TAG = Logger.getLogTag(ParkViewHost.class);
    private Launcher mLauncher;
    private DragController mDragController;
    private DragSource mDragSource;

    private static final Rect sTempRect = new Rect();
    private final int[] mTempXY = new int[2];
    private final Canvas mCanvas = new Canvas();

    public BaseUnitViewDragHelper(Launcher launcher, DragController dragController, DragSource dragSource) {
        mLauncher = launcher;
        mDragController = dragController;
        mDragSource = dragSource;
    }

    void startDrag(BaseUnitView child) {

        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }

        BaseUnitInfo baseUnitInfo = child.getUnitInfo();
        baseUnitInfo.onDragStart();

        beginDragShared(child, baseUnitInfo, mDragSource);
    }

    public void beginDragShared(View child, BaseUnitInfo baseUnitInfo, DragSource source) {
        child.clearFocus();
        child.setPressed(false);

        mLauncher.onDragStarted(child);
        // The drag bitmap follows the touch point around on the screen
        AtomicInteger padding = new AtomicInteger(Workspace.DRAG_BITMAP_PADDING);
        final Bitmap b = createDragBitmap(child, padding);

        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();

        float scale = mLauncher.getDragLayer().getLocationInDragLayer(child, mTempXY);
        int dragLayerX = Math.round(mTempXY[0] - (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY = Math.round(mTempXY[1] - (bmpHeight - scale * bmpHeight) / 2
                - padding.get() / 2);
//        Logger.d(LOG_TAG, "beginDragShared %s %s %s", info, x, y);
//        LauncherAppState app = LauncherAppState.getInstance();
//        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        Point dragVisualizeOffset = null;
        Rect dragRect = null;
//        if (child instanceof BubbleTextView) {
//            int iconSize = grid.iconSizePx;
//            int top = child.getPaddingTop();
//            int left = (bmpWidth - iconSize) / 2;
//            int right = left + iconSize;
//            int bottom = top + iconSize;
//            dragLayerY += top;
//            // Note: The drag region is used to calculate drag layer offsets, but the
//            // dragVisualizeOffset in addition to the dragRect (the size) to position the outline.
//            dragVisualizeOffset = new Point(-padding.get() / 2, padding.get() / 2);
//            dragRect = new Rect(left, top, right, bottom);
//        } else if (child instanceof FolderIcon) {
//            int previewSize = grid.folderIconSizePx;
//            dragRect = new Rect(0, child.getPaddingTop(), child.getWidth(), previewSize);
//        }
        dragRect = new Rect(0, 0, child.getWidth(), child.getHeight());

        // Clear the pressed state if necessary
//        if (child instanceof BaseUnitView) {
//            BaseUnitView icon = (BaseUnitView) child;
//            icon.clearPressedBackground();
//        }

        if (child.getTag() == null || !(child.getTag() instanceof BaseUnitInfo)) {
            String msg = "Drag started with a view that has no tag set. This "
                    + "will cause a BaseUnitInfo crash. "
                    + "View: " + child + "  tag: " + child.getTag();
            throw new IllegalStateException(msg);
        }

        DragView dv = mDragController.startDrag(b, dragLayerX, dragLayerY, source, child.getTag(),
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, dragRect, scale);
        dv.setIntrinsicIconScaleFactor(source.getIntrinsicIconScaleFactor());

//        if (child.getParent() instanceof ShortcutAndWidgetContainer) {
//            mDragSourceInternal = (ShortcutAndWidgetContainer) child.getParent();
//        }

        b.recycle();
    }

    /**
     * Returns a new bitmap to show when the given View is being dragged around.
     * Responsibility for the bitmap is transferred to the caller.
     * @param expectedPadding padding to add to the drag view. If a different padding was used
     * its value will be changed
     */
    public Bitmap createDragBitmap(View v, AtomicInteger expectedPadding) {
        Bitmap b;

        int padding = expectedPadding.get();
        if (v instanceof TextView) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            Rect bounds = getDrawableBounds(d);
            b = Bitmap.createBitmap(bounds.width() + padding,
                    bounds.height() + padding, Bitmap.Config.ARGB_8888);
            expectedPadding.set(padding - bounds.left - bounds.top);
        } else {
            b = Bitmap.createBitmap(
                    v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
        }

        mCanvas.setBitmap(b);
        drawDragView(v, mCanvas, padding);
        mCanvas.setBitmap(null);

        return b;
    }

    private static Rect getDrawableBounds(Drawable d) {
        Rect bounds = new Rect();
        d.copyBounds(bounds);
        if (bounds.width() == 0 || bounds.height() == 0) {
            bounds.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        } else {
            bounds.offsetTo(0, 0);
        }
        return bounds;
    }

    /**
     * Draw the View v into the given Canvas.
     *
     * @param v the view to draw
     * @param destCanvas the canvas to draw on
     * @param padding the horizontal and vertical padding to use when drawing
     */
    private static void drawDragView(View v, Canvas destCanvas, int padding) {
        final Rect clipRect = sTempRect;
        v.getDrawingRect(clipRect);

        boolean textVisible = false;

        destCanvas.save();
        if (v instanceof TextView) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            Rect bounds = getDrawableBounds(d);
            clipRect.set(0, 0, bounds.width() + padding, bounds.height() + padding);
            destCanvas.translate(padding / 2 - bounds.left, padding / 2 - bounds.top);
            d.draw(destCanvas);
        } else {
            if (v instanceof FolderIcon) {
                // For FolderIcons the text can bleed into the icon area, and so we need to
                // hide the text completely (which can't be achieved by clipping).
                if (((FolderIcon) v).getTextVisible()) {
                    ((FolderIcon) v).setTextVisible(false);
                    textVisible = true;
                }
            }
            destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
            destCanvas.clipRect(clipRect, Region.Op.REPLACE);
            v.draw(destCanvas);

            // Restore text visibility of FolderIcon if necessary
            if (textVisible) {
                ((FolderIcon) v).setTextVisible(true);
            }
        }
        destCanvas.restore();
    }
}
