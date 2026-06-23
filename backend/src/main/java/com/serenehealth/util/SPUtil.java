package com.serenehealth.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {

    private static final String SP_NAME = "serene_health_prefs";

    private static SharedPreferences sp;

    public static void init(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static void putString(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public static String getString(String key, String def) {
        return sp.getString(key, def);
    }

    public static void putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean def) {
        return sp.getBoolean(key, def);
    }

    public static void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static int getInt(String key, int def) {
        return sp.getInt(key, def);
    }

    public static void putLong(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public static long getLong(String key, long def) {
        return sp.getLong(key, def);
    }

    public static boolean isFirstLaunch() {
        return getBoolean("is_first_launch", true);
    }

    public static void setFirstLaunchDone() {
        putBoolean("is_first_launch", false);
    }

    public static boolean isLoggedIn() {
        return getBoolean("is_logged_in", false);
    }

    public static void setLoggedIn(boolean loggedIn) {
        putBoolean("is_logged_in", loggedIn);
    }

    public static long getCurrentUserId() {
        return getLong("current_user_id", -1);
    }

    public static void setCurrentUserId(long userId) {
        putLong("current_user_id", userId);
    }

    public static void clear() {
        sp.edit().clear().apply();
    }
}
