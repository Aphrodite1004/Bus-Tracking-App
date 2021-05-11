package com.tracking.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Model.User;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Fragment fragment;
    Context context;
    private FirebaseAuth mAuth;
    BottomNavigationView bottomNavigationView;
    Menu nav_Menu;
    MenuItem bus, driver, route, move, track;
    String user_type="";
    MyPrefs prefs;
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
        setContentView(R.layout.activity_main);
        try {
            context = MainActivity.this;
            langPrefs=new LangPrefs(context);
            loadLocale();
            mAuth = FirebaseAuth.getInstance();
            prefs=new MyPrefs(context);
            fragment = new TrackFragment(context);
            loadFragment(fragment);
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            user_type=prefs.get_Val("user_type");


            bottomNavigationView = findViewById(R.id.bottomView);
            bottomNavigationView.getMenu().findItem(R.id.nav_bus).setTitle(context.getResources().getString(R.string.buses));
            bottomNavigationView.getMenu().findItem(R.id.nav_driver).setTitle(context.getResources().getString(R.string.driver));
            bottomNavigationView.getMenu().findItem(R.id.nav_route).setTitle(context.getResources().getString(R.string.route));
            bottomNavigationView.getMenu().findItem(R.id.nav_track).setTitle(context.getResources().getString(R.string.track));
            if (user_type.equalsIgnoreCase("a")) {
                bottomNavigationView.getMenu().findItem(R.id.nav_move).setTitle(context.getResources().getString(R.string.users));
            } else {
                bottomNavigationView.getMenu().findItem(R.id.nav_move).setTitle(context.getResources().getString(R.string.go_to));
            }

            initialize_bottom(user_type);

            bottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.nav_bus:
                                    fragment = new BusFragment();
                                    loadFragment(fragment);
                                    return true;
                                case R.id.nav_driver:
                                    fragment = new DriverFragment();
                                    loadFragment(fragment);
                                    return true;
                                case R.id.nav_route:
                                    fragment = new RouteFragment();
                                    loadFragment(fragment);
                                    return true;
                                case R.id.nav_move:
                                    if (user_type.equalsIgnoreCase("a")) {
                                        fragment = new UsersFragment();
                                    } else {
                                        if (isGPSEnabled()) {
                                            fragment = new GoToFragment(context);
                                        } else {
                                            DialogClass dialog = new DialogClass(context,
                                                    context.getResources().getString(R.string.message), "Please Turn on GPS");
                                            dialog.show();
                                        }
                                    }
                                    loadFragment(fragment);

                                    return true;
                                case R.id.nav_track:
                                    fragment = new TrackFragment(context);
                                    loadFragment(fragment);
                                    return true;
                            }
                            return true;
                        }
                    });
        } catch (Exception ex) {
        }
    }

    private void initialize_bottom(String val) {
        try {
            nav_Menu = bottomNavigationView.getMenu();
            bus = nav_Menu.findItem(R.id.nav_bus);
            driver = nav_Menu.findItem(R.id.nav_driver);
            route = nav_Menu.findItem(R.id.nav_route);
            move = nav_Menu.findItem(R.id.nav_move);
            if(val.equalsIgnoreCase("a")) {
                Bottom_Admin();
            }else if(val.equalsIgnoreCase("u")) {
                Bottom_User();
            }else if(val.equalsIgnoreCase("d")) {
                Bottom_Driver();
            }else if(val.equalsIgnoreCase("g")) {
                Bottom_Guest();
            }
        } catch (Exception ex) {
        }
    }

    public boolean isGPSEnabled (){
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    private void Bottom_Guest() {
        bus.setVisible(true);
        driver.setVisible(false);
        route.setVisible(true);
        move.setVisible(false);
        track.setVisible(true);
    }
    private void Bottom_Driver() {
        bus.setVisible(true);
        driver.setVisible(false);
        route.setVisible(true);
        move.setVisible(true);
        track.setVisible(false);
    }

    private void Bottom_User() {
        bus.setVisible(true);
        driver.setVisible(false);
        route.setVisible(true);
        move.setVisible(false);
        track.setVisible(true);
    }

    private void Bottom_Admin() {
        bus.setVisible(true);
        driver.setVisible(true);
        route.setVisible(true);
        move.setTitle(context.getResources().getString(R.string.users));
        move.setIcon(R.drawable.ic_person_black_24dp);
        move.setVisible(true);
        track.setVisible(true);
    }

    private void loadFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout_main, fragment);
            transaction.commit();
        } catch (Exception ex) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (user_type.equalsIgnoreCase("a")) {
            getMenuInflater().inflate(R.menu.menu_admin, menu);
            MenuItem itm_change_pwd = menu.findItem(R.id.nav_change_pwd);

            itm_change_pwd.setTitle(context.getResources().getString(R.string.change_password));

            MenuItem itm_language = menu.findItem(R.id.nav_language);
            itm_language.setTitle(context.getResources().getString(R.string.change_language));

            MenuItem itm_contact_us = menu.findItem(R.id.nav_contact_us);
            itm_contact_us.setTitle(context.getResources().getString(R.string.contact_us));

            MenuItem itm_sign_out = menu.findItem(R.id.nav_sign_out);
            itm_sign_out.setTitle(context.getResources().getString(R.string.sign_out));
        } else if (user_type.equalsIgnoreCase("u")) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            MenuItem itm_profile = menu.findItem(R.id.nav_profile);
            itm_profile.setTitle(context.getResources().getString(R.string.profile));
            MenuItem itm_change_pwd = menu.findItem(R.id.nav_change_pwd);

            itm_change_pwd.setTitle(context.getResources().getString(R.string.change_password));

            MenuItem itm_language = menu.findItem(R.id.nav_language);
            itm_language.setTitle(context.getResources().getString(R.string.change_language));

            MenuItem itm_contact_us = menu.findItem(R.id.nav_contact_us);
            itm_contact_us.setTitle(context.getResources().getString(R.string.contact_us));

            MenuItem itm_sign_out = menu.findItem(R.id.nav_sign_out);
            itm_sign_out.setTitle(context.getResources().getString(R.string.sign_out));
        } else if (user_type.equalsIgnoreCase("d")) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            MenuItem itm_profile = menu.findItem(R.id.nav_profile);
            itm_profile.setTitle(context.getResources().getString(R.string.profile));
            MenuItem itm_change_pwd = menu.findItem(R.id.nav_change_pwd);

            itm_change_pwd.setTitle(context.getResources().getString(R.string.change_password));

            MenuItem itm_language = menu.findItem(R.id.nav_language);
            itm_language.setTitle(context.getResources().getString(R.string.change_language));

            MenuItem itm_contact_us = menu.findItem(R.id.nav_contact_us);
            itm_contact_us.setTitle(context.getResources().getString(R.string.contact_us));

            MenuItem itm_sign_out = menu.findItem(R.id.nav_sign_out);
            itm_sign_out.setTitle(context.getResources().getString(R.string.sign_out));
        } else if (user_type.equalsIgnoreCase("g")) {
            getMenuInflater().inflate(R.menu.menu_guest, menu);

            MenuItem itm_language = menu.findItem(R.id.nav_language);
            itm_language.setTitle(context.getResources().getString(R.string.change_language));

            MenuItem itm_contact_us = menu.findItem(R.id.nav_contact_us);
            itm_contact_us.setTitle(context.getResources().getString(R.string.contact_us));

            MenuItem itm_sign_out = menu.findItem(R.id.nav_sign_out);
            itm_sign_out.setTitle(context.getResources().getString(R.string.sign_out));
        }
        return true;
    }
    int checkedItem=0;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_sign_out) {

            mAuth.signOut();
            prefs.Clear_Pref();
            super.onBackPressed();
        }
        if (id == R.id.nav_profile) {
            if(user_type.equalsIgnoreCase("d")){
                startActivity(new Intent(context, DriverProfileActivity.class));
            }
            if(user_type.equalsIgnoreCase("u")){
                startActivity(new Intent(context, UserProfileActivity.class));
            }
        }
        if (id == R.id.nav_language) {
            langPrefs = new LangPrefs(context);
            String lang=langPrefs.get_Val("l");
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getResources().getString(R.string.choose_the_language));

            String[] animals = {"English", "اَلْعَرَبِيَّةُ"};
            if(lang.equalsIgnoreCase("en")) {
                checkedItem=0;
            }else if(lang.equalsIgnoreCase("ar")) {
                checkedItem=1;
            }
            builder.setSingleChoiceItems(animals, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkedItem=which;
                }
            });
            builder.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    langPrefs=new LangPrefs(context);
                    if(checkedItem==0) {
                        langPrefs.put_Val("l", "en");
                    }else if(checkedItem==1) {
                        langPrefs.put_Val("l", "ar");
                    }
                    Intent intent = new Intent(context, SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                    Runtime.getRuntime().exit(0);
                }
            });
            builder.setNegativeButton(context.getResources().getString(R.string.cancel), null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if (id == R.id.nav_change_pwd) {
            startActivity(new Intent(context, RecoverPwdActivity.class));
        }
        if (id == R.id.nav_contact_us) {
            try {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "ejunaid@taibahu.edu.sa", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                context.startActivity(Intent.createChooser(emailIntent, null));

                //startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("ejunaid@taibahu.edu.sa")));
            }catch (Exception ex){

            }

            /*try {
                final Dialog dialog_contact = new Dialog(context);
                dialog_contact.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog_contact.setCancelable(true);
                dialog_contact.setContentView(R.layout.dialog_contact);
                Window window = dialog_contact.getWindow();
                window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
                TextView tv_title = dialog_contact.findViewById(R.id.title);
                tv_title.setText(context.getResources().getString(R.string.help_and_support));
                TextView tv_call = dialog_contact.findViewById(R.id.tv_call);
                tv_call.setText(context.getResources().getString(R.string.call));
                TextView tv_email = dialog_contact.findViewById(R.id.tv_email);
                tv_email.setText(context.getResources().getString(R.string.send_email));
                tv_call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_contact.dismiss();
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + "123456789"));
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(intent);
                        }
                    }
                });
                tv_email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog_contact.dismiss();
                        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("ejunaid@taibahu.edu.sa")));
                    }
                });
                dialog_contact.show();
            }catch (Exception ex){
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}