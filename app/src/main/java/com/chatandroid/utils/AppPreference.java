package com.chatandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreference {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "ptit_chatters";

    // All Shared Preferences Keys
    private static final String CURRENT_CHATING_USER = "current_chatting_user";


    // Constructor
    public AppPreference(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    /**
     * Current chatting user
     * */
    public void setCurrentChattingUser(String user){

        editor.putString(CURRENT_CHATING_USER, user);

        // commit changes
        editor.commit();
    }

    public String getCurrentChattingUser(){
        return  pref.getString(CURRENT_CHATING_USER,null);
    }


    public void removeCurrentChattingUser(){
        editor.remove(CURRENT_CHATING_USER);
    }

    public void destroy(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }


}
