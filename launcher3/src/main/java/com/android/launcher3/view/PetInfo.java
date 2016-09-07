package com.android.launcher3.view;

import android.animation.Animator;
import android.content.Context;
import android.view.View;

import com.android.launcher3.FirstFrameAnimatorHelper;
import com.android.launcher3.LauncherAnimatorHelper;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.Logger;

import junit.framework.Assert;

/**
 * Created by NineG on 2016/7/3.
 */
public abstract class PetInfo extends BaseUnitInfo {
    private static final String LOG_TAG = Logger.getLogTag(PetInfo.class);
    private FoodInfo mFoodInfoToBeEaten;

    public PetInfo(Context context) {
        super(context);
    }

    public interface PetInfoChangedObserver extends OnInfoChangedObserver {
        boolean onHungry();
        boolean onEat(FoodInfo foodInfo);
        int[] getMoveRangeX();
        int[] getMoveRangeY();
    }


    protected int[] getMoveRangeX() {
        Assert.assertTrue(mInfoChangedObserver instanceof PetInfoChangedObserver);
        return ((PetInfoChangedObserver) mInfoChangedObserver).getMoveRangeX();
    }

    protected int[] getMoveRangeY() {
        Assert.assertTrue(mInfoChangedObserver instanceof PetInfoChangedObserver);
        return ((PetInfoChangedObserver) mInfoChangedObserver).getMoveRangeY();
    }

    boolean hungry(FoodInfo foodInfo, PetInfo petInfo) {
        return true;
    }

    boolean eat(Context context, FoodInfo foodInfo) {
        //playAnimation
        Assert.assertTrue(mInfoChangedObserver instanceof PetInfoChangedObserver);
        boolean result = false;
        if (mFoodInfoToBeEaten != null && foodInfo != null) {
            Double d1 = Utilities.sqareDistance(mFoodInfoToBeEaten.getX(), mFoodInfoToBeEaten.getY(), getX(), getY());
            Double d2 = Utilities.sqareDistance(foodInfo.getX(), foodInfo.getY(), getX(), getY());
            if (d1 > d2) {
                mFoodInfoToBeEaten = foodInfo;
                result = true;
            }
        } else {
            mFoodInfoToBeEaten = foodInfo;
            result = true;
        }

        if (result) {
            setCurMovement(getEatMovement(context, foodInfo));
            ((PetInfoChangedObserver) mInfoChangedObserver).onEat(foodInfo);
        }

        return result;
    }

    public FoodInfo getFoodInfoToBeEaten() {
        return mFoodInfoToBeEaten;
    }

    void eatCompletely() {
        mFoodInfoToBeEaten = null;
    }

//    abstract PetAnimationInfo getPetAnimationInfo(Context context);
    abstract boolean interest(BaseUnitInfo info);
    abstract protected Movement getNextMovement(Context context);
    abstract protected Movement getEatMovement(Context context, FoodInfo info);

    @Override
    public BaseUnitView generateView(Context context) {
        return new PetView(context);
    }

    @Override
    public boolean notifyNewInfo(Context context, BaseUnitInfo newInfo) {
        if (interest(newInfo)) {
            if (newInfo instanceof FoodInfo) {
                return eat(context, (FoodInfo) newInfo);
            }
        }

        return false;
    }

    @Override
    public boolean notifyInfoChanged(Context context, BaseUnitInfo infoChanged) {
        if (infoChanged.equals(mFoodInfoToBeEaten)) {
            mFoodInfoToBeEaten = null;
            return eat(context, (FoodInfo) infoChanged);
        }
        return false;
    }

    protected Movement mCurMovement = null;

    Movement getCurrentMovement(Context context) {
        if (mCurMovement == null)
            setCurMovement(getNextMovement(context));
        return  mCurMovement;
    }

    class Movement {
        State state;
        float startX, startY;
        float targetX, targetY;
        int weight;
        long stayTime;
        LauncherAnimatorHelper launcherAnimatorHelper;

        PetAnimationInfo selfAnimation;
        Movement nextMovement;

        Movement(State state, int stayTime, PetAnimationInfo selfAnimation) {
            this.state = state;
            this.stayTime = stayTime;
            this.selfAnimation = selfAnimation;
        }

        Movement(State state, int weight) {
            this.state = state;
            this.weight = weight;
        }

        Movement(State state, float startX, float startY, float nextX, float nextY, int stayTime, PetAnimationInfo selfAnimation, LauncherAnimatorHelper moveAnimator) {
            this.state = state;
            this.startX = startX;
            this.startY = startY;
            this.targetX = nextX;
            this.targetY = nextY;

            this.stayTime = stayTime;
            this.selfAnimation = selfAnimation;
            this.launcherAnimatorHelper = moveAnimator;
        }

        public float getStartX() {
            return startX;
        }


        public float getStartY() {
            return startY;
        }

        public float getTargetX() {
            return targetX;
        }

        public float getTargetY() {
            return targetY;
        }

        public Animator getMoveAnimator(View targetView) {
            if (launcherAnimatorHelper != null) {
                Animator animator = launcherAnimatorHelper.getAnimator();
                FirstFrameAnimatorHelper firstFrameAnimatorHelper = launcherAnimatorHelper.getFirstFrameAnimatorHelper();
                animator.setTarget(targetView);
                firstFrameAnimatorHelper.setTarget(targetView);
                return animator;
            }
            return null;
        }

        public PetAnimationInfo getPetAnimationInfo() {
            return selfAnimation;
        }

        public long getStayTime() {
            return stayTime;
        }

        public Movement getNextMovement () {
            Logger.d(LOG_TAG, "Movement getNextMovement %s, %s", state, nextMovement);
            return nextMovement;
        }

        @Override
        public String toString() {
            return super.toString() + "#state: " + state;
        }
    }

    long mLastMovementTime;

    /***
     *
     * @return the remaining to next movement.
     * */
    public long isMovementChanged(Context context) {
        Logger.d(LOG_TAG, "isMovementChanged %s", mCurMovement);

        if (mCurMovement == null)
            return 0;

        long timeDiff = System.currentTimeMillis() - mLastMovementTime;

        Logger.d(LOG_TAG, "isMovementChanged timeDiff %s, %s", timeDiff, mCurMovement.getStayTime());

        if (timeDiff >= mCurMovement.getStayTime()) {
            //movement changed
            Movement nextMovement = mCurMovement.getNextMovement();
            setCurMovement(nextMovement == null ? getNextMovement(context) : nextMovement);
            return 0;
        }

        return mCurMovement.getStayTime() - timeDiff;
    }


    protected void setCurMovement(Movement newMovement) {
        mCurMovement = newMovement;
        setState(mCurMovement.state);
        mLastMovementTime = System.currentTimeMillis();
    }

    Movement refreshMovement(Context context) {
        return getCurrentMovement(context);
    }
}
