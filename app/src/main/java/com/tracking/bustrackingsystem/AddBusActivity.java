package com.tracking.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddBusActivity extends AppCompatActivity {

    Context context;
    EditText et_bus_no, et_driver;
    ImageView img_driver;
    Dialog_Loading loading;
    String[] lst_driver;
    List<String> temp = new ArrayList<>();
    AlertDialog.Builder builder;
    AlertDialog dialog;
    Button btn_add;
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
        setContentView(R.layout.activity_add_bus);
        try {
            context = AddBusActivity.this;
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            loading = new Dialog_Loading(context);
            langPrefs=new LangPrefs(context);
            loadLocale();
            et_bus_no = findViewById(R.id.et_bus_no);
            et_bus_no.setHint(context.getResources().getString(R.string.bus_number));
            et_driver = findViewById(R.id.et_driver);
            et_driver.setHint(context.getResources().getString(R.string.driver));


            img_driver = findViewById(R.id.img_driver);

            btn_add=findViewById(R.id.btnAdd);
            btn_add.setText(context.getResources().getString(R.string.add_buss));
            btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!et_bus_no.getText().toString().trim().equalsIgnoreCase("")) {
                        Add_Bus();
                    }else {
                        Toast.makeText(context,context.getResources().getString(R.string.fill_all_fields),Toast.LENGTH_LONG).show();
                    }

                }
            });

            get_Drivers();
            img_driver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog = builder.create();
                    dialog.show();
                }
            });

        } catch (Exception ex) {
        }
    }

    void get_Drivers() {
        loading.show();
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.driver));

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("IDs/").orderByChild("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("Type").getValue().toString().equalsIgnoreCase("d") &&
                                ds.child("Bus").getValue().toString().equalsIgnoreCase("")) {
                            temp.add(ds.child("Name").getValue().toString());
                        }
                    }
                    lst_driver = new String[temp.size()];
                    for (int i = 0; i < temp.size(); i++) {
                        lst_driver[i] = temp.get(i);
                    }

                    builder.setItems(lst_driver, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            et_driver.setText(lst_driver[which]);
                        }
                    });
                    loading.dismiss();
                } catch (Exception ex) {
                    loading.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loading.dismiss();
            }
        });
    }

    boolean flag=false;
    void Add_Bus(){
        loading.show();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("Bus/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    flag=false;
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        if (ds.getKey().equalsIgnoreCase(et_bus_no.getText().toString().trim())){
                           flag=true;
                        }
                    }
                    if(!flag){
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();

                        //DatabaseReference ref = database.getReference("Bus/" + et_bus_no.getText().toString().trim());
                        DatabaseReference ref = database.getReference("Bus").child(et_bus_no.getText().toString().trim());
                        String val= ref.push().getKey();
                        ref=ref.child(val);
                        ref.child("Driver").setValue(et_driver.getText().toString().trim());
                        ref.child("Source").setValue("");
                        ref.child("Destination").setValue("");
                        ref.child("Departure").setValue("");
                        ref.child("Distance").setValue("");
                        ref.child("CurrentLat").setValue("");
                        ref.child("CurrentLong").setValue("");
                        ref.child("Dest_name").setValue("");
                        ref.child("Source_name").setValue("");
                        ref.child("Full").setValue("0");

                        ref.push();
                        loading.dismiss();
                        DialogClass dialog = new DialogClass(context,
                                context.getResources().getString(R.string.message), context.getResources().getString(R.string.added));
                        dialog.show();

                        et_driver.setText("");
                        et_bus_no.setText("");
                    }else{
                        DialogClass dialog = new DialogClass(context,
                                context.getResources().getString(R.string.message), context.getResources().getString(R.string.already_add));
                        dialog.show();
                        loading.dismiss();
                    }

                }catch (Exception ex){
                    loading.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loading.dismiss();
            }
        });
    }
}