package com.android.launcher3.view;

import android.content.ComponentName;
import android.content.Context;

import com.android.launcher3.AppInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.util.Logger;

import java.util.HashMap;

/**
 * Created by NineG on 2016/7/3.
 */
public class UnitManager {
    private static final String LOG_TAG = Logger.getLogTag(UnitManager.class);

    private HashMap<String, BaseUnitInfo> mBaseInfos = new HashMap<String, BaseUnitInfo>();

    public UnitManager() {
    }

    public void addUnit(BaseUnitInfo info) {
        Logger.d(LOG_TAG, "addUnit info %s", info);
        mBaseInfos.put(info.id, info);
    }

    public void remove(BaseUnitInfo info) {
        Logger.d(LOG_TAG, "remove info %s", info);
        mBaseInfos.remove(info.id);
    }
//
//    public boolean feed(FoodInfo foodInfo, PetInfo petInfo) {
//        if (hungry(foodInfo, petInfo)) {
//            return petInfo.eat(foodInfo);
//        }
//        return false;
//    }
//
//    private boolean hungry(FoodInfo foodInfo, PetInfo petInfo) {
//        return true;
//    }

    public static BaseUnitInfo transformFromItemInfo(Context context, ItemInfo itemInfo) {
        BaseUnitInfo result = null;
        if (itemInfo instanceof AppInfo) {
            AppInfo appInfo = (AppInfo) itemInfo;
            ComponentName cn = appInfo.getComponentName();
            result = generateInfo(context, cn == null ? null : cn.toString());
        } else if (itemInfo instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
            ComponentName cn = shortcutInfo.getTargetComponent();
            result = generateInfo(context, cn == null ? null : cn.toString());
        }

        return result;
    }

    private static BaseUnitInfo generateInfo(Context context, String significantString) {
        if (significantString == null)
            return null;

        int sum = 0;
        for(int i = 0; i < significantString.length(); ++i) {
            sum += significantString.codePointAt(i);
        }

        Logger.d(LOG_TAG, "generateInfo fromItemInfo %s, %s", sum, significantString);
        BaseUnitInfo result = new FoodCornInfo(context);
        return result;
    }

    public void notifyNewItemFired(Context context, BaseUnitInfo newInfo) {
        Logger.d(LOG_TAG, "notifyNewItemFired %s", newInfo);
        for(BaseUnitInfo info : mBaseInfos.values()) {
            if (info.equals(newInfo))
                continue;
            if (info.notifyNewInfo(context, newInfo)) {
                Logger.d(LOG_TAG, "%s handle %s", info, newInfo);
                break;
            }
        }
    }

    public void notifyInfoChanged(Context context, BaseUnitInfo infoChanged) {
        Logger.d(LOG_TAG, "notifyInfoChanged %s", infoChanged);
        for(BaseUnitInfo info : mBaseInfos.values()) {
            if (info.equals(infoChanged))
                continue;
            if (info.notifyInfoChanged(context, infoChanged)) {
                Logger.d(LOG_TAG, "%s handle change %s", info, infoChanged);
            }
        }
    }

    public void notifyPauseByDragged() {
        Logger.d(LOG_TAG, "notifyPauseByDragged");
        for(BaseUnitInfo info : mBaseInfos.values()) {
            info.onPauseAction();
        }
    }

    public void notifyResumeByDragged() {
        Logger.d(LOG_TAG, "notifyResumeByDragged");
        for(BaseUnitInfo info : mBaseInfos.values()) {
            info.onResumeAction();
        }
    }
}
