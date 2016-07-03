package com.android.launcher3.view;

import java.util.HashMap;

/**
 * Created by NineG on 2016/7/3.
 */
public class UnitManager {
    private HashMap<String, BaseUnitInfo> mBaseInfos;

    public UnitManager() {
    }

    public void addUnit(BaseUnitInfo info) {
        mBaseInfos.put(info.id, info);
    }

    public void remove(BaseUnitInfo info) {
        mBaseInfos.remove(info.id);
    }

    public boolean feed(FoodInfo foodInfo, PetInfo petInfo) {
        if (hungry(foodInfo, petInfo)) {
            return petInfo.eat(foodInfo);
        }
        return false;
    }

    private boolean hungry(FoodInfo foodInfo, PetInfo petInfo) {
        return true;
    }
}
