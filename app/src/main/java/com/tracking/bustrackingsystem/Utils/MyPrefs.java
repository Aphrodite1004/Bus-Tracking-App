package com.tracking.bustrackingsystem.Utils;

import android.content.Context;
import android.content.SharedPreferences;


public class MyPrefs {
    public static final String MyPREFERENCES = "MyPrefs";

    SharedPreferences sharedpreferences;
    Context ctx;


    public MyPrefs(Context c) {
        this.ctx = c;
        sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    public void put_Val(String key, String value) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String get_Val(String key) {
        String val = "";
        sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        val = sharedpreferences.getString(key, "");
        return val;
    }

    public void Clear_Pref(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }
}
