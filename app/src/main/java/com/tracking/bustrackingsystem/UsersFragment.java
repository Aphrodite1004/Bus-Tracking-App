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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Adapter.CustomAdapterUser;
import com.tracking.bustrackingsystem.Model.Model_User;
import com.tracking.bustrackingsystem.Model.User;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    Context context;
    ArrayList<Model_User> my_arr = new ArrayList<>();
    CustomAdapterUser adapter;
    MyPrefs prefs;
    String user_type;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
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
    public UsersFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        try {
            context = view.getContext();
            langPrefs=new LangPrefs(context);
            loadLocale();
            rc= view.findViewById(R.id.rc);
            loading=new Dialog_Loading(context);
            get_Users();
        } catch (Exception ex) {

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
                        if(ds.child("Type").getValue().toString().equalsIgnoreCase("u") ){
                            Model_User user = new Model_User();
                            user.name = ds.child("Name").getValue().toString();
                            user.id = ds.getKey();
                            my_arr.add(user);
                        }
                    }
                    adapter = new CustomAdapterUser(context, my_arr);
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
