package com.serenehealth.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {

    private static final String SP_NAME = "serene_health_prefs";

    private static SharedPreferences sp;

    public static void init(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences getSp() {
        return sp;
    }

    public static void putString(String key, String value) {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            prefs.edit().putString(key, value).apply();
        }
    }

    public static String getString(String key, String def) {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            return prefs.getString(key, def);
        }
        return def;
    }

    public static void putBoolean(String key, boolean value) {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            prefs.edit().putBoolean(key, value).apply();
        }
    }

    public static boolean getBoolean(String key, boolean def) {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            return prefs.getBoolean(key, def);
        }
        return def;
    }

    public static void putInt(String key, int value) {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            prefs.edit().putInt(key, value).apply();
        }
    }

    public static int getInt(String key, int def) {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            return prefs.getInt(key, def);
        }
        return def;
    }

    public static void putLong(String key, long value) {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            prefs.edit().putLong(key, value).apply();
        }
    }

    public static long getLong(String key, long def) {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            return prefs.getLong(key, def);
        }
        return def;
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

    // ==================== Admin Session ====================

    public static void setAdminRole(String role) {
        putString("admin_role", role);
    }

    public static String getAdminRole() {
        return getString("admin_role", "");
    }

    public static void setAdminDoctorId(long doctorId) {
        putLong("admin_doctor_id", doctorId);
    }

    public static long getAdminDoctorId() {
        return getLong("admin_doctor_id", -1);
    }

    public static void clearAdmin() {
        putString("admin_role", "");
        putLong("admin_doctor_id", -1);
    }

    public static boolean isAdminLoggedIn() {
        return !getAdminRole().isEmpty();
    }

    public static void clear() {
        SharedPreferences prefs = getSp();
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
    }
}
