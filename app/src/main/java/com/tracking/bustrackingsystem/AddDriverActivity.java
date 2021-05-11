package com.tracking.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;

import java.util.Locale;

public class AddDriverActivity extends AppCompatActivity {

    EditText et_name, et_email, et_address, et_license,et_number, et_pwd;
    Button btn_add;
    Context context;
    private FirebaseAuth mAuth;
    Dialog_Loading loading;
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
        setContentView(R.layout.activity_add_driver);
        try{
            context=AddDriverActivity.this;
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            langPrefs=new LangPrefs(context);
            loadLocale();
            loading=new Dialog_Loading(context);
            et_name=findViewById(R.id.et_name);
            et_name.setHint(context.getResources().getString(R.string.name));
            et_email=findViewById(R.id.et_email);
            et_email.setHint(context.getResources().getString(R.string.email));
            et_address=findViewById(R.id.et_address);
            et_address.setHint(context.getResources().getString(R.string.address));
            et_license=findViewById(R.id.et_license);
            et_license.setHint(context.getResources().getString(R.string.license_number));
            et_number=findViewById(R.id.et_number);
            et_number.setHint(context.getResources().getString(R.string.number));
            et_pwd=findViewById(R.id.et_Pwd);
            et_pwd.setHint(context.getResources().getString(R.string.password));
            btn_add=findViewById(R.id.btnAdd);
            btn_add.setText(context.getResources().getString(R.string.add_driver));
            btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!et_name.getText().toString().equalsIgnoreCase("") &&
                            !et_email.getText().toString().equalsIgnoreCase("") &&
                            !et_address.getText().toString().equalsIgnoreCase("") &&
                            !et_license.getText().toString().equalsIgnoreCase("") &&
                            !et_number.getText().toString().equalsIgnoreCase("") &&
                            !et_pwd.getText().toString().equalsIgnoreCase("") ) {
                        if (!isValidEmail(et_email.getText().toString())) {
                            DialogClass dialog = new DialogClass(context,
                                    context.getResources().getString(R.string.message), context.getResources().getString(R.string.enter_valid_email));
                            dialog.show();
                        }  else if (et_pwd.getText().toString().length()<6) {
                            DialogClass dialog = new DialogClass(context,
                                    context.getResources().getString(R.string.message), context.getResources().getString(R.string.password_length_should_be_greater_than_or_equal_to));
                            dialog.show();
                        }else {
                            mAuth = FirebaseAuth.getInstance();
                            signUp(et_email.getText().toString().trim(),
                                    et_name.getText().toString().trim(),
                                    et_address.getText().toString().trim(),
                                    et_license.getText().toString().trim(),
                                    et_number.getText().toString().trim(),
                                    et_pwd.getText().toString().trim());
                        }
                    } else {
                        DialogClass dialog = new DialogClass(context,
                                context.getResources().getString(R.string.message), context.getResources().getString(R.string.fill_all_fields));
                        dialog.show();
                    }
                }
            });
        }catch (Exception ex){
        }
    }
    void signUp(final String email, final String name, final String address, final String license, final String number, final String password) {
        loading.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (sendRegistrationToServer(email, name,address,license, number, password)) {
                                loading.dismiss();
                                et_name.setText("");
                                et_email.setText("");
                                et_address.setText("");
                                et_license.setText("");
                                et_number.setText("");
                                et_pwd.setText("");

                            } else {
                                mAuth.getCurrentUser().delete();
                                loading.dismiss();
                            }
                        } else {
                            loading.dismiss();
                            DialogClass dialog = new DialogClass(context,
                                    context.getResources().getString(R.string.message), context.getResources().getString(R.string.authentication_failed));
                            dialog.show();

                        }
                    }
                });



    }
    private boolean sendRegistrationToServer(String email,String name, String address, String license,String num, String pass) {
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();

            FirebaseUser user = mAuth.getCurrentUser();
            String uid = user.getUid();
            DatabaseReference ref = database.getReference("IDs/" + uid);

            ref.child("UserEmail").setValue(email);
            ref.child("Name").setValue(name);
            ref.child("Address").setValue(address);
            ref.child("License").setValue(license);
            ref.child("Number").setValue(num);
            ref.child("Type").setValue("d");
            ref.child("status").setValue("1");

            ref.push();

            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        loading.dismiss();
                        DialogClass dialog = new DialogClass(context,
                                context.getResources().getString(R.string.message), context.getResources().getString(R.string.driver_added_successfully)+" "+getString(R.string.verify_Mail));
                        dialog.show();

                    }
                }
            });
            return true;
        }catch (Exception ex){
            loading.dismiss();
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
