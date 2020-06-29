package com.chatandroid.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import com.chatandroid.R;
import com.google.firebase.database.DataSnapshot;

public class Tools {

    public static void setSystemBarColorInt(Activity act, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

    public static void copyToClipboard(Context context, String data) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clipboard", data);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.text_coppied, Toast.LENGTH_SHORT).show();
    }

    public static String getRefValue(DataSnapshot dataSnapshot) {
        return (dataSnapshot.exists()) ? dataSnapshot.getValue().toString() : "";
    }

}
