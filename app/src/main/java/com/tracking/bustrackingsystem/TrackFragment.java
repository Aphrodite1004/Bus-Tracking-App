package com.tracking.bustrackingsystem;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tracking.bustrackingsystem.Model.Model_Bus;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.PicassoMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class TrackFragment extends Fragment {
    Context context;

    TrackFragment(Context cont) {
        this.context = cont;
    }

    private int REQUEST_CODE_PERMISSION_MULTIPLE = 123;
    private GoogleMap googleMapHomeFrag;
    private double[] latLng = new double[2];
    private LatLng driverLatLng;
    private PicassoMarker marker;
    private boolean isDeninedRTPs = true;       // initially true to prevent anim(2)
    private boolean showRationaleRTPs = false;
    private float start_rotation;


    private void checkAndRequestRunTimePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_PERMISSION_MULTIPLE);

            }
        }


        // onRunTimePermissionGranted();
    }

    Button btnTrack;
    TextView edtbusid;
    MapView mMapView;
    TextView tv_lat_lng;
    LangPrefs langPrefs;
    private static final String Locale_KeyValue = "l";

    public void loadLocale() {
        String language = langPrefs.get_Val(Locale_KeyValue);
        setLanguageForApp(language);
    }

    private void setLanguageForApp(String languageToLoad) {
        Locale locale;
        if (languageToLoad.equals("")) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(languageToLoad);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    TextView et_bus_route;
    // private GoogleMap googleMap;
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_track, container, false);
        try {
            langPrefs = new LangPrefs(context);


            et_bus_route=view.findViewById(R.id.et_bus_route);
            et_bus_route.setHint(context.getResources().getString(R.string.route));
            et_bus_route.setText("");
            loadLocale();
            tv_lat_lng = view.findViewById(R.id.tv_lat_lng);
            btnTrack = (Button) view.findViewById(R.id.btnTrackBus);
            btnTrack.setText(context.getResources().getString(R.string.track));
            edtbusid = (TextView) view.findViewById(R.id.et_busid);
            edtbusid.setHint(context.getResources().getString(R.string.bus_number));
            btnTrack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!edtbusid.getText().toString().equals("") &&
                    !et_bus_route.getText().toString().equals("") ) {
                        SetDriverFirebase(edtbusid.getText().toString());
                    }else {
                        DialogClass dialogClass=new DialogClass(context,
                                context.getResources().getString(R.string.message),
                                context.getResources().getString(R.string.select_bus_route));
                        dialogClass.show();
                    }
                }
            });
            get_Drivers();
            edtbusid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        SELECTED_ROUTE_KEY="";
                        dialog = builder.create();
                        dialog.show();
                    } catch (Exception ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            et_bus_route.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String valll=edtbusid.getText().toString().trim();
                        if(!valll.equals("")) {
                            SELECTED_ROUTE_KEY="";
                            dialog_route = builder_route.create();
                            dialog_route.show();
                        }else {
                            DialogClass dialogClass=new DialogClass(context,
                                    context.getResources().getString(R.string.message),
                                    context.getResources().getString(R.string.select_bus));
                            dialogClass.show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

       /* try{

            checkAndRequestRunTimePermissions();
            getCurrentLocation();
            initMap();
        }catch (Exception ex){

        }*/
            checkAndRequestRunTimePermissions();
            mMapView = (MapView) view.findViewById(R.id.map);
            try {
                mMapView.onCreate(savedInstanceState);
                mMapView.onResume(); // needed to get the map to display immediately
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                MapsInitializer.initialize(context.getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMapHomeFrag = mMap;

                    // For showing a move to my location button
                    googleMapHomeFrag.setMyLocationEnabled(true);

                    // For dropping a marker at a point on the Map
                    //LatLng sydney = new LatLng(-34, 151);
                    driverLatLng = new LatLng(24.4714392, 39.3373543);

                    //googleMapHomeFrag.addMarker(new MarkerOptions().position(driverLatLng).title("Driver Location").snippet("Marker Description"));

                    // For zooming automatically to the location of the marker
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(driverLatLng).zoom(9).build();
                    googleMapHomeFrag.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                }
            });
        } catch (Exception ex) {

        }

        return view;
    }


    private void onRunTimePermissionGranted() {

        isDeninedRTPs = false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_MULTIPLE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isDeninedRTPs = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showRationaleRTPs = shouldShowRequestPermissionRationale(permission);
                        }

                        break;
                    }

                }
                onRunTimePermissionDenied();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /******************************************************/

    private void onRunTimePermissionDenied() {
        if (isDeninedRTPs) {
            if (!showRationaleRTPs) {
                //goToSettings();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSION_MULTIPLE);
                }
            }
        } else {
            onRunTimePermissionGranted();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        /*  if (locationUtilObj != null /*&& !locationUtilObj.isGoogleAPIConnected()*///) {
         /*   locationUtilObj.checkLocationSettings();
            locationUtilObj.restart_location_update();
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /*
    private void initMap() {
        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);

        mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (googleMap != null) {
                    googleMapHomeFrag = googleMap;
                    googleMapHomeFrag.getUiSettings().setAllGesturesEnabled(true);
                    googleMapHomeFrag.getUiSettings().setScrollGesturesEnabled(true);
                    googleMapHomeFrag.getUiSettings().setCompassEnabled(true);
                    googleMapHomeFrag.getUiSettings().setMapToolbarEnabled(true);

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    googleMapHomeFrag.setMyLocationEnabled(true);
                    googleMapHomeFrag.getUiSettings().setMyLocationButtonEnabled(true);

                    if(null != driverLatLng){
                        if (googleMapHomeFrag != null) {
                            googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 12.0f));
                            googleMapHomeFrag.getUiSettings().setZoomControlsEnabled(true);
                        }
                    }


                }
            }
        });
    }
*/


    void SetDriverFirebase(final String Busid) {
        //  String email=prefs.get_Val("email");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();*/
        //   ref.child("UserEmail").setValue(email);
        //setValue( latLng[0]);

        database.getReference("Bus/" + Busid).child(SELECTED_ROUTE_KEY).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    //for (DataSnapshot dataSnapshot : ds.getChildren()) {
                        //if (dataSnapshot.getKey().equals(Busid))
                        {
                            if (!dataSnapshot.child("CurrentLat").getValue().toString().equals("") &&
                                    !dataSnapshot.child("CurrentLat").getValue().toString().equals("")) {
                                Double CurrentLat = (Double.parseDouble(dataSnapshot.child("CurrentLat").getValue().toString()));
                                Double CurrentLong = (Double.parseDouble(dataSnapshot.child("CurrentLong").getValue().toString()));
                                driverLatLng = new LatLng(CurrentLat, CurrentLong);
                                tv_lat_lng.setText(CurrentLat + "," + CurrentLong);
                                if (CurrentLat == 0 && CurrentLong == 0) {
                                    DialogClass dialog = new DialogClass(context,
                                            context.getResources().getString(R.string.message),
                                            context.getResources().getString(R.string.ride_is_ended));
                                    dialog.show();
                                } else
                                    //UPDATE_MARKER(driverLatLng.latitude, driverLatLng.longitude);
                                    updateLocation(driverLatLng.latitude, driverLatLng.longitude);
                            } else {
                                updateLocation(0.0, 0.0);
                                DialogClass dialog = new DialogClass(context,
                                        context.getResources().getString(R.string.message), context.getResources().getString(R.string.bus_is_not_moving_yet_now));
                                dialog.show();
                            }

                    }
                } catch (Exception ex) {
//Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void moveVechile(final Marker myMarker, final Double finalPositionLat, final Double finalPositionLong) {

        final LatLng startPosition = myMarker.getPosition();

        /*final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + (finalPositionLat) * t,
                        startPosition.longitude * (1 - t) + (finalPositionLong) * t);
                myMarker.setPosition(currentPosition);
                // myMarker.setRotation(finalPosition.getBearing());


                // Repeat till progress is completeelse
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 10);
                    // handler.postDelayed(this, 100);
                } else {
                    if (hideMarker) {
                        myMarker.setVisible(false);
                    } else {
                        myMarker.setVisible(true);
                    }
                }
            }
        });*/
        try {
            LatLng latLng = new LatLng(finalPositionLat, finalPositionLong);
            myMarker.setPosition(latLng);
            myMarker.setVisible(true);
        } catch (Exception ex) {

        }

    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    private void UPDATE_MARKER(Double locationlat, Double locationlong) {
        try {
            LatLng latLng = new LatLng(locationlat, locationlong);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
        } catch (Exception ex) {

        }
    }

    public void updateLocation(Double locationlat, Double locationlong) {
        try {
            latLng[0] = locationlat;
            latLng[1] = locationlong;

            if (marker == null) {
                marker = new PicassoMarker(googleMapHomeFrag.addMarker(new MarkerOptions().position(new LatLng(latLng[0], latLng[1]))));

                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference("Bus/").child(edtbusid.getText().toString()).child(SELECTED_ROUTE_KEY)
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            String full = dataSnapshot.child("Full").getValue().toString();
                            if (full.equalsIgnoreCase("1")) {
                                Picasso.with(context).load(R.drawable.buss_full_icon).resize(75, 100).into(marker);
                            } else if (full.equalsIgnoreCase("0")) {
                                Picasso.with(context).load(R.drawable.bus_a).resize(100, 100).into(marker);
                            }
/*
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (ds.getKey().equals(edtbusid.getText().toString())) {
                                    for(DataSnapshot ds_1:ds.getChildren()) {
                                        if(ds_1.getKey().equals(SELECTED_ROUTE_KEY)) {
                                            String full = ds.child("Full").getValue().toString();
                                            if (full.equalsIgnoreCase("1")) {
                                                Picasso.with(context).load(R.drawable.buss_full_icon).resize(75, 100).into(marker);
                                            } else if (full.equalsIgnoreCase("0")) {
                                                Picasso.with(context).load(R.drawable.bus_a).resize(100, 100).into(marker);
                                            }
                                        }
                                    }
                                }
                            }
*/
                        } catch (Exception ex) {
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng[0], latLng[1]), 18.0f));

            }

            if ((latLng[0] != -1 && latLng[0] != 0) && (latLng[1] != -1 && latLng[1] != 0)) {
                //googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 12.0f));
                //  float bearing = (float) bearingBetweenLocations(driverLatLng, new LatLng(locationlat,locationlong));
                if (marker != null) {
                    moveVechile(marker.getmMarker(), locationlat, locationlong);
                    //  rotateMarker(marker.getmMarker(), bearing, start_rotation);
                }
                driverLatLng = new LatLng(latLng[0], latLng[1]);
                // writeFileOnInternalStorage(MainActivity.this,"map.txt","Loc:"+driverLatLng+"\n");
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.location_not_found), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {

        }
    }

    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;


                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                start_rotation = -rot > 180 ? rot / 2 : rot;
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    List<Model_Bus> temp;
    List<String> lst_buses,lst_routes_keys,lst_routes;
    String[] arr_bus, arr_routes_keys,arr_routes;

    AlertDialog.Builder builder,builder_route;
    AlertDialog dialog,dialog_route;
    String SELECTED_ROUTE_KEY="";
    Dialog_Loading loading;
    void get_Drivers() {
        loading=new Dialog_Loading(context);
        loading.show();

        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.buses));
        builder_route=new AlertDialog.Builder(context);
        builder_route.setTitle(context.getResources().getString(R.string.route));
        temp = new ArrayList<>();
        lst_routes_keys=new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("Bus/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    for (DataSnapshot ds_1 : dataSnapshot.getChildren()) {
                        String BUS_NO = ds_1.getKey();
                        for (DataSnapshot ds : ds_1.getChildren()) {
                            Model_Bus model_bus = new Model_Bus();
                            model_bus.setBus_no(BUS_NO);
                            model_bus.setKey_db(ds.getKey());
                            model_bus.setSource(ds.child("Source").getValue().toString());
                            model_bus.setSource_name(ds.child("Source_name").getValue().toString());
                            model_bus.setDest(ds.child("Destination").getValue().toString());
                            model_bus.setDest_name(ds.child("Dest_name").getValue().toString());
                            temp.add(model_bus);
                        }
                    }
                    lst_buses = new ArrayList<>();
                    if (temp.size() > 0) {
                        for (int i = 0; i < temp.size(); i++) {
                            boolean flag = false;
                            for (int j = 0; j < lst_buses.size(); j++) {
                                if (temp.get(i).getBus_no().equals(lst_buses.get(j))) {
                                    flag = true;
                                }
                            }
                            if (!flag) {
                                lst_buses.add(temp.get(i).getBus_no());
                            }
                        }
                    }


                    arr_bus = new String[lst_buses.size()];
                    for (int i = 0; i < lst_buses.size(); i++) {
                        arr_bus[i] = lst_buses.get(i);
                    }

                    builder.setItems(arr_bus, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            edtbusid.setText(arr_bus[which]);
                            et_bus_route.setText("");
                            lst_routes_keys=new ArrayList<>();
                            lst_routes=new ArrayList<>();
                            for(int i=0;i<temp.size();i++){
                                if(temp.get(i).getBus_no().equals(edtbusid.getText().toString())){
                                    lst_routes_keys.add(temp.get(i).getKey_db());
                                    lst_routes.add(temp.get(i).getSource_name()+" TO "+temp.get(i).getDest_name());
                                }
                            }
                            arr_routes_keys =new String[lst_routes_keys.size()];
                            arr_routes =new String[lst_routes.size()];

                            for(int i=0;i<lst_routes_keys.size();i++){
                                arr_routes_keys[i]=lst_routes_keys.get(i);
                                arr_routes[i]=lst_routes.get(i);
                            }
                            builder_route.setItems(arr_routes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    et_bus_route.setText(arr_routes[which]);
                                    SELECTED_ROUTE_KEY=arr_routes_keys[which];
                                    //Toast.makeText(context, SELECTED_ROUTE_KEY,Toast.LENGTH_SHORT).show();
                                }
                            });





                            //Toast.makeText(context,val,Toast.LENGTH_SHORT).show();

                            //builder_route.setItems()
                        }
                    });
                    loading.dismiss();
                } catch (Exception ex) {
                    loading.dismiss();
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loading.dismiss();
            }
        });
    }
}