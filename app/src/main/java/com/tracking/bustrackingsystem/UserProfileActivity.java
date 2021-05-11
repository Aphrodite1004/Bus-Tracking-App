package com.tracking.bustrackingsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {

    TextView tv_title_name, tv_title_ph_num,tv_title_email;
    TextView tv_email, tv_name, tv_ph_num;
    Context context;
    Button btn_update;
    String user_type;
    MyPrefs prefs;
    String id;
    Intent intent;
    Dialog_Loading loading;
    FirebaseDatabase database;
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
        setContentView(R.layout.activity_user_profile);
        try {
            context = UserProfileActivity.this;
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            loading=new Dialog_Loading(context);
            langPrefs=new LangPrefs(context);
            loadLocale();
            database= FirebaseDatabase.getInstance();
            prefs=new MyPrefs(context);
            tv_name = findViewById(R.id.tv_name);

            tv_ph_num = findViewById(R.id.tv_ph_num);

            tv_email= findViewById(R.id.tv_email);


            btn_update = findViewById(R.id.btn_update);
            btn_update.setText(context.getResources().getString(R.string.update));
            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    update_User();
                }
            });

            user_type=prefs.get_Val("user_type");
            tv_title_name = findViewById(R.id.tv_title_name);
            tv_title_ph_num = findViewById(R.id.tv_title_ph_num);
            tv_title_email = findViewById(R.id.tv_title_email);
            tv_title_name.setText(context.getResources().getString(R.string.name));
            tv_title_ph_num.setText(context.getResources().getString(R.string.phone_number));
            tv_title_email.setText(context.getResources().getString(R.string.email));


            if (user_type.equalsIgnoreCase("a")) {
                intent=getIntent();
                id=intent.getStringExtra("id");

                Drawable img = context.getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp);
                tv_title_name.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                tv_title_ph_num.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                btn_update.setVisibility(View.VISIBLE);
            }else{
                id=prefs.get_Val("ID");
                btn_update.setVisibility(View.GONE);
                tv_title_name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                tv_title_ph_num.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                tv_title_name.setEnabled(false);
                tv_title_ph_num.setEnabled(false);
                tv_email.setText(prefs.get_Val("email"));
                tv_name.setText(prefs.get_Val("name"));
                tv_ph_num.setText(prefs.get_Val("number"));
            }

            getval();
            tv_title_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog update_dialog = new Dialog(context);
                    update_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    update_dialog.setCancelable(true);
                    update_dialog.setContentView(R.layout.dialog_item);
                    Window window = update_dialog.getWindow();
                    window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
                    ImageView img_close=update_dialog.findViewById(R.id.btnclose);
                    img_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            update_dialog.dismiss();
                        }
                    });
                    TextView tv_title=update_dialog.findViewById(R.id.tv_title);
                    final EditText et_text=update_dialog.findViewById(R.id.tv_text);
                    tv_title.setText(context.getResources().getString(R.string.name));
                    et_text.setHint(context.getResources().getString(R.string.name));
                    et_text.setText(tv_name.getText().toString());
                    Button btn_ok=update_dialog.findViewById(R.id.btn_OK);
                    btn_ok.setText(context.getResources().getString(R.string.ok));
                    btn_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!et_text.getText().toString().equalsIgnoreCase("")) {
                                tv_name.setText(et_text.getText().toString().trim());
                                update_dialog.dismiss();
                            }else {
                                DialogClass dialog = new DialogClass(context,
                                        context.getResources().getString(R.string.message), context.getResources().getString(R.string.enter_name));
                                dialog.show();
                            }
                        }
                    });
                    update_dialog.show();
                }
            });
            tv_title_ph_num.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog update_dialog = new Dialog(context);
                    update_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    update_dialog.setCancelable(true);
                    update_dialog.setContentView(R.layout.dialog_item);
                    Window window = update_dialog.getWindow();
                    window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
                    ImageView img_close=update_dialog.findViewById(R.id.btnclose);
                    img_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            update_dialog.dismiss();
                        }
                    });
                    TextView tv_title=update_dialog.findViewById(R.id.tv_title);
                    final EditText et_text=update_dialog.findViewById(R.id.tv_text);
                    tv_title.setText(context.getResources().getString(R.string.phone_number));
                    et_text.setHint(context.getResources().getString(R.string.enter_phone_number));
                    et_text.setText(tv_ph_num.getText().toString());
                    Button btn_ok=update_dialog.findViewById(R.id.btn_OK);
                    btn_ok.setText(context.getResources().getString(R.string.ok));
                    btn_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!et_text.getText().toString().equalsIgnoreCase("")) {
                                tv_ph_num.setText(et_text.getText().toString().trim());
                                update_dialog.dismiss();
                            }else {
                                DialogClass dialog = new DialogClass(context,
                                        context.getResources().getString(R.string.message), context.getResources().getString(R.string.enter_phone_number));
                                dialog.show();
                            }
                        }
                    });
                    update_dialog.show();
                }
            });

        } catch (Exception ex) {
        }
    }

    void getval(){
        try{
            loading.show();
            database.getReference("IDs/").child(id).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tv_name.setText(dataSnapshot.getValue(String.class));

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loading.dismiss();
                }
            });
            database.getReference("IDs/").child(id).child("UserEmail").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tv_email.setText(dataSnapshot.getValue(String.class));

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loading.dismiss();
                }
            });
            database.getReference("IDs/").child(id).child("Number").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tv_ph_num.setText(dataSnapshot.getValue(String.class));
                    loading.dismiss();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loading.dismiss();
                }
            });

        }catch (Exception ex){
            loading.dismiss();
            Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();

        }
    }
    void update_User(){
        try {
            loading.show();

            database.getReference("IDs/").child(id).child("Number").setValue(tv_ph_num.getText().toString());
            database.getReference("IDs/").child(id).child("Name").setValue(tv_name.getText().toString());
            Toast.makeText(context,"Updated",Toast.LENGTH_LONG).show();
            loading.dismiss();
        }catch (Exception ex){
            loading.dismiss();
            Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

}
