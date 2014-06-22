package com.jifenqi;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;

public class Utils {
    public static Dialog getErrorDigitDialog(Context context) {
        return new AlertDialog.Builder(context)
        .setTitle(R.string.wrong_digit_title)
        .setMessage(R.string.wrong_digit_message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                
            }
        })
        .create();
    }
    
    public static int getShuxingPlayer(int zhuangjiaPlayer) {
        return (zhuangjiaPlayer + 2) % 4;
    }

    public static String getZipaiDir() {
        String path = null;
        File extDir = Environment.getExternalStorageDirectory();
        try {
            String zipaiDirPath = extDir.getCanonicalPath() + "/" + Const.APP_DIR + "/" + Const.ZIPAI;
            File zipaiDir = new File(zipaiDirPath);
            if(!zipaiDir.exists()) {
                boolean ret = zipaiDir.mkdirs();
                if(!ret) {
                    return null;
                }
            }
            path = zipaiDir.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }
    
    public static String getFilePath(String fileName) {
        return getZipaiDir() + "/" + fileName;
    }
    
    public static boolean getBooleanSP(Context context, String key) {
        SharedPreferences settings =  context.getSharedPreferences(Const.PREF_NAME, 0);
        return settings.getBoolean(key, false);
    }
    
    public static void putBooleanSP(Context context, String key, boolean value) {
        SharedPreferences settings =  context.getSharedPreferences(Const.PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    public static int getIntSP(Context context, String key) {
        SharedPreferences settings =  context.getSharedPreferences(Const.PREF_NAME, 0);
        return settings.getInt(key, 1);
    }
    
    public static void putIntSP(Context context, String key, int value) {
        SharedPreferences settings =  context.getSharedPreferences(Const.PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    
    public static String getStringSP(Context context, String key) {
        SharedPreferences settings =  context.getSharedPreferences(Const.PREF_NAME, 0);
        return settings.getString(key, "");
    }
    
    public static void putStringSP(Context context, String key, String value) {
        SharedPreferences settings =  context.getSharedPreferences(Const.PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
