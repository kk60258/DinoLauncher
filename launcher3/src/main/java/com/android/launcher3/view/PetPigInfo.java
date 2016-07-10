package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;

import com.android.launcher3.LauncherAnimUtils;
import com.android.launcher3.LauncherAnimatorHelper;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.android.launcher3.view.BaseUnitInfo.State.Idle;

/**
 * Created by NineG on 2016/7/3.
 */
public class PetPigInfo extends PetInfo {
    private static final String LOG_TAG = Logger.getLogTag(PetPigInfo.class);

    public PetPigInfo(Context context) {
        super(context);
    }

    @Override
    protected void initSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mSizeMap.clear();
//        mSizeMap.put(State.Idle, new Size(208, 159));
        mSizeMap.put(State.Idle, new Size(pxFromDp(104, dm), pxFromDp(79.5f, dm)));
        mSizeMap.put(State.Move, new Size(pxFromDp(104, dm), pxFromDp(79.5f, dm)));
//        mSizeMap.put(State.Eat, new Size(128, 162));
        mSizeMap.put(State.Eat, new Size(pxFromDp(64, dm), pxFromDp(81, dm)));
//        mSizeMap.put(State.Sleep, new Size(228, 140));
        mSizeMap.put(State.Sleep, new Size(pxFromDp(114, dm), pxFromDp(70, dm)));

        initMovement();
    }

    @Override
    protected void onStateChanged(State oldState, State newState) {

    }

    PetAnimationInfo getPetAnimationInfo(Context context, State state) {
        PetAnimationInfo info = null;
        switch(state) {
            case Idle:
                    if (mPetAnimationInfo_Idle == null) {
                        mPetAnimationInfo_Idle = new PetAnimationInfo(
                            new PetAnimationInfo.InfoSet(((BitmapDrawable) context.getResources().getDrawable(R.drawable.pet_pig_front)).getBitmap(), -1)
                        );
                    }

                    info = mPetAnimationInfo_Idle;
                break;
            case Move:
                if (mPetAnimationInfo_Move == null) {
                    mPetAnimationInfo_Move = new PetAnimationInfo(
                            new PetAnimationInfo.InfoSet(((BitmapDrawable) context.getResources().getDrawable(R.drawable.pet_pig_walk1)).getBitmap(), 1000),
                            new PetAnimationInfo.InfoSet(((BitmapDrawable) context.getResources().getDrawable(R.drawable.pet_pig_walk2)).getBitmap(), 1000)
                    );
                }

                info = mPetAnimationInfo_Move;
                break;
            case Sleep:
                if (mPetAnimationInfo_Sleep == null) {
                    mPetAnimationInfo_Sleep = new PetAnimationInfo(
                            new PetAnimationInfo.InfoSet(((BitmapDrawable) context.getResources().getDrawable(R.drawable.pet_pig_sleep1)).getBitmap(), -1)
                    );
                }

                info = mPetAnimationInfo_Sleep;
                break;
            case Eat:
                if (mPetAnimationInfo_Eat == null) {
                    mPetAnimationInfo_Eat = new PetAnimationInfo(
                            new PetAnimationInfo.InfoSet(((BitmapDrawable) context.getResources().getDrawable(R.drawable.pet_pig_tail1)).getBitmap(), 300),
                            new PetAnimationInfo.InfoSet(((BitmapDrawable) context.getResources().getDrawable(R.drawable.pet_pig_tail2)).getBitmap(), 300)
                    );
                }

                info = mPetAnimationInfo_Eat;
                break;
        }

        return info;
    }

    private PetAnimationInfo mPetAnimationInfo_Idle;
    private PetAnimationInfo mPetAnimationInfo_Move;
    private PetAnimationInfo mPetAnimationInfo_Sleep;
    private PetAnimationInfo mPetAnimationInfo_Eat;

    @Override
    boolean interest(BaseUnitInfo info) {
        return true;
    }

    @Override
    protected Movement getNextMovement(Context context) {
        return getRandomMovement(context);
    }

    protected Movement getMoveMovement(Context context) {
        int[] rangeX = getMoveRangeX();
        int[] rangeY = getMoveRangeY();

        if (rangeX == null || rangeY == null)
            return null;
        int targetX = randomNumber(rangeX[0], rangeX[1]);
        int targetY = randomNumber(rangeY[0], rangeY[1]);

        return getMoveMovement(context, targetX, targetY);
    }

    protected Movement getMoveMovement(Context context, float targetX, float targetY) {
        boolean reverseHorizontal = targetX > getX() ? true : false;
        PetAnimationInfo selfAnimationInfo = getPetAnimationInfo(context, State.Move);
        selfAnimationInfo.setHorizontalReverse(reverseHorizontal);

        LauncherAnimatorHelper moveAnimator = LauncherAnimUtils.ofLauncherAnimatorHelperPropertyValuesHolder(null, PropertyValuesHolder.ofFloat("x", targetX), PropertyValuesHolder.ofFloat("y", targetY));
        moveAnimator.getAnimator().setInterpolator(new LinearInterpolator());

        long duration = calculateMoveDuration(targetX, targetY);
        moveAnimator.getAnimator().setDuration(duration);

        Movement result = new Movement(State.Move, targetX, targetY, (int) duration, selfAnimationInfo, moveAnimator);
        return result;
    }

    @Override
    protected Movement getEatMovement(Context context, FoodInfo info) {
        Movement move = getMoveMovement(context, info.getX(), info.getY());

        PetAnimationInfo selfAnimationInfo = getPetAnimationInfo(context, State.Eat);
        Movement eat = new Movement(State.Eat, getEatMovementDuration(info), selfAnimationInfo);
        move.nextMovement = eat;

        return move;
    }

    Random mRandom = new Random();
    private ArrayList<Movement> mMovementList;
    private int mTotalMovementWeight;

    private Movement getRandomMovement(Context context) {
        Movement movement = getRandomMovementByWeight();
        Movement result = null;
        switch (movement.state) {
            case Idle:
                result = new Movement(State.Idle, randomIdleStaytime(), getPetAnimationInfo(context, State.Idle));
                break;
            case Move:
                result = getMoveMovement(context);
                break;
            case Sleep:
                result = new Movement(State.Sleep, randomSleepStaytime(), getPetAnimationInfo(context, State.Sleep));
                break;
        }
        Logger.d(LOG_TAG, "getRandomMovement %s", result.state);
        return result;
    }

    private long calculateMoveDuration(float targetX, float targetY) {
        float distance = Utilities.distance(targetX, targetY, x, y);
        long duration = (long) (distance / getCurrentSpeed());
        return duration;
    }

    private int randomNumber(int base, int max) {
        return base + mRandom.nextInt(max - base);
    }

    private static final float SPEED_MOVE_BASE = 0.1f;

    private float getCurrentSpeed() {
        return SPEED_MOVE_BASE;
    }

    private static final int STAY_TIME_IDLE_MAX = 7000;
    private static final int STAY_TIME_IDLE_MIN = 3000;

    private int randomIdleStaytime() {
        return randomNumber(STAY_TIME_IDLE_MIN, STAY_TIME_IDLE_MAX);
    }

    private static final int STAY_TIME_SLEEP_MAX = 10000;
    private static final int STAY_TIME_SLEEP_MIN = 5000;

    private int randomSleepStaytime() {
        return randomNumber(STAY_TIME_SLEEP_MIN, STAY_TIME_SLEEP_MAX);
    }

    private static final int STAY_TIME_EAT_BASE = 3000;

    private int getEatMovementDuration(FoodInfo info) {
        return STAY_TIME_EAT_BASE;
    }

    private Movement getRandomMovementByWeight() {
        int weightIndex = randomMovementIndex();
        for(Movement move : mMovementList) {
            weightIndex -= move.weight;
            if (weightIndex < 0)
                return move;
        }
        return null;
    }

    private int randomMovementIndex() {
        int movementIndex = mRandom.nextInt(getMovementWeight());
        return movementIndex;
    }

    private int getMovementWeight() {
        return mTotalMovementWeight;
    }

    private void initMovement() {
        if (mMovementList != null)
            return;
        mMovementList = new ArrayList<Movement>();
        mMovementList.add(new Movement(State.Idle, 5));
        mMovementList.add(new Movement(State.Move, 3));
        mMovementList.add(new Movement(State.Sleep, 1));

        mTotalMovementWeight = 0;
        for (Movement movement : mMovementList) {
            mTotalMovementWeight += movement.weight;
        }
    }
}

