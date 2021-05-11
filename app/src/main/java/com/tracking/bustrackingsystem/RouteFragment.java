package com.tracking.bustrackingsystem;


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
import com.tracking.bustrackingsystem.Adapter.CustomAdapterRoute;
import com.tracking.bustrackingsystem.Model.Model_Bus;
import com.tracking.bustrackingsystem.Model.Model_Route;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.Locale;

public class RouteFragment extends Fragment {
    ArrayList<Model_Route> my_arr = new ArrayList<>();
    CustomAdapterRoute adapter;
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
    public RouteFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_route, container, false);
        try{
            context=view.getContext();
            langPrefs=new LangPrefs(context);
            loadLocale();
            prefs=new MyPrefs(context);
            loading=new Dialog_Loading(context);
            user_type=prefs.get_Val("user_type");
            //user_type="d";
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
                    Intent i=new Intent(context,AddRouteActivity.class);
                    i.putExtra("from","fab");
                    startActivity(i);
                }
            });
            get_Route();
            /*for(int i=0;i<10;i++){
                Model_Route driver=new Model_Route();
                my_arr.add(driver);
            }
            adapter = new CustomAdapterRoute(context, my_arr);
            rc.setLayoutManager(new LinearLayoutManager(context));
            rc.setAdapter(adapter);*/
        }catch (Exception ex){

        }
        return view;
    }

    void get_Route(){
        loading.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("Bus/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    my_arr.clear();
                    my_arr=new ArrayList<>();
                    String bus_no="";
                    for(DataSnapshot ds_bus_no: dataSnapshot.getChildren()) {
                        bus_no=ds_bus_no.getKey();
                        for (DataSnapshot ds_route : ds_bus_no.getChildren()) {
                            Model_Route bus = new Model_Route();
                            bus.setBus_no(bus_no);
                            bus.setSource_name(ds_route.child("Source_name").getValue().toString());
                            bus.setDest_name(ds_route.child("Dest_name").getValue().toString());
                            bus.setDeparture(ds_route.child("Departure").getValue().toString());
                            bus.setDistance(ds_route.child("Distance").getValue().toString());
                            bus.setDriver(ds_route.child("Driver").getValue().toString());
                            bus.setSource(ds_route.child("Source").getValue().toString());
                            bus.setDest(ds_route.child("Destination").getValue().toString());
                            bus.setDb_key(ds_route.getKey());
                            my_arr.add(bus);
                        }

                    }
                    adapter = new CustomAdapterRoute(context, my_arr);
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
