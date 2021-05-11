package com.tracking.bustrackingsystem;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tracking.bustrackingsystem.Utils.LangPrefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private LatLng latLng;
    private Marker marker;
    Geocoder geocoder;
    String lat,lng;
    TextView tv_selected_location;
    Button btn_done;
    String from="";
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
        try {
            setContentView(R.layout.activity_map);
            setTitle(getResources().getString(R.string.bus_tracking_system));
            langPrefs=new LangPrefs(getApplicationContext());
            loadLocale();
            from = getIntent().getStringExtra("from");
            tv_selected_location = findViewById(R.id.tv_selected_location);
            btn_done = findViewById(R.id.btn_done);
            btn_done.setText(getResources().getString(R.string.done));
            btn_done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    if (from.equalsIgnoreCase("source")) {
                        intent.putExtra("source", tv_selected_location.getText().toString());
                        intent.putExtra("lat_lng", lat + "," + lng);
                    }
                    if (from.equalsIgnoreCase("dest")) {
                        intent.putExtra("dest", tv_selected_location.getText().toString());
                        intent.putExtra("lat_lng", lat + "," + lng);
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                /*Intent intent = new Intent();
                intent.putExtra("editTextValue", tv_selected_location.getText().toString());
                setResult(RESULT_OK, intent);
                finish();*/
                /*DialogClass dialogClass=new DialogClass(MapActivity.this,
                        "msg",lat+","+lng+"|"+tv_selected_location.getText().toString());
                dialogClass.show();*/
                }
            });
            // SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            //  mapFragment.getMapAsync(this);

            geocoder = new Geocoder(this, Locale.getDefault());
            setUpMapIfNeeded();
        }catch (Exception ex){

        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFragment   = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            mapFragment.getMapAsync(this);     // .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                //save current location
                latLng = point;

                List<Address> addresses = new ArrayList<>();
                try {
                    addresses = geocoder.getFromLocation(point.latitude, point.longitude,1);
                    lat=point.latitude+"";
                    lng=point.longitude+"";


                } catch (IOException e) {
                    e.printStackTrace();
                }

                android.location.Address address = addresses.get(0);

                if (address != null) {
                    StringBuilder sb = new StringBuilder();
                    //for (int i = 0; i < address.getAddressLine(0); i++){
                    sb.append(address.getAddressLine(0) + "\n");
                    //  }
                    tv_selected_location.setText(sb.toString());
                    /*DialogClass dialogClass=new DialogClass(getApplicationContext(),
                            "msg",lat+","+lng+"|"+sb.toString());
                    dialogClass.show();*/
                    //Toast.makeText(MapActivity.this, lat+","+lng+"|"+sb.toString(), Toast.LENGTH_LONG).show();
                }

                //remove previously placed Marker
                if (marker != null) {
                    marker.remove();
                }

                //place marker where user just clicked
                marker = mMap.addMarker(new MarkerOptions().position(point).title(getResources().getString(R.string.marker))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(point));


            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        // Add a marker in Sydney, Australia, and move the camera.
     /*   LatLng sydney = new LatLng(33.754840, 72.755682);
        mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.getUiSettings().setZoomControlsEnabled(true);*/


    }
}