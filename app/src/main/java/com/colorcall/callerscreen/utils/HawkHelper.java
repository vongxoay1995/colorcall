package com.colorcall.callerscreen.utils;


import android.content.Context;

import com.colorcall.callerscreen.database.Background;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

public class HawkHelper {
    private static String LOAD_DATA_FIRST_FIRST = "LOAD_DATA_FIRST_FIRST";
    private static String ENABLE_COLOR = "ENABLE_COLOR";
    private static String ENABLE_FLASH = "ENABLE_FLASH";

    private static String LIST_BACKGROUND = "LIST_BACKGROUND";
    private static String TIME_STAMP = "TIME_STAMP";
    private static String LAST_TIME_SHOW_INTER = "LAST_TIME_SHOW_INTER";
    private static String COUNT_FOR_DIALOG_RATE = "CountShowRate";
    private static String COUNT_OPEN_APP = "COUNT_OPEN_APP";
    private static String CAN_SHOW_DIALOG_RATE = "CAN_SHOW_DIALOG_RATE";
    private static String COUNT_FOR_DIALOG_UPDATE = "COUNT_FOR_DIALOG_UPDATE";
    private static String CAN_SHOW_DIALOG_UPDATE = "CAN_SHOW_DIALOG_UPDATE";
    private static String FREE_APPLY_COUNT = "FREE_APPLY_COUNT";
    private static String IS_SCREEN_CALL = "IS_SCREEN_CALL";
    private static String PRIORITY_ADS = "PRIORITY_ADS";
    private static String IS_AB = "IS_AB";
    private static String IS_PAY = "IS_PAY";
    private static String IS_SHOWED_OB = "IS_SHOWED_OB";

    /**
     * Đảm bảo Hawk đã được khởi tạo trước khi dùng.
     * An toàn khi gọi nhiều lần và từ bất kỳ Context nào (BroadcastReceiver, Service, v.v.)
     */
    private static void ensureInit(Context context) {
        if (!Hawk.isBuilt()) {
            Hawk.init(context.getApplicationContext()).build();
        }
    }

    public static boolean isLoadDataFirst() {
        return Hawk.isBuilt() && Hawk.get(LOAD_DATA_FIRST_FIRST, false);
    }

    public static void setLoadDataFirst(boolean value) {
        if (Hawk.isBuilt()) Hawk.put(LOAD_DATA_FIRST_FIRST, value);
    }

    public static boolean isShowedOb() {
        return Hawk.isBuilt() && Hawk.get(IS_SHOWED_OB, false);
    }

    public static void setShowedOb(boolean value) {
        if (Hawk.isBuilt()) Hawk.put(IS_SHOWED_OB, value);
    }

    public static boolean isPayed() {
        return Hawk.isBuilt() && Hawk.get(IS_PAY, false);
    }

    public static void setPay(boolean value) {
        if (Hawk.isBuilt()) Hawk.put(IS_PAY, value);
    }


    public static ArrayList<Background> getListBackground() {
        return  SharedPreferencesUtil.INSTANCE.getListBackground();
        //return Hawk.get(LIST_BACKGROUND, new ArrayList<>());
    }

    public static void setListBackground(ArrayList<Background> listBackground) {
        SharedPreferencesUtil.INSTANCE.saveListBackground(listBackground);
        //if (Hawk.isBuilt()) Hawk.put(LIST_BACKGROUND, listBackground);
    }

    public static boolean isEnableColorCall() {
        return Hawk.isBuilt() && Hawk.get(ENABLE_COLOR, false);
    }

    public static void setStateColorCall(boolean value) {
        if (Hawk.isBuilt()) Hawk.put(ENABLE_COLOR, value);
    }

    public static long getTimeStamp() {
        return Hawk.isBuilt() ? Hawk.get(TIME_STAMP, (long) 0) : 0L;
    }

    public static void setTimeStamp(long timeStamp) {
        if (Hawk.isBuilt()) Hawk.put(TIME_STAMP, timeStamp);
    }

    public static boolean isEnableFlash() {
        return Hawk.isBuilt() && Hawk.get(ENABLE_FLASH, false);
    }

    public static void setFlash(boolean value) {
        if (Hawk.isBuilt()) Hawk.put(ENABLE_FLASH, value);
    }

    public static void setBackgroundSelect(Background backgroundSelect) {
        BackgroundSelectionStorage.put(backgroundSelect);
    }

    public static Background getBackgroundSelect() {
        Background background = new Background(null, 0, "thumbDefault/default1.webp", "/raw/default1", false, "default1");
        return BackgroundSelectionStorage.get(background);
    }


    public static int getCountOpenApp() {
        return Hawk.isBuilt() ? Hawk.get(COUNT_OPEN_APP, 0) : 0;
    }

    public static void setCountOpenApp(int value) {
        if (Hawk.isBuilt()) Hawk.put(COUNT_OPEN_APP, value);
    }

    public static int getCoutShowRate() {
        return Hawk.isBuilt() ? Hawk.get(COUNT_FOR_DIALOG_RATE, 0) : 0;
    }

    public static void setCountRate(int value) {
        if (Hawk.isBuilt()) Hawk.put(COUNT_FOR_DIALOG_RATE, value);
    }

    public static boolean isCanShowDiaLogRate() {
        return !Hawk.isBuilt() || Hawk.get(CAN_SHOW_DIALOG_RATE, true);
    }

    public static void setDialogShowRate(boolean value) {
        if (Hawk.isBuilt()) Hawk.put(CAN_SHOW_DIALOG_RATE, value);
    }

    public static int getFreeApplyCount() {
        return Hawk.isBuilt() ? Hawk.get(FREE_APPLY_COUNT, 0) : 0;
    }

    public static void setFreeApplyCount(int count) {
        if (Hawk.isBuilt()) Hawk.put(FREE_APPLY_COUNT, count);
    }
}
