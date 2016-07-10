package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.android.launcher3.Alarm;
import com.android.launcher3.OnAlarmListener;
import com.android.launcher3.util.Logger;

import junit.framework.Assert;

/**
 * Created by NineG on 2016/7/3.
 */
public class PetView extends BaseUnitView implements PetInfo.OnInfoChangedObserver {
    private static final String LOG_TAG = Logger.getLogTag(PetView.class);
    protected PetAnimationInfo mPetAnimationInfo;

    public PetView(Context context) {
        super(context);
    }

    public PetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setUnitInfo(BaseUnitInfo info) {
        clearOnInfoChangedObserver(mInfo);
        super.setUnitInfo(info);

        if (info instanceof PetInfo) {
            PetInfo petInfo = (PetInfo) info;
            registerInfoChangedObserver(petInfo);
        } else {
            Logger.d(LOG_TAG, "invalid info %s", info);
        }
    }

    public void registerInfoChangedObserver(PetInfo petInfo) {
        petInfo.setOnInfoChangedObserver(this);
    }

    public void clearOnInfoChangedObserver(BaseUnitInfo info) {
        if (info instanceof PetInfo) {
            ((PetInfo) info).clearOnInfoChangedObserver();
        } else {
            Logger.d(LOG_TAG, "invalid info %s", info);
        }
    }

    @Override
    public boolean onHungry() {
        return false;
    }

    @Override
    public boolean onEat(final FoodInfo foodInfo) {
        startMovement(true);
        return true;
    }

    @Override
    public int[] getMoveRangeX() {
        if (!(getParent() instanceof ParkViewHost))
            return null;

        int[] result = ((ParkViewHost)getParent()).getChildPossibleLocX();
        result[1] -= mInfo.getCurrentWidth();
        return result;
    }

    @Override
    public int[] getMoveRangeY() {
        if (!(getParent() instanceof ParkViewHost))
            return null;

        int[] result = ((ParkViewHost)getParent()).getChildPossibleLocY();
        result[1] -= mInfo.getCurrentHeight();
        return result;
    }

    Paint mPaint = new Paint();
    Paint debugPaint1;
    Paint debugPaint2;

    @Override
    protected void dispatchDraw(Canvas canvas) {
//        Logger.d(LOG_TAG, "dispatchDraw %s", this);
        if (mPetAnimationInfo != null && mPetAnimationInfo.isReady()) {
//            Logger.d(LOG_TAG, "set timelapsed %s, duration", timelapsed, duration);
            long timeToNextFrame = mPetAnimationInfo.next();
            PetAnimationInfo.InfoSet set = mPetAnimationInfo.getCurrentSet();
//            Logger.d(LOG_TAG, "set bitmap %s/%s", set.bitmap.getWidth(), set.bitmap.getHeight());
            if (mPetAnimationInfo.isHorizontalReverse()) {
                int save = canvas.save();
                canvas.scale(-1f, 1f);
                canvas.translate(-set.bitmap.getWidth(), 0);
                canvas.drawBitmap(set.bitmap, set.left, set.top, mPaint);
                canvas.restoreToCount(save);
            } else {
                canvas.drawBitmap(set.bitmap, set.left, set.top, mPaint);
            }

            if (timeToNextFrame > 0)
                scheduleNextInvalidate(timeToNextFrame);
        }

        super.dispatchDraw(canvas);

        if (debugPaint1 == null) {
            debugPaint1 = new Paint();
            debugPaint1.setColor(0xffcccc00);
        }

        if (getClipBounds() != null) {
            Logger.d(LOG_TAG, "draw clip bound %s", this);
            canvas.drawRect(getClipBounds(), debugPaint1);
        }

        if (debugPaint2 == null) {
            debugPaint2 = new Paint();
            debugPaint2.setColor(0xff00cccc);
        }

        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), debugPaint2);
    }

    private Alarm mAnimateAlarm;

    private void scheduleNextInvalidate(long delay) {
        if (mAnimateAlarm == null) {
            mAnimateAlarm = new Alarm();
            mAnimateAlarm.setOnAlarmListener(new OnAlarmListener() {
                @Override
                public void onAlarm(Alarm alarm) {
                    invalidate();
                }
            });
        }
        mAnimateAlarm.setAlarm(delay);
    }

    private Alarm mMovementAlarm;

    private void scheduleNextMovement(long delay) {
        if (mMovementAlarm == null) {
            mMovementAlarm = new Alarm();
            mMovementAlarm.setOnAlarmListener(new OnAlarmListener() {
                @Override
                public void onAlarm(Alarm alarm) {
                    startMovement();
                }
            });
        }
        mMovementAlarm.setAlarm(delay);
    }

    private Animator mMovementAnimator;
    private boolean isLastMovementAnimatorCanceled = false;
    public void stopMovement() {
        if (mMovementAnimator != null) {
            mMovementAnimator.end();
            mMovementAnimator = null;
        }
        mMovementAlarm.cancelAlarm();
    }

    public void startMovement() {
        startMovement(false);
    }
    public void startMovement(boolean force) {
        Logger.d(LOG_TAG, "startMovement %s", mInfo);

//        if (mMovementAnimator != null && mMovementAnimator.isRunning()) {
//            Thread.dumpStack();
//            Logger.d(LOG_TAG, "startMovement mMovementAnimator.isRunning");
//            return;
//        }

        Assert.assertTrue(mInfo instanceof PetInfo);
        Context context = this.getContext();
        final PetInfo petInfo = (PetInfo) mInfo;

        long remainingTime = petInfo.isMovementChanged(context);
        final PetInfo.Movement preMovement = petInfo.getCurrentMovement(context);

        if (!force && remainingTime > 0) {
            //movement not changed
            scheduleNextMovement(remainingTime);
        } else {
            final PetInfo.Movement curMovement = petInfo.getCurrentMovement(context);

            int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mInfo.getCurrentWidth(), MeasureSpec.EXACTLY);
            int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mInfo.getCurrentHeight(), MeasureSpec.EXACTLY);

            Logger.d(LOG_TAG, "startMovement x %s, tx %s, y %s, ty %s", getX(), getTranslationX(), getY(), getTranslationY());
            Logger.d(LOG_TAG, "startMovement pre left %s, top %s, %s ,%s", getLeft(), getTop(), getLeft() + getTranslationX(), getTop() + getTranslationY());
            measure(newWidthMeasureSpec, newHeightMeasureSpec);
            Logger.d(LOG_TAG, "startMovement layout %s, %s, %s, %s", (int) mInfo.getX(), (int) mInfo.getY(), (int) mInfo.getCurrentWidth(), mInfo.getCurrentHeight());
            setTranslationX(0f);
            setTranslationY(0f);
            layout((int) mInfo.getX(), (int) mInfo.getY(), (int) mInfo.getX() + mInfo.getCurrentWidth(), (int) mInfo.getY() + mInfo.getCurrentHeight());

            Logger.d(LOG_TAG, "startMovement x %s, tx %s, y %s, ty %s", getX(), getTranslationX(), getY(), getTranslationY());
            Logger.d(LOG_TAG, "startMovement post left %s, top %s, %s ,%s", getLeft(), getTop(), getLeft() + getTranslationX(), getTop() + getTranslationY());

            mPetAnimationInfo = curMovement.getPetAnimationInfo();
            if (mMovementAnimator != null) {
                mMovementAnimator.cancel();
            }

            mMovementAnimator = curMovement.getMoveAnimator(this);
            Logger.d(LOG_TAG, "startMovement mPetAnimationInfo %s, mMovementAnimator %s", mPetAnimationInfo, mMovementAnimator);
            if (mPetAnimationInfo != null)
                invalidate();

            if (mMovementAnimator != null) {
                mMovementAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        isLastMovementAnimatorCanceled = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        isLastMovementAnimatorCanceled = true;
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mMovementAnimator = null;
                        if (!isLastMovementAnimatorCanceled) {
                            setX(curMovement.getX());
                            setY(curMovement.getY());
                            scheduleNextMovement(50);
                        }
                        Logger.d(LOG_TAG, "mMovementAnimator end %s, %s, %s", mInfo.getX(), mInfo.getY(), isLastMovementAnimatorCanceled);
                        //a short delay
                    }
                });

                mMovementAnimator.start();
            } else {
                //remain current state for a period of time.
                scheduleNextMovement(curMovement.getStayTime());

                //eat food
                if (preMovement != null && preMovement.state == BaseUnitInfo.State.Eat && petInfo.getFoodInfoToBeEaten() != null) {
                    petInfo.getFoodInfoToBeEaten().beEaten((PetInfo) mInfo, 1);
                    petInfo.eatCompletely();
                }
            }
        }
    }
}
