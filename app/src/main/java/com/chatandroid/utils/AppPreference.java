package com.chatandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreference {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "ptit_chatters";

    // All Shared Preferences Keys
    private static final String CURRENT_CHATTING_USER = "current_chatting_user";
    private static final String LOCALE = "locale";
    private static final String NIGHT_MODE = "night_mode";

    // Constructor
    public AppPreference(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Current chatting user
     */
    public void setCurrentChattingUser(String user) {

        editor.putString(CURRENT_CHATTING_USER, user);

        editor.commit();
    }

    public String getCurrentChattingUser() {
        return pref.getString(CURRENT_CHATTING_USER, null);
    }

    public void setAppLanguage(String locale) {
        editor.putString(LOCALE, locale);

        editor.commit();
    }

    public String getAppLanguage() {
        return pref.getString(LOCALE, "vi");
    }

    public void setNightMode(boolean nightMode) {
        editor.putBoolean(NIGHT_MODE, nightMode);
        editor.commit();
    }

    public boolean getNightMode() {
        return pref.getBoolean("night_mode", false);
    }

    public void destroy() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }
}
