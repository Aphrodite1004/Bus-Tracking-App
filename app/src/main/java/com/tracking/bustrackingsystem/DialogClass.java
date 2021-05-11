package com.tracking.bustrackingsystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;

import com.tracking.bustrackingsystem.Utils.LangPrefs;

import java.util.Locale;


//This class is for showing the Dialog
public class DialogClass {
    Context context;
    String txt,title;
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
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }
    //Constructor that take title and message that you want to show, first call this
    public DialogClass(Context context, String title, String txt) {
        this.context = context;
        this.txt = txt;
        this.title = title;
        langPrefs=new LangPrefs(context);
        loadLocale();
    }

    //than call this to show the dialog
    public void show() {
        try {
            //Initialize the dialog
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);

            //set the message text for the dialog
            dialog.setMessage(txt);

            //set the title text for the dialog
            dialog.setTitle(title);

            //set the text of dilog button
            dialog.setPositiveButton(context.getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = dialog.create();


            alertDialog.show();
        }catch (Exception ex){
        }
    }
}
