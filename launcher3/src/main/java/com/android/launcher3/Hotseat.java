/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.launcher3.util.Logger;
import com.android.launcher3.view.FoodInfo;

import java.util.ArrayList;
import java.util.List;

public class Hotseat extends FrameLayout {
    private static final String LOG_TAG = Logger.getLogTag(Hotseat.class);
    private static final int DURATION_ACCEPT_DROP = 200;
    private static final int DURATION_RETURN_NEUTRAL = 200;
    private static final int DURATION_DROP_IN_CANNON = 400;

    private static final int DURATION_FIRE_CANNON_SCALE_Y = 120;
    private static final int DURATION_FIRE_CANNON_SCALE_Y_BACK = 180;
    private static final int DURATION_FIRE_CANNON_SCALE_X = 120;
    private static final int DURATION_FIRE_CANNON_SCALE_X_BACK = 180;

    private static final int DURATION_FIRE_CANNON_SCALE_CHARGE = 600;
    private static final int DURATION_FIRE_CANNON_SCALE_SHOOT = 100;
    private static final int DURATION_FIRE_CANNON_SCALE_RECOVERY = 200;

    public static final float DURATION_FRACTION_TO_NOTIFY_FOOD = 0.5f;

    private static final int CONFIG_FIRE_CANNON_SHIFT = 30;
    public static final float CONFIG_FIRE_CANNON_SCALE_SHOOT = 1.8f;

    private CellLayout mContent;

    private Launcher mLauncher;

    private int mAllAppsButtonRank;

    private boolean mTransposeLayoutWithOrientation;
    private boolean mIsLandscape;

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources r = context.getResources();
        mTransposeLayoutWithOrientation = 
                r.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        mIsLandscape = context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
    }

    CellLayout getLayout() {
        return mContent;
    }

    /**
     * Registers the specified listener on the cell layout of the hotseat.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mContent.setOnLongClickListener(l);
    }
  
    private boolean hasVerticalHotseat() {
        return (mIsLandscape && mTransposeLayoutWithOrientation);
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return hasVerticalHotseat() ? (mContent.getCountY() - y - 1) : x;
    }
    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return hasVerticalHotseat() ? 0 : rank;
    }
    int getCellYFromOrder(int rank) {
        return hasVerticalHotseat() ? (mContent.getCountY() - (rank + 1)) : 0;
    }
    public boolean isAllAppsButtonRank(int rank) {
        if (LauncherAppState.isDisableAllApps()) {
            return false;
        } else {
            return rank == mAllAppsButtonRank;
        }
    }

    /** This returns the coordinates of an app in a given cell, relative to the DragLayer */
    Rect getCellCoordinates(int cellX, int cellY) {
        Rect coords = new Rect();
        mContent.cellToRect(cellX, cellY, 1, 1, coords);
        int[] hotseatInParent = new int[2];
        Utilities.getDescendantCoordRelativeToParent(this, mLauncher.getDragLayer(),
                hotseatInParent, false);
        coords.offset(hotseatInParent[0], hotseatInParent[1]);

        // Center the icon
        int cWidth = mContent.getShortcutsAndWidgets().getCellContentWidth();
        int cHeight = mContent.getShortcutsAndWidgets().getCellContentHeight();
        int cellPaddingX = (int) Math.max(0, ((coords.width() - cWidth) / 2f));
        int cellPaddingY = (int) Math.max(0, ((coords.height() - cHeight) / 2f));
        coords.offset(cellPaddingX, cellPaddingY);

        return coords;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

        mAllAppsButtonRank = grid.hotseatAllAppsRank;
        mContent = (CellLayout) findViewById(R.id.layout);
        if (grid.isLandscape && !grid.isLargeTablet()) {
            mContent.setGridSize(1, (int) grid.numHotseatIcons);
        } else {
            mContent.setGridSize((int) grid.numHotseatIcons, 1);
        }
        mContent.setIsHotseat(true);

        resetLayout();
    }

    void resetLayout() {
        mContent.removeAllViewsInLayout();

        if (!LauncherAppState.isDisableAllApps()) {
            // Add the Apps button
            Context context = getContext();

            LayoutInflater inflater = LayoutInflater.from(context);
            TextView allAppsButton = (TextView)
                    inflater.inflate(R.layout.all_apps_button, mContent, false);

            prepareAllAppsButtonAsset(context);

            allAppsButton.setCompoundDrawables(null, mAllAppsDrawable, null, null);

            allAppsButton.setContentDescription(context.getString(R.string.all_apps_button_label));
            allAppsButton.setOnKeyListener(new HotseatIconKeyEventListener());
            if (mLauncher != null) {
                allAppsButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());
                mLauncher.setAllAppsButton(allAppsButton);
                allAppsButton.setOnClickListener(mLauncher);
                allAppsButton.setOnFocusChangeListener(mLauncher.mFocusHandler);
                mAllAppsButtonAnimator = new AllAppsButtonAnimator(allAppsButton);
            }

            // Note: We do this to ensure that the hotseat is always laid out in the orientation of
            // the hotseat in order regardless of which orientation they were added
            int x = getCellXFromOrder(mAllAppsButtonRank);
            int y = getCellYFromOrder(mAllAppsButtonRank);
            CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x,y,1,1);
            lp.canReorder = false;
            lp.isLockedToGrid = true;
            mContent.addViewToCellLayout(allAppsButton, -1, allAppsButton.getId(), lp, true);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // We don't want any clicks to go through to the hotseat unless the workspace is in
        // the normal state.
        if (mLauncher.getWorkspace().workspaceInModalState()) {
            return true;
        }
        return false;
    }

    void addAllAppsFolder(IconCache iconCache,
            ArrayList<AppInfo> allApps, ArrayList<ComponentName> onWorkspace,
            Launcher launcher, Workspace workspace) {
        if (LauncherAppState.isDisableAllApps()) {
            FolderInfo fi = new FolderInfo();

            fi.cellX = getCellXFromOrder(mAllAppsButtonRank);
            fi.cellY = getCellYFromOrder(mAllAppsButtonRank);
            fi.spanX = 1;
            fi.spanY = 1;
            fi.container = LauncherSettings.Favorites.CONTAINER_HOTSEAT;
            fi.screenId = mAllAppsButtonRank;
            fi.itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
            fi.title = "More Apps";
            LauncherModel.addItemToDatabase(launcher, fi, fi.container, fi.screenId, fi.cellX,
                    fi.cellY, false);
            FolderIcon folder = FolderIcon.fromXml(R.layout.folder_icon, launcher,
                    getLayout(), fi, iconCache);
            workspace.addInScreen(folder, fi.container, fi.screenId, fi.cellX, fi.cellY,
                    fi.spanX, fi.spanY);

            for (AppInfo info: allApps) {
                ComponentName cn = info.intent.getComponent();
                if (!onWorkspace.contains(cn)) {
                    Log.d(LOG_TAG, "Adding to 'more apps': " + info.intent);
                    ShortcutInfo si = info.makeShortcut();
                    fi.add(si);
                }
            }
        }
    }

    void addAppsToAllAppsFolder(ArrayList<AppInfo> apps) {
        if (LauncherAppState.isDisableAllApps()) {
            View v = mContent.getChildAt(getCellXFromOrder(mAllAppsButtonRank), getCellYFromOrder(mAllAppsButtonRank));
            FolderIcon fi = null;

            if (v instanceof FolderIcon) {
                fi = (FolderIcon) v;
            } else {
                return;
            }

            FolderInfo info = fi.getFolderInfo();
            for (AppInfo a: apps) {
                ShortcutInfo si = a.makeShortcut();
                info.add(si);
            }
        }
    }

    private Drawable mCannonDrawable;
    private Drawable mAllAppsDrawable;

    private void prepareAllAppsButtonAsset(Context context) {
        mCannonDrawable = context.getResources().getDrawable(R.drawable.cannon168);
        Utilities.resizeIconDrawable(mCannonDrawable);

        mAllAppsDrawable = context.getResources().getDrawable(R.drawable.all_apps_button_icon);
        Utilities.resizeIconDrawable(mAllAppsDrawable);
    }

    private boolean mNeedToDelayChangeToAllapps;
    public void onDragStart(final DragSource source, Object info, int dragAction) {
        mNeedToDelayChangeToAllapps = false;
        changeToCannon();
    }

    public void onDragEnd() {
        if (!mNeedToDelayChangeToAllapps)
            changeToAllapps();
    }

    private void changeToCannon() {
        View allappsbutton = mLauncher.getAllAppsButton();
        if (allappsbutton instanceof TextView) {
            Logger.d(LOG_TAG, "hotseat onDragStart");
            ((TextView) allappsbutton).setCompoundDrawables(null, mCannonDrawable, null, null);
        }
    }

    private void changeToAllapps() {
        View allappsbutton = mLauncher.getAllAppsButton();
        if (allappsbutton instanceof TextView) {
            Logger.d(LOG_TAG, "hotseat onDragEnd");
            ((TextView) allappsbutton).setCompoundDrawables(null, mAllAppsDrawable, null, null);
        }
    }

    public void onDragEnterAllAppsButton(ItemInfo info) {
        if (!acceptDropIntoCannon(info))
            return;

        if (mAllAppsButtonAnimator != null)
            mAllAppsButtonAnimator.startAccept();
    }

    public void onDragExitAllAppsButton() {
        if (mAllAppsButtonAnimator != null)
            mAllAppsButtonAnimator.startNeutral();
    }

    private AllAppsButtonAnimator mAllAppsButtonAnimator;

    private class AllAppsButtonAnimator {
        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;
        private AnimatorSet mFireAnimatorSet;
        private List<Animator> mFireAnimatorList;
        ItemInfo mItemInfoToFire;
        Runnable mActionAfterFireAnimation;

        private boolean hasNotifyShoot = false;
        AllAppsButtonAnimator(View allappsButton) {
            mAcceptAnimator = LauncherAnimUtils.ofPropertyValuesHolder(allappsButton,
                    PropertyValuesHolder.ofFloat("scaleX", 1.3f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.3f));

            mAcceptAnimator.setDuration(DURATION_ACCEPT_DROP);

            mNeutralAnimator = LauncherAnimUtils.ofPropertyValuesHolder(allappsButton,
                    PropertyValuesHolder.ofFloat("scaleX", allappsButton.getScaleX()),
                    PropertyValuesHolder.ofFloat("scaleY", allappsButton.getScaleY()));

            mNeutralAnimator.setDuration(DURATION_RETURN_NEUTRAL);

            mFireAnimatorSet = LauncherAnimUtils.createAnimatorSet();
            float initTranslationY = allappsButton.getTranslationY();
            ValueAnimator scaleYAnimator = LauncherAnimUtils.ofFloat(allappsButton, "scaleY", 1.3f);
            ValueAnimator scaleYBackAnimator = LauncherAnimUtils.ofFloat(allappsButton, "scaleY", 1f);
            ValueAnimator scaleXAnimator = LauncherAnimUtils.ofFloat(allappsButton, "scaleX", 1.3f);
            ValueAnimator scaleXBackAnimator = LauncherAnimUtils.ofFloat(allappsButton, "scaleX", 1f);

            ValueAnimator chargeAnimator = LauncherAnimUtils.ofPropertyValuesHolder(allappsButton,
                    PropertyValuesHolder.ofFloat("scaleX", 1.3f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.5f),
                    PropertyValuesHolder.ofFloat("translationY", initTranslationY + 1.5f * Utilities.pxFromDp(CONFIG_FIRE_CANNON_SHIFT, allappsButton.getResources().getDisplayMetrics())));


            ValueAnimator shootAnimator = LauncherAnimUtils.ofPropertyValuesHolder(allappsButton,
                    PropertyValuesHolder.ofFloat("scaleX", 0.5f),
                    PropertyValuesHolder.ofFloat("scaleY", CONFIG_FIRE_CANNON_SCALE_SHOOT),
                    PropertyValuesHolder.ofFloat("translationY", initTranslationY  - Utilities.pxFromDp(CONFIG_FIRE_CANNON_SHIFT, allappsButton.getResources().getDisplayMetrics())));

            ValueAnimator recoveryAnimator = LauncherAnimUtils.ofPropertyValuesHolder(allappsButton,
                    PropertyValuesHolder.ofFloat("scaleX", allappsButton.getScaleX()),
                    PropertyValuesHolder.ofFloat("scaleY", allappsButton.getScaleY()),
                    PropertyValuesHolder.ofFloat("translationY", initTranslationY));


            scaleYAnimator.setDuration(DURATION_FIRE_CANNON_SCALE_Y);
            scaleYAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    Logger.d(LOG_TAG, "hotseat scaleYAnimator start");
                    hasNotifyShoot = false;
                }
            });

            scaleYBackAnimator.setDuration(DURATION_FIRE_CANNON_SCALE_Y_BACK);
            scaleYBackAnimator.setStartDelay(DURATION_FIRE_CANNON_SCALE_Y);

            scaleXAnimator.setDuration(DURATION_FIRE_CANNON_SCALE_X);
            scaleXAnimator.setStartDelay(DURATION_FIRE_CANNON_SCALE_Y);

            scaleXBackAnimator.setDuration(DURATION_FIRE_CANNON_SCALE_X_BACK);
            scaleXBackAnimator.setStartDelay(DURATION_FIRE_CANNON_SCALE_Y + DURATION_FIRE_CANNON_SCALE_X);

            chargeAnimator.setDuration(DURATION_FIRE_CANNON_SCALE_CHARGE);
            chargeAnimator.setStartDelay(DURATION_FIRE_CANNON_SCALE_Y + DURATION_FIRE_CANNON_SCALE_X + DURATION_FIRE_CANNON_SCALE_X_BACK);

            shootAnimator.setDuration(DURATION_FIRE_CANNON_SCALE_SHOOT);
            shootAnimator.setStartDelay(DURATION_FIRE_CANNON_SCALE_Y + DURATION_FIRE_CANNON_SCALE_X + DURATION_FIRE_CANNON_SCALE_X_BACK + DURATION_FIRE_CANNON_SCALE_CHARGE);
            shootAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (!hasNotifyShoot && animation.getAnimatedFraction() > DURATION_FRACTION_TO_NOTIFY_FOOD) {
                        notifyToSupplyFood();
                        hasNotifyShoot = true;
                    }
                }
            });

            recoveryAnimator.setDuration(DURATION_FIRE_CANNON_SCALE_RECOVERY);
            recoveryAnimator.setStartDelay(DURATION_FIRE_CANNON_SCALE_Y + DURATION_FIRE_CANNON_SCALE_Y_BACK + DURATION_FIRE_CANNON_SCALE_X + DURATION_FIRE_CANNON_SCALE_X_BACK + DURATION_FIRE_CANNON_SCALE_CHARGE + DURATION_FIRE_CANNON_SCALE_SHOOT);
            recoveryAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    notifyFireAnimationEnd();
                }
            });

            mFireAnimatorSet.play(scaleYAnimator);
            mFireAnimatorSet.play(scaleYBackAnimator);
            mFireAnimatorSet.play(scaleXAnimator);
            mFireAnimatorSet.play(scaleXBackAnimator);
            mFireAnimatorSet.play(chargeAnimator);
            mFireAnimatorSet.play(shootAnimator);
            mFireAnimatorSet.play(recoveryAnimator);

        }

        public void startAccept() {
            Logger.d(LOG_TAG, "hotseat startAccept");
            if (mNeutralAnimator.isRunning())
                mNeutralAnimator.cancel();

            if (!mAcceptAnimator.isRunning())
                mAcceptAnimator.start();
        }

        public void startNeutral() {
            Logger.d(LOG_TAG, "hotseat startNeutral");
            if (mAcceptAnimator.isRunning())
                mAcceptAnimator.end();

            if (!mNeutralAnimator.isRunning())
                mNeutralAnimator.start();
        }

        public void prepareFire(ItemInfo itemInfo, final Runnable actionAfterFireAnimation) {
            Logger.d(LOG_TAG, "hotseat prepareFire");
            mItemInfoToFire = itemInfo;
            mActionAfterFireAnimation = actionAfterFireAnimation;
            //reparent to get enough space for animation
            View allappsbutton = mLauncher.getAllAppsButton();
            mLastLayoutParamsOfAllappsbutton = (CellLayout.LayoutParams) allappsbutton.getLayoutParams();
            mDragLayerLayoutParamsOfAllappsbutton = getDragLayerLayoutParams(allappsbutton);
            allappsbutton.setLayoutParams(mDragLayerLayoutParamsOfAllappsbutton);
            mLauncher.reparentToFireCannon(allappsbutton);

            mFireAnimatorSet.start();
        }


        public void notifyToSupplyFood() {
            Logger.d(LOG_TAG, "hotseat notifyToSupplyFood");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    changeToAllapps();
                    if (mActionAfterFireAnimation != null)
                        mActionAfterFireAnimation.run();
                }
            };
            mLauncher.fireToParkView(mLauncher, mItemInfoToFire, runnable);
        }

        private void notifyFireAnimationEnd() {
            mIsDuringDropAndFireAnimation = false;

            View allappsbutton = mLauncher.getAllAppsButton();
            allappsbutton.setLayoutParams(mLastLayoutParamsOfAllappsbutton);
            ((ViewGroup) allappsbutton.getParent()).removeView(allappsbutton);
            mContent.addViewToCellLayout(allappsbutton, -1, allappsbutton.getId(), mLastLayoutParamsOfAllappsbutton, true);

        }
    }

    private CellLayout.LayoutParams mLastLayoutParamsOfAllappsbutton;
    private DragLayer.LayoutParams mDragLayerLayoutParamsOfAllappsbutton;

    private DragLayer.LayoutParams getDragLayerLayoutParams(View view) {
        int[] loc = new int[2];
        mLauncher.getDragLayer().getLocationInDragLayer(view, loc);
        DragLayer.LayoutParams dlp = new DragLayer.LayoutParams(view.getLayoutParams());
        dlp.setX(loc[0]);
        dlp.setY(loc[1]);
        dlp.customPosition = true;
        return dlp;
    }

    /***prepare accept drop animation**/
    private void onDrop(DragView animateView, View targetView, float scaleRelativeToDragLayer, final ItemInfo itemInfo, final Runnable postAnimationRunnable, final Runnable actionAfterFireAnimation) {

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        mNeedToDelayChangeToAllapps = true;
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = new Rect();

            Workspace workspace = mLauncher.getWorkspace();
            // Set cellLayout and this to it's final state to compute final animation locations
            workspace.setFinalTransitionTransform(getLayout());
            float scaleX = getScaleX();
            float scaleY = getScaleY();
            setScaleX(1.0f);
            setScaleY(1.0f);
            scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(targetView, to);
            // Finished computing final animation locations, restore current state
            setScaleX(scaleX);
            setScaleY(scaleY);
            workspace.resetTransitionTransform(getLayout());

            //make sure the center of animatedview animate to the center of targetView
            to.offset(to.width() / 2 - animateView.getMeasuredWidth() / 2, to.height() / 2 - animateView.getMeasuredHeight() / 2);

            float finalAlpha = 0f;
            float finalScale = 0f;
            Runnable actionAfterDropInAnimation = new Runnable() {
                @Override
                public void run() {
                    if (postAnimationRunnable != null)
                        postAnimationRunnable.run();
                    prefireCannonBall(itemInfo, actionAfterFireAnimation);
                }
            };
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DURATION_DROP_IN_CANNON,
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    actionAfterDropInAnimation, DragLayer.ANIMATION_END_DISAPPEAR, null);
        } else {
            prefireCannonBall(itemInfo, actionAfterFireAnimation);
        }
    }

    private void prefireCannonBall(ItemInfo itemInfo, final Runnable actionAfterFireAnimation) {
        mAllAppsButtonAnimator.prepareFire(itemInfo, actionAfterFireAnimation);
//        if (actionAfterFireAnimation != null)
//            actionAfterFireAnimation.run();
    }

    private void preparefire() {

    }

    public boolean acceptDropIntoCannon(ItemInfo item) {
        final int itemType = item.itemType;
        return !mIsDuringDropAndFireAnimation && (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);
    }

    public void dropIntoCannon(DropTarget.DragObject d, final Runnable actionAfterFireAnimation) {
        if (mIsDuringDropAndFireAnimation)
            return;

        mIsDuringDropAndFireAnimation = true;
        onDrop(d.dragView, mLauncher.getAllAppsButton(), 1.0f, (ItemInfo) d.dragInfo, d.postAnimationRunnable, actionAfterFireAnimation);
    }

    private boolean mIsDuringDropAndFireAnimation;
    /***********************/
}
