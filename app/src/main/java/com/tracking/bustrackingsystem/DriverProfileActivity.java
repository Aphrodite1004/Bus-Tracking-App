package com.tracking.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Model.Model_Bus;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverProfileActivity extends AppCompatActivity {

    Context context;
    MyPrefs prefs;
    String user_type;
    Button btn_update;
    TextView tv_title_name, tv_title_ph_num, tv_title_address, tv_title_license, tv_title_bus,tv_title_email;
    TextView tv_email, tv_name, tv_ph_num, tv_address, tv_license, tv_bus;
    String id;
    Intent intent;
    Dialog_Loading loading;

    AlertDialog.Builder builder;
    AlertDialog dialog;
    String current_bus="";
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
        setContentView(R.layout.activity_driver_profile);
        try {
            context = DriverProfileActivity.this;
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            loading=new Dialog_Loading(context);
            langPrefs=new LangPrefs(context);
            loadLocale();
            database= FirebaseDatabase.getInstance();
            prefs=new MyPrefs(context);
            user_type=prefs.get_Val("user_type");

            tv_title_name = findViewById(R.id.tv_title_name);
            tv_title_name.setHint(context.getResources().getString(R.string.name));
            tv_title_ph_num = findViewById(R.id.tv_title_ph_num);

            tv_title_address = findViewById(R.id.tv_title_address);

            tv_title_license = findViewById(R.id.tv_title_license);

            tv_title_bus = findViewById(R.id.tv_title_bus_no);
            tv_title_email = findViewById(R.id.tv_title_email);

            tv_email= findViewById(R.id.tv_email);
            tv_name = findViewById(R.id.tv_name);
            tv_ph_num = findViewById(R.id.tv_ph_num);
            tv_address = findViewById(R.id.tv_address);
            tv_license = findViewById(R.id.tv_license);
            tv_bus = findViewById(R.id.tv_bus_no);

            btn_update = findViewById(R.id.btn_update);
            btn_update.setText(context.getResources().getString(R.string.update));
            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    update_User();
                }
            });


            if (user_type.equalsIgnoreCase("a")) {
                intent=getIntent();
                id=intent.getStringExtra("id");
                Drawable img = context.getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp);
                tv_title_ph_num.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                tv_title_address.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                tv_title_license.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                tv_title_bus.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                btn_update.setVisibility(View.VISIBLE);
            } else {
                id=prefs.get_Val("ID");
                btn_update.setVisibility(View.GONE);
                tv_title_ph_num.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                tv_title_address.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                tv_title_license.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                tv_title_bus.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

                tv_title_name.setEnabled(false);
                        tv_title_ph_num.setEnabled(false);
                tv_title_address.setEnabled(false);
                        tv_title_license.setEnabled(false);
                tv_title_bus.setEnabled(false);

                btn_update.setVisibility(View.GONE);
            }

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
                    et_text.setHint(context.getResources().getString(R.string.phone_number));
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
            tv_title_address.setOnClickListener(new View.OnClickListener() {
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
                    tv_title.setText(context.getResources().getString(R.string.address));
                    et_text.setHint(context.getResources().getString(R.string.address));
                    et_text.setText(tv_address.getText().toString());
                    Button btn_ok=update_dialog.findViewById(R.id.btn_OK);
                    btn_ok.setText(context.getResources().getString(R.string.ok));
                    btn_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!et_text.getText().toString().equalsIgnoreCase("")) {
                                tv_address.setText(et_text.getText().toString().trim());
                                update_dialog.dismiss();
                            }else {
                                DialogClass dialog = new DialogClass(context,
                                        context.getResources().getString(R.string.message), context.getResources().getString(R.string.enter_address));
                                dialog.show();
                            }
                        }
                    });
                    update_dialog.show();
                }
            });
            tv_title_license.setOnClickListener(new View.OnClickListener() {
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
                    tv_title.setText(context.getResources().getString(R.string.license_number));
                    et_text.setHint(context.getResources().getString(R.string.license_number));
                    et_text.setText(tv_license.getText().toString());
                    Button btn_ok=update_dialog.findViewById(R.id.btn_OK);
                    btn_ok.setText(context.getResources().getString(R.string.ok));
                    btn_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!et_text.getText().toString().equalsIgnoreCase("")) {
                                tv_license.setText(et_text.getText().toString().trim());
                                update_dialog.dismiss();
                            }else {
                                DialogClass dialog = new DialogClass(context,
                                        context.getResources().getString(R.string.message), context.getResources().getString(R.string.enter_license));
                                dialog.show();
                            }
                        }
                    });
                    update_dialog.show();
                }
            });
            tv_title_bus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(!tv_bus.getText().toString().equals("")) {
                            current_bus = tv_bus.getText().toString();
                        }
                        dialog = builder.create();
                        dialog.show();
                    }catch (Exception ex){
                        Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });
            tv_title_email.setText(context.getResources().getString(R.string.email));
            tv_title_name.setText(context.getResources().getString(R.string.name));
            tv_title_ph_num.setText(context.getResources().getString(R.string.phone_number));
            tv_title_address.setText(context.getResources().getString(R.string.address));
            tv_title_license.setText(context.getResources().getString(R.string.license_number));
            tv_title_bus.setText(context.getResources().getString(R.string.assigned_bus));
            getval();
            get_Buses();
        } catch (Exception ex) {

        }
    }

    List<Model_Bus> temp;
    String[] lst_bus;
    FirebaseDatabase database;
    void get_Buses() {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.buses));
        temp = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("Bus/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /*lst_bus=new  String[]{"sd","dsdsd"};
                builder.setItems(lst_bus, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tv_bus.setText(lst_bus[which]);
                    }
                });*/
                try {
                    String bus_no = "";
                    for (DataSnapshot ds_1 : dataSnapshot.getChildren()) {
                        bus_no = ds_1.getKey();
                        for (DataSnapshot ds : ds_1.getChildren()) {
                            String driver = ds.child("Driver").getValue().toString();
                            if (driver.equalsIgnoreCase("")) {
                                Model_Bus bus = new Model_Bus();
                                bus.setBus_no(bus_no);
                                bus.setDriver(ds.child("Driver").getValue().toString());
                                boolean flag=false;
                                if (temp.size() > 0) {
                                    for (int i = 0; i < temp.size(); i++) {
                                        if (temp.get(i).getBus_no().equals(bus_no)) {
                                            flag=true;
                                        }
                                    }
                                }
                                if(!flag){
                                    temp.add(bus);
                                }
                            }
                        }
                    }

                    lst_bus = new String[temp.size()];
                    for (int i = 0; i < temp.size(); i++) {
                        lst_bus[i] = temp.get(i).getBus_no();
                    }
                    builder.setItems(lst_bus, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tv_bus.setText(lst_bus[which]);
                        }
                    });
                } catch (Exception ex) {
                    Toast.makeText(context, "get bus" + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loading.dismiss();
                }
            });
            database.getReference("IDs/").child(id).child("Address").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tv_address.setText(dataSnapshot.getValue(String.class));

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loading.dismiss();
                }
            });
            database.getReference("IDs/").child(id).child("License").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tv_license.setText(dataSnapshot.getValue(String.class));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loading.dismiss();
                }
            });
            database.getReference("IDs/").child(id).child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tv_bus.setText(dataSnapshot.getValue(String.class));
                    if(!dataSnapshot.getValue(String.class).equals("")) {
                        current_bus = tv_bus.getText().toString();
                    }
                    loading.dismiss();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loading.dismiss();
                }
            });

        }catch (Exception ex){
            loading.dismiss();
            Toast.makeText(context,"get val"+ex.getMessage(),Toast.LENGTH_LONG).show();

        }
    }

    void update_User(){
        try {
            loading.show();
            database.getReference("IDs/").child(id).child("Number").setValue(tv_ph_num.getText().toString());
            database.getReference("IDs/").child(id).child("Name").setValue(tv_name.getText().toString());
            database.getReference("IDs/").child(id).child("Address").setValue(tv_address.getText().toString());
            database.getReference("IDs/").child(id).child("License").setValue(tv_license.getText().toString());
            database.getReference("IDs/").child(id).child("Bus").setValue(tv_bus.getText().toString());

            if(!current_bus.equals("")) {
                //CHANGE KRNA HA
                database.getReference("Bus/").child(current_bus).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            for(DataSnapshot ds: dataSnapshot.getChildren()) {
                                database.getReference("Bus/").child(current_bus).child(ds.getKey()).child("Driver").setValue("");
                            }
                        }catch (Exception ex){
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        loading.dismiss();
                    }
                });
                //database.getReference("Bus/").child(current_bus).child("Driver").setValue("");
            }

            final String bus=tv_bus.getText().toString();
            final String name=tv_name.getText().toString();

            //FirebaseDatabase database = FirebaseDatabase.getInstance();

            database.getReference("Bus/").child(bus).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        for(DataSnapshot ds: dataSnapshot.getChildren()) {
                            database.getReference("Bus/").child(bus).child(ds.getKey()).child("Driver").setValue(name);
                        }
                    }catch (Exception ex){
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    loading.dismiss();
                }
            });


            //database.getReference("Bus/").child(bus).child("Driver").setValue(name);

            Toast.makeText(context,context.getResources().getString(R.string.updated),Toast.LENGTH_LONG).show();
            loading.dismiss();
            get_Buses();
        }catch (Exception ex){
            loading.dismiss();
            Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
}