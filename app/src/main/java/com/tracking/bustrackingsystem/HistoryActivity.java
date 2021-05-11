package com.tracking.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Adapter.CustomAdapterBus;
import com.tracking.bustrackingsystem.Adapter.CustomAdapterHistory;
import com.tracking.bustrackingsystem.Model.Model_Bus;
import com.tracking.bustrackingsystem.Model.Model_History;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    ArrayList<Model_History> my_arr = new ArrayList<>();
    CustomAdapterHistory adapter;
    Context context;
    String bus="";
    Dialog_Loading loading;
    RecyclerView rc;
    TextView tv_no_histroy;
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
        setContentView(R.layout.activity_history);
        try{
            context=HistoryActivity.this;
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            langPrefs=new LangPrefs(context);
            loadLocale();
            loading=new Dialog_Loading(context);
            tv_no_histroy=findViewById(R.id.tv_no_history);
            tv_no_histroy.setText(context.getResources().getString(R.string.no_history));
            bus=getIntent().getStringExtra("bus");
            rc=findViewById(R.id.rc);
            get_history(bus);
        }catch (Exception ex){

        }
    }
    void get_history(String bus){
        loading.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("History/").child(bus).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    my_arr.clear();
                    my_arr=new ArrayList<>();
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        Model_History bus = new Model_History();
                        bus.setDriver(ds.child("Driver").getValue().toString());
                        bus.setSource(ds.child("StartPosition").getValue().toString().trim());
                        bus.setDestination(ds.child("EndPosition").getValue().toString().trim());
                        bus.setStart(ds.child("StartDate").getValue().toString());
                        bus.setEnd(ds.child("EndDate").getValue().toString());
                        my_arr.add(bus);
                    }
                    if(my_arr.size()>0){
                        tv_no_histroy.setVisibility(View.GONE);
                        rc.setVisibility(View.VISIBLE);
                    }
                    adapter = new CustomAdapterHistory(context, my_arr);
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
