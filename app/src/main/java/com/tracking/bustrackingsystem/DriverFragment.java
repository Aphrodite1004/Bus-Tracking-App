package com.tracking.bustrackingsystem;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Adapter.CustomAdapterDriver;
import com.tracking.bustrackingsystem.Model.Model_Driver;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class DriverFragment extends Fragment {
    private FloatingActionButton fab_Main;
    Context context;
    ArrayList<Model_Driver> my_arr = new ArrayList<>();
    CustomAdapterDriver adapter;
    MyPrefs prefs;
    String user_type;
    Dialog_Loading loading;
    RecyclerView rc;
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
    public DriverFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_driver, container, false);
        try{
            context=view.getContext();
            prefs=new MyPrefs(context);
            loading=new Dialog_Loading(context);

            langPrefs=new LangPrefs(context);
            loadLocale();
            user_type=prefs.get_Val("user_type");
            //user_type="a";
            rc=view.findViewById(R.id.rc);
            fab_Main = view.findViewById(R.id.floatingActionButton);
            if(user_type.equalsIgnoreCase("a")) {
                fab_Main.setVisibility(View.VISIBLE);
            }else {
                fab_Main.setVisibility(View.GONE);
            }
            fab_Main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(context,AddDriverActivity.class));
                }
            });
            get_Users();
        }catch (Exception ex){
            Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
        return view;
    }
    void get_Users(){
        loading.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("IDs/").orderByChild("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    my_arr.clear();
                    my_arr=new ArrayList<>();
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        if(ds.child("Type").getValue().toString().equalsIgnoreCase("d") ){
                            if(ds.child("status").getValue().toString().equalsIgnoreCase("1")) {
                                Model_Driver driver = new Model_Driver();
                                driver.name = ds.child("Name").getValue().toString();
                                driver.id = ds.getKey();
                                my_arr.add(driver);
                            }
                        }
                    }
                    adapter = new CustomAdapterDriver(context, my_arr);
                    rc.setLayoutManager(new LinearLayoutManager(context));
                    rc.setAdapter(adapter);
                    loading.dismiss();
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
