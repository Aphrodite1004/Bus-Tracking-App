package com.tracking.bustrackingsystem;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Model.Model_Route;
import com.tracking.bustrackingsystem.Model.User;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class GoToFragment extends Fragment implements LocationListener {
    EditText et_destination, et_source;
    TextView et_Route;
    FirebaseDatabase database;
Switch swtch_full;
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.permission_was_granted),Toast.LENGTH_LONG).show();


                } else {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.permission_denied),
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    Context context;

    public GoToFragment(Context con) {
        this.context = con;

        // Required empty public constructor
    }

    MyPrefs prefs;

    Button startDrive,btn_end_drive;

    /******************************************************/

    String BUS_ASSIGNED="";
    void getBus() {
        loading = new Dialog_Loading(context);
        loading.show();
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
                            if (user.type.equals("d") && user.email.equals(prefs.get_Val("email"))) {
                                user.busid = ds.child("Bus").getValue().toString();
                                BUS_ASSIGNED=ds.child("Bus").getValue().toString();
                            } else {
                                user.busid = "";
                            }
                            list.add(user);
                        }
                    }
                    String busid = "";
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).email.equalsIgnoreCase(prefs.get_Val("email"))) {

                            busid = list.get(i).busid;
                        }
                    }
                    prefs.put_Val("Bus", busid);
                    getBusDetails();

                } catch (Exception ex) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    AlertDialog.Builder dialog_route;

    boolean switch_flag=false;
    ArrayList<Model_Route> lst_route;
    String[] string_arr_routes;
    String FINAL_ROUTE_KEY="";

    void getBusDetails() {
        final String bus = prefs.get_Val("Bus");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        lst_route=new ArrayList<>();
        dialog_route = new AlertDialog.Builder(context);
        dialog_route.setTitle(context.getResources().getString(R.string.select_route));

        database.getReference("Bus/" + bus).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {

                for (DataSnapshot dataSnapshot : ds.getChildren()) {
                    String route_key = dataSnapshot.getKey();
                    if (!route_key.equals("")) {

                        Model_Route model_route = new Model_Route();
                        model_route.setBus_no(bus);
                        //model_route.setDriver(dataSnapshot.child("Driver").getValue().toString());
                        model_route.setDb_key(route_key);
                        model_route.setSource(dataSnapshot.child("Source").getValue().toString());
                        model_route.setDest(dataSnapshot.child("Destination").getValue().toString());
                        model_route.setSource_name(dataSnapshot.child("Source_name").getValue().toString());
                        model_route.setDest_name(dataSnapshot.child("Dest_name").getValue().toString());
                        model_route.setBus_full(dataSnapshot.child("Full").getValue().toString());

                        lst_route.add(model_route);


                        /*prefs.put_Val("Source", dataSnapshot.child("Source").getValue().toString());
                        prefs.put_Val("Destination", dataSnapshot.child("Destination").getValue().toString());
                        if (dataSnapshot.child("Full").getValue().equals("1")) {
                            swtch_full.setChecked(true);
                            switch_flag = true;
                        } else {
                            swtch_full.setChecked(false);
                            switch_flag = true;
                        }

                        et_source.setText(dataSnapshot.child("Source_name").getValue().toString());
                        et_destination.setText(dataSnapshot.child("Dest_name").getValue().toString());
                        startDrive.setBackground(getResources().getDrawable(R.drawable.on_click_btn_change_color));*/

                    }
                }
                string_arr_routes=new String[lst_route.size()];
                for(int i=0;i<lst_route.size();i++){
                    string_arr_routes[i]=lst_route.get(i).getSource_name()+" TO "+ lst_route.get(i).getDest_name();
                }
                //for(int i=0;i<lst_route.size();i++){
                    et_Route.setText(lst_route.get(0).getSource_name()+" TO "+ lst_route.get(0).getDest_name());
                    FINAL_ROUTE_KEY=lst_route.get(0).getDb_key();

                if (lst_route.get(0).bus_full.equals("1")) {
                    swtch_full.setChecked(true);
                    switch_flag = true;
                } else {
                    swtch_full.setChecked(false);
                    switch_flag = true;
                }
                //}
                startDrive.setBackground(getResources().getDrawable(R.drawable.on_click_btn_change_color));
                loading.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loading.dismiss();
            }
        });

    }
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

    public boolean isGPSEnabled (Context mContext){
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    int CURENT_SELECTED=0;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_go_to, container, false);
        try {
            context = view.getContext();
            langPrefs=new LangPrefs(context);
            loadLocale();
            database= FirebaseDatabase.getInstance();
            prefs = new MyPrefs(getContext());
            et_destination = view.findViewById(R.id.et_destination);
            et_Route = view.findViewById(R.id.et_Route);
            et_Route.setHint(context.getResources().getString(R.string.route));
            et_Route.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_route.setItems(string_arr_routes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CURENT_SELECTED=which;
                            et_Route.setText(string_arr_routes[which]);
                            FINAL_ROUTE_KEY=lst_route.get(which).getDb_key();
                            if (lst_route.get(which).bus_full.equals("1")) {
                                swtch_full.setChecked(true);
                                switch_flag = true;
                            } else {
                                swtch_full.setChecked(false);
                                switch_flag = true;
                            }

                           /* String lat_source=lst_route.get(which).getSource();
                            String lat_destination=lst_route.get(which).getDest();
                            Toast.makeText(context,lat_source+","+lat_destination,Toast.LENGTH_SHORT).show();*/

                            prefs.put_Val("Source", lst_route.get(which).getSource());
                            prefs.put_Val("Destination", lst_route.get(which).getDest());
                        }
                    });

                    // create and show the alert dialog
                    AlertDialog dialog = dialog_route.create();
                    dialog.show();
                }
            });
            et_destination.setHint(context.getResources().getString(R.string.destination));
            et_source = view.findViewById(R.id.et_source);
            et_source.setHint(context.getResources().getString(R.string.source));

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            swtch_full =view.findViewById(R.id.switch_full);
            swtch_full.setText(context.getResources().getString(R.string.mark_full));
            swtch_full.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(swtch_full.isChecked()){
                        Model_Route replacable_mode=new Model_Route();
                        replacable_mode.bus_full="1";
                        replacable_mode.setBus_no(lst_route.get(CURENT_SELECTED).getBus_no());
                        replacable_mode.setDb_key(lst_route.get(CURENT_SELECTED).getDb_key());
                        replacable_mode.setDest(lst_route.get(CURENT_SELECTED).getDest());
                        replacable_mode.setDest_name(lst_route.get(CURENT_SELECTED).getDest_name());
                        replacable_mode.setSource_name(lst_route.get(CURENT_SELECTED).getSource_name());
                        replacable_mode.setSource(lst_route.get(CURENT_SELECTED).getSource());



                        lst_route.set(CURENT_SELECTED,replacable_mode);
                        database.getReference("Bus/").child(BUS_ASSIGNED).child(FINAL_ROUTE_KEY).child("Full").setValue("1");
                        if(switch_flag) {
                            Toast.makeText(context, context.getResources().getString(R.string.updated_as_full), Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!swtch_full.isChecked()){
                        Model_Route replacable_mode=new Model_Route();
                        replacable_mode.bus_full="0";
                        replacable_mode.setBus_no(lst_route.get(CURENT_SELECTED).getBus_no());
                        replacable_mode.setDb_key(lst_route.get(CURENT_SELECTED).getDb_key());
                        replacable_mode.setDest(lst_route.get(CURENT_SELECTED).getDest());
                        replacable_mode.setDest_name(lst_route.get(CURENT_SELECTED).getDest_name());
                        replacable_mode.setSource(lst_route.get(CURENT_SELECTED).getSource());
                        replacable_mode.setSource_name(lst_route.get(CURENT_SELECTED).getSource_name());
                        lst_route.set(CURENT_SELECTED,replacable_mode);
                        database.getReference("Bus/").child(BUS_ASSIGNED).child(FINAL_ROUTE_KEY).child("Full").setValue("0");
                        if(switch_flag) {
                            Toast.makeText(context, context.getResources().getString(R.string.updated_as_empty), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
/*            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(swtch_full.isChecked()){
                        //Toast.makeText(context,BUS_ASSIGNED,Toast.LENGTH_LONG).show();
                        database.getReference("Bus/").child(BUS_ASSIGNED).child(FINAL_ROUTE_KEY).child("Full").setValue("1");
                        if(switch_flag) {
                            Toast.makeText(context, context.getResources().getString(R.string.updated_as_full), Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!swtch_full.isChecked()){
                        database.getReference("Bus/").child(BUS_ASSIGNED).child(FINAL_ROUTE_KEY).child("Full").setValue("0");
                        if(switch_flag) {
                            Toast.makeText(context, context.getResources().getString(R.string.updated_as_empty), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }*/);

            getBus();

            //getCurrentLocation();
            startDrive = (Button) view.findViewById(R.id.btnRegister);
            startDrive.setText(context.getResources().getString(R.string.start_ride));
            btn_end_drive= (Button) view.findViewById(R.id.btnEnd);
            btn_end_drive.setText(context.getResources().getString(R.string.end_ride));
            btn_end_drive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefs=new MyPrefs(context);
                    prefs.put_Val("loc","end");
                    PackageManager pm  = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, OurBroadcastReceiver.class);
                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                    Intent background = new Intent(context, LocationService.class);
                    context.stopService(background);


                    btn_end_drive.setBackground(getResources().getDrawable(R.drawable.on_click_btn_change_color_before));
                    AddtoHistory(false);


                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("Bus/").child(prefs.get_Val("Bus")).child(FINAL_ROUTE_KEY);
                    ref.child("CurrentLat").setValue("0.0");
                    ref.child("CurrentLong").setValue("0.0");

                    DialogClass dialog = new DialogClass(context,
                            context.getResources().getString(R.string.ride_is_ended),
                            context.getResources().getString(R.string.started_at)+": "+msg_start_time.trim()+"\n"+
                                    context.getResources().getString(R.string.started_at)+": "+msg_end_time.trim()+"\n"+
                                    context.getResources().getString(R.string.source)+": "+msg_start_position.trim()+"\n"+
                                    context.getResources().getString(R.string.destination)+": "+msg_end_position.trim()+"\n"+
                                    context.getResources().getString(R.string.ended_at)+": "+msg_end_time);
                    dialog.show();
                }
            });
            startDrive.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    try {
                        prefs=new MyPrefs(context);
                        prefs.put_Val("loc","start");
                        prefs.put_Val("FINAL_ROUTE_KEY",FINAL_ROUTE_KEY);

                        startDrive.setEnabled(false);
                        startDrive.setBackground(getResources().getDrawable(R.drawable.on_click_btn_change_color_before));
                        if (isGPSEnabled(context)) {
                            AddtoHistory(true);
                            btn_end_drive.setEnabled(true);
                            btn_end_drive.setBackground(getResources().getDrawable(R.drawable.on_click_btn_change_color));
                            Intent background = new Intent(context, LocationService.class);
                            //Intent background = new Intent(context, GoogleService.class);
                            Log.e("AlarmReceive ", "testing called broadcast called");
                            //String lat_long="";

                        /*locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        }
                        Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);*/

                            //lat_long = location1.getLatitude() + "," + location1.getLongitude() + "";
                       /* GPSTracker gps;
                        gps = new GPSTracker(context);
                        if(gps.canGetLocation()) {

                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();
                            lat_long=latitude+","+longitude;
                        } else {

                            gps.showSettingsAlert();
                        }*/
                            context.startService(background);

                            String[] loc = prefs.get_Val("Destination").split(",");
                            Double a = Double.parseDouble(loc[0]);
                            Double b = Double.parseDouble(loc[1]);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + a + "," + b + "");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startDrive.setEnabled(false);
                            startActivity(mapIntent);
                        } else {
                            DialogClass dialog = new DialogClass(context,
                                    context.getResources().getString(R.string.message), context.getResources().getString(R.string.please_turn_on_gps));
                            dialog.show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });



      /*  LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            checkAndRequestRunTimePermissions();
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,this);
*/
        }catch (Exception ex){
            Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
        return view;
    }

    private double[] latLng = new double[2];
    LatLng driverLatLng;

    @Override
    public void onResume() {
        super.onResume();
    }
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                //Log.w("My Current loction address", strReturnedAddress.toString());
            } else {
                //Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }
    String msg_start_time="",msg_driver="",msg_end_time="",msg_end_position="",msg_start_position="";

    String val="";
    void AddtoHistory(Boolean start) {
        try {
            GPSTracker gps;
            gps = new GPSTracker(context);
            if (gps.canGetLocation()) {
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                prefs = new MyPrefs(context);
                String name = prefs.get_Val("name");
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd  'at' HH:mm:ss ");
                String currentDateandTime = sdf.format(new Date());
                String uid = prefs.get_Val("Bus");
                DatabaseReference ref = database.getReference("History/").child(uid);


                if (start) {
                    val= ref.push().getKey();
                    ref = ref.child(val);
                    ref.child("Date").setValue(currentDateandTime);
                    ref.child("Driver").setValue(name);
                    ref.child("StartDate").setValue(currentDateandTime);
                    ref.child("StartPosition").setValue(getCompleteAddressString(latitude, longitude));
                    ref.child("EndDate").setValue("");
                    ref.child("EndPosition").setValue("");
                    msg_start_time=currentDateandTime;
                    msg_driver=name;
                    msg_start_position=getCompleteAddressString(latitude, longitude);

                } else {
                    ref = ref.child(val);
                    ref.child("EndDate").setValue(currentDateandTime);
                    ref.child("EndPosition").setValue(getCompleteAddressString(latitude, longitude));
                    msg_end_time=currentDateandTime;
                    msg_end_position=getCompleteAddressString(latitude, longitude);
                }
                ref.push();
            } else {
                gps.showSettingsAlert();
            }
        } catch (Exception ex) {

        }
    }

    public void updateLocation(Location location) {
        latLng[0] = location.getLatitude();
        latLng[1] = location.getLongitude();

     /*   if(marker==null){
            marker = new PicassoMarker(googleMapHomeFrag.addMarker(new MarkerOptions().position(new LatLng(latLng[0], latLng[1]))));
            Picasso.with(context).load(R.mipmap.car).resize( 100,  100)
                    .into(marker);
            googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng[0], latLng[1]), 12.0f));
        }*/

        if ((latLng[0] != -1 && latLng[0] != 0) && (latLng[1] != -1 && latLng[1] != 0)) {
            //googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 12.0f));
            //float bearing = (float) bearingBetweenLocations(driverLatLng, new LatLng(location.getLatitude(), location.getLongitude()));
            /*if (marker != null) {
                moveVechile(marker.getmMarker(), location);
                rotateMarker(marker.getmMarker(), location.getBearing(), start_rotation);
            }*/
            driverLatLng = new LatLng(latLng[0], latLng[1]);
            SetDriverFirebase();
            // writeFileOnInternalStorage(MainActivity.this,"map.txt","Loc:"+driverLatLng+"\n");
        } else {
            //Toast.makeText(context, "Location Not Found", Toast.LENGTH_LONG).show();
        }
    }

    void SetDriverFirebase() {
        String email = prefs.get_Val("email");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        String uid = prefs.get_Val("Bus");
        DatabaseReference ref = database.getReference("Bus/" + uid);
        ref.child("Driver").setValue(email);
        ref.child("CurrentLat").setValue(latLng[0]);
        ref.child("CurrentLong").setValue(latLng[1]);
        ref.push();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            String strLocation =
                    DateFormat.getTimeInstance().format(location.getTime()) + "\n" +
                            "Latitude=" + location.getLatitude() + "\n" +
                            "Longitude=" + location.getLongitude();
            Toast.makeText(getContext(), strLocation, Toast.LENGTH_LONG).show();
            updateLocation(location);

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    Dialog_Loading loading;


}
