package com.tracking.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Model.User;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    Context context;
    EditText et_email, et_pwd;
    Button btn_login;
    RadioButton rd_admin, rd_user, rd_driver;
    TextView tv_not_mem_1, tv_not_mem_2,tv_guest;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    TextView tv_recover;
    MyPrefs prefs;

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
    private void onRunTimePermissionGranted() {

        isDeninedRTPs = false;

    }
    /******************************************************/

    /**
     * predefined method to check run time permissions list call back
     *
     * @param requestCode   : to handle the corresponding request
     * @param permissions:  contains the list of requested permissions
     * @param grantResults: contains granted and un granted permissions result list
     */
    private boolean isDeninedRTPs = true;       // initially true to prevent anim(2)
    private boolean showRationaleRTPs = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_MULTIPLE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isDeninedRTPs = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showRationaleRTPs = shouldShowRequestPermissionRationale(permission);
                        }

                        break;
                    }

                }
                onRunTimePermissionDenied();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /******************************************************/

    private void onRunTimePermissionDenied() {
        if (isDeninedRTPs) {
            if (!showRationaleRTPs) {
                //goToSettings();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSION_MULTIPLE);
                }
            }
        } else {
            onRunTimePermissionGranted();
        }
    }

    private final int REQUEST_CODE_PERMISSION_MULTIPLE = 123;

    private void checkAndRequestRunTimePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+

            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_PERMISSION_MULTIPLE);

            }
        }


        onRunTimePermissionGranted();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            context = LoginActivity.this;
            prefs = new MyPrefs(context);
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            langPrefs=new LangPrefs(context);
            loadLocale();

            mAuth = FirebaseAuth.getInstance();
            tv_guest=findViewById(R.id.tv_guest);
            tv_guest.setText(context.getResources().getString(R.string.login_as_guest));
            tv_guest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefs.put_Val("user_type", "g");
                    prefs.put_Val("email", "guest");
                    prefs.put_Val("name", "guest");
                    prefs.put_Val("ID", "");
                    startActivity(new Intent(context,MainActivity.class));
                }
            });
            user = mAuth.getCurrentUser();
            loading = new Dialog_Loading(context);

            if (user != null) {
                if (user.isEmailVerified()) {
                    startActivity(new Intent(LoginActivity.this,
                            MainActivity.class));
                } else {
                    DialogClass dialog = new DialogClass(context,
                            context.getResources().getString(R.string.message), "Please Verify Email to login.");
                    dialog.show();
                }
            }else if(prefs.get_Val("user_type").equalsIgnoreCase("g")) {
                startActivity(new Intent(LoginActivity.this,
                        MainActivity.class));
            }
            tv_recover = findViewById(R.id.btn_recover);
            tv_recover.setText(context.getResources().getString(R.string.reset_pwd));
            tv_recover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(context, RecoverPwdActivity.class));
                }
            });
            et_email = findViewById(R.id.et_Email);
            et_email.setHint(context.getResources().getString(R.string.email));
            et_pwd = findViewById(R.id.et_Pwd);
            et_pwd.setHint(context.getResources().getString(R.string.password));
            btn_login = findViewById(R.id.btnLogin);
            btn_login.setText(context.getResources().getString(R.string.sign_in));
            rd_admin = findViewById(R.id.rd_admin);

            rd_user = findViewById(R.id.rd_user);
            rd_driver = findViewById(R.id.rd_driver);
            tv_not_mem_1 = findViewById(R.id.btnNotMember1);
            tv_not_mem_2 = findViewById(R.id.btnNotMember2);
            tv_not_mem_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NotMem();
                }
            });
            tv_not_mem_1.setText(context.getResources().getString(R.string.not_a_member));
            tv_not_mem_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NotMem();
                }
            });
            tv_not_mem_2.setText(context.getResources().getString(R.string.create_account));

            btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!et_email.getText().toString().trim().equalsIgnoreCase("") &&
                            !et_pwd.getText().toString().trim().equalsIgnoreCase("")) {
                        if (!isValidEmail(et_email.getText().toString())) {
                            DialogClass dialog = new DialogClass(context,
                                    context.getResources().getString(R.string.message), context.getResources().getString(R.string.enter_valid_email));
                            dialog.show();
                        } else {
                            if (rd_user.isChecked()) {
                                Login(et_email.getText().toString(), et_pwd.getText().toString());
                            } else {
                                DialogClass dialog = new DialogClass(context,
                                        context.getResources().getString(R.string.message), context.getResources().getString(R.string.wrong_user_type_selected));
                                dialog.show();
                            }
                        }
                    } else {
                        DialogClass dialog = new DialogClass(context,
                                context.getResources().getString(R.string.message), context.getResources().getString(R.string.fill_all_fields));
                        dialog.show();
                    }
                }
            });
            checkAndRequestRunTimePermissions();
        } catch (Exception ex) {
        }
    }

    private void NotMem() {
        try {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        } catch (Exception ex) {

        }
    }

    private void Login(String email, String password) {
        try {
            loading.show();
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                if (user.isEmailVerified()) {
                                    check_User_Type();
                                } else {
                                    loading.dismiss();
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                    }
                                                }
                                            });
                                    DialogClass dialog = new DialogClass(context,
                                            context.getResources().getString(R.string.message), getString(R.string.verify_Mail));
                                    dialog.show();

                                }

                            } else {
                                loading.dismiss();
                                DialogClass dialog = new DialogClass(context,
                                        context.getResources().getString(R.string.message), context.getResources().getString(R.string.couldnt_login_wrong_email_password));
                                dialog.show();

                            }
                        }
                    });
        } catch (Exception ex) {
            loading.dismiss();
        }
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    void check_User_Type() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("IDs/").orderByChild("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> list = new ArrayList<>();
                try {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("Type").getValue() != null) {
                            User user = new User();
                            user.email = ds.child("UserEmail").getValue().toString();
                            user.type = ds.child("Type").getValue().toString();
                            user.name = ds.child("Name").getValue().toString();
                            user.ID = ds.getKey();
                            user.status = ds.child("status").getValue().toString();
                            list.add(user);
                        }
                    }
                    String type = "", email = "", ID = "",name="",status="";
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).email.equalsIgnoreCase(et_email.getText().toString().trim())) {
                            type = list.get(i).type;
                            email = list.get(i).email;
                            name = list.get(i).name;
                            ID = list.get(i).ID;
                            status=list.get(i).status;
                        }
                    }
                    if(status.equals("1")) {
                        loading.dismiss();
                        prefs.put_Val("user_type", type);
                        prefs.put_Val("email", email);
                        prefs.put_Val("name", name);
                        prefs.put_Val("ID", ID);

                        finish();
                        startActivity(new Intent(getApplicationContext(),
                                MainActivity.class));
                    }else {
                        loading.dismiss();
                        mAuth.signOut();
                        DialogClass dialogClass=new DialogClass(context,
                                context.getResources().getString(R.string.message),
                                context.getResources().getString(R.string.account_delete));
                        dialogClass.show();
                    }
                } catch (Exception ex) {
                    loading.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}