package com.tracking.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.Locale;

public class RecoverPwdActivity extends AppCompatActivity {
    EditText et_email;
    Button btn_recover;
    MyPrefs prefs;
    Context context;
    String email;
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
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_pwd);
        try {
            context=RecoverPwdActivity.this;
            langPrefs=new LangPrefs(context);
            loadLocale();
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            prefs=new MyPrefs(context);
            email=prefs.get_Val("email");
            et_email = (EditText) findViewById(R.id.tv_Email);
            et_email.setHint(context.getResources().getString(R.string.email));
            if(!email.equalsIgnoreCase("")){
                et_email.setText(email);
                et_email.setEnabled(false);
            }

            btn_recover = (Button) findViewById(R.id.btnRecover);
            btn_recover.setText(context.getResources().getString(R.string.recover));
            btn_recover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = et_email.getText().toString().trim();
                    if (!email.equalsIgnoreCase("")) {
                        if (isValidEmail(email)) {
                            //FORGET PASSWORD CODING
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Forget Password", "Email sent.");
                                            }
                                        }
                                    });
                            DialogClass dialog = new DialogClass(RecoverPwdActivity.this,
                                    context.getResources().getString(R.string.message), context.getResources().getString(R.string.check_email));
                            dialog.show();
                        } else {
                            DialogClass dialog = new DialogClass(RecoverPwdActivity.this,
                                    context.getResources().getString(R.string.message), context.getResources().getString(R.string.enter_valid_email));
                            dialog.show();
                        }
                    } else {
                        DialogClass dialog = new DialogClass(RecoverPwdActivity.this,
                                context.getResources().getString(R.string.message),
                                context.getResources().getString(R.string.please_enter_Email));
                        dialog.show();
                    }
                }
            });
        } catch (Exception ex) {
        }
    }
}
