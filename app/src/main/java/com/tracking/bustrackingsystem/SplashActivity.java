package com.tracking.bustrackingsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends AppCompatActivity {
    TextView tv_title;

    LangPrefs langPrefs;
    private static final String Locale_KeyValue = "l";
    public void loadLocale() {
        String language = langPrefs.get_Val(Locale_KeyValue);
        setLanguageForApp(language);
    }
    private void setLanguageForApp(String languageToLoad){
        Locale locale;
        if(languageToLoad.equals("")){
            locale = Locale.getDefault();
        }
        else {
            locale = new Locale(languageToLoad);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        try{
            langPrefs =new LangPrefs(getApplicationContext());
            loadLocale();
            setTitle(getResources().getString(R.string.bus_tracking_system));
            tv_title=findViewById(R.id.tv_title);
            tv_title.setText(getResources().getString(R.string.bus_tracking_system));
            finishSplash();
        }catch (Exception ex){
        }
    }

    void finishSplash(){
        try{
            Thread mSplashThread = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(300);
                        }
                    } catch (InterruptedException ex) {
                        ex.fillInStackTrace();
                    }
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            };
            mSplashThread.start();
        }catch (Exception ex){

        }
    }
}

