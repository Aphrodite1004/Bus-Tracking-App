package com.tracking.bustrackingsystem;


import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Adapter.CustomAdapterBus;
import com.tracking.bustrackingsystem.Adapter.CustomAdapterDriver;
import com.tracking.bustrackingsystem.Model.Model_Bus;
import com.tracking.bustrackingsystem.Model.Model_Driver;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.Locale;

public class BusFragment extends Fragment {
    ArrayList<Model_Bus> my_arr = new ArrayList<>();
    CustomAdapterBus adapter;
    private FloatingActionButton fab_Main;
    Context context;
    MyPrefs prefs;
    String user_type;
    RecyclerView rc;
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
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }
    public BusFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_bus, container, false);
        try{
            context=view.getContext();
            langPrefs=new LangPrefs(context);
            loadLocale();
            loading=new Dialog_Loading(context);
            prefs=new MyPrefs(context);
            user_type=prefs.get_Val("user_type");
            //user_type="d";
            rc =view.findViewById(R.id.rc);
            fab_Main = view.findViewById(R.id.floatingActionButton);
            if(user_type.equalsIgnoreCase("a")) {
                fab_Main.setVisibility(View.VISIBLE);
            }else {
                fab_Main.setVisibility(View.GONE);
            }
            fab_Main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, AddBusActivity.class));
                }
            });
            get_Busses();


        }catch (Exception ex){

        }
        return  view;
    }
    void get_Busses(){
        loading.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("Bus/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    my_arr.clear();
                    my_arr=new ArrayList<>();
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                            Model_Bus bus = new Model_Bus();
                            bus.setBus_no(ds.getKey());
                            my_arr.add(bus);
                    }
                    adapter = new CustomAdapterBus(context, my_arr);
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
