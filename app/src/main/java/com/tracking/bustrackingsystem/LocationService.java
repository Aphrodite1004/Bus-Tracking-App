package com.tracking.bustrackingsystem;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.text.SimpleDateFormat;
import java.util.Date;


public class LocationService extends Service {

    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    Intent intent;
    int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        //AddtoHistory(true);


        intent = new Intent(getApplicationContext(), OurBroadcastReceiver.class);
    }

    MyPrefs prefs;

/*
    void AddtoHistory(Boolean start) {
        prefs = new MyPrefs(LocationService.this);
        String name = prefs.get_Val("name");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd  'at' HH:mm:ss ");
        String currentDateandTime = sdf.format(new Date());
        String uid = prefs.get_Val("Bus");
        DatabaseReference ref = database.getReference("History/").child(uid);
        String key =  ref.push().getKey();
        ref=ref.child(key);
        ref.child("Date").setValue(currentDateandTime);
        ref.child("Driver").setValue(name);
        if (start) {
            ref.child("StartDate").setValue(currentDateandTime);
            ref.child("StartPosition").setValue("");

        } else {
            ref.child("EndDate").setValue(currentDateandTime);
            ref.child("EndPosition").setValue("");
            listener.onProviderDisabled(LocationManager.NETWORK_PROVIDER);
            listener.onProviderDisabled(LocationManager.GPS_PROVIDER);
        }
        ref.push();
    }
*/

    @Override
    public void onStart(Intent intent, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 100, (LocationListener) listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 4, listener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        //Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        //handler.removeCallbacks(sendUpdatesToUI);
        try {
            super.onDestroy();
            prefs = new MyPrefs(getApplicationContext());

            if (prefs.get_Val("loc").equals("end")) {
                locationManager.removeUpdates(listener);
                PackageManager pm = getApplicationContext().getPackageManager();
                ComponentName componentName = new ComponentName(getApplicationContext(), OurBroadcastReceiver.class);
                pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                Toast.makeText(getApplicationContext(), "Ride Ended", Toast.LENGTH_LONG).show();
                Log.v("STOP_SERVICE", "DONE");
                stopSelf();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("Bus/").child(prefs.get_Val("Bus")).child(prefs.get_Val("FINAL_ROUTE_KEY"));
                ref.child("CurrentLat").setValue("0.0");
                ref.child("CurrentLong").setValue("0.0");
            }

            /*final FirebaseDatabase database = FirebaseDatabase.getInstance();


            database.getReference("Bus/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.getKey().equals(prefs.get_Val("Bus"))) {
                                String val_1=ds.child("CurrentLat").getValue().toString();
                                String val_2=ds.child("CurrentLong").getValue().toString();


                                if (val_1.equals("0.0") && val_2.equals("0.0")){
                                    locationManager.removeUpdates(listener);
                                    PackageManager pm = getApplicationContext().getPackageManager();
                                    ComponentName componentName = new ComponentName(getApplicationContext(), OurBroadcastReceiver.class);
                                    pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                            PackageManager.DONT_KILL_APP);
                                    Toast.makeText(getApplicationContext(), "Ride Ended", Toast.LENGTH_LONG).show();
                                    Log.v("STOP_SERVICE", "DONE");
                                    stopSelf();
                                }
                            }
                        }


                    } catch (Exception ex) {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
*/




            /*prefs=new MyPrefs(getApplicationContext());
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("Bus/").child(prefs.get_Val("Bus"));
            ref.child("CurrentLat").setValue("0.0");
            ref.child("CurrentLong").setValue("0.0");*/

            /*locationManager.removeUpdates(listener);
            PackageManager pm  = getApplicationContext().getPackageManager();
            ComponentName componentName = new ComponentName(getApplicationContext(), OurBroadcastReceiver.class);
            pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            Toast.makeText(getApplicationContext(),"Ride Ended",Toast.LENGTH_LONG).show();
            Log.v("STOP_SERVICE", "DONE");
            stopSelf();*/


        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Ride Ended" + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    public class MyLocationListener implements LocationListener {
        private double[] latLng = new double[2];
        MyPrefs prefs;

        Boolean Compare() {
            try {
                String des = prefs.get_Val("Destination");
                String dest[] = des.split(",");
                if (latLng[0] == Double.parseDouble(dest[0]) &&
                        latLng[1] == Double.parseDouble(dest[1])) {
                    //AddtoHistory(false);
                    return true;
                }
            } catch (Exception ex) {
            }
            return false;
        }

        void SetDriverFirebase() {
            try {
                prefs = new MyPrefs(LocationService.this);
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                if (prefs.get_Val("loc").equals("start")) {
                    String uid = prefs.get_Val("Bus");
                    String name = prefs.get_Val("name");
                    DatabaseReference ref = database.getReference("Bus/").child(uid).child(prefs.get_Val("FINAL_ROUTE_KEY"));
                    ref.child("Driver").setValue(name);
                    ref.child("CurrentLat").setValue(latLng[0]);
                    ref.child("CurrentLong").setValue(latLng[1]);
                    ref.push();
                }else if (prefs.get_Val("loc").equals("end")) {
                    DatabaseReference ref = database.getReference("Bus/").child(prefs.get_Val("Bus")).child(prefs.get_Val("FINAL_ROUTE_KEY"));
                    ref.child("CurrentLat").setValue("0.0");
                    ref.child("CurrentLong").setValue("0.0");
                }

                /*if (!Compare()) {
                    String name = prefs.get_Val("name");
                    DatabaseReference ref = database.getReference("Bus/").child(uid);
                    ref.child("Driver").setValue(name);
                    ref.child("CurrentLat").setValue(latLng[0]);
                    ref.child("CurrentLong").setValue(latLng[1]);
                    ref.push();
                } else {
                    DatabaseReference ref = database.getReference("Bus/").child(uid);
                    ref.child("Driver").setValue("");
                    ref.child("CurrentLat").setValue("0");
                    ref.child("CurrentLong").setValue("0");
                    ref.push();
                }*/
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "SetDriverFirebase"+ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        public void onLocationChanged(final Location loc) {
            try {
                Log.i("*****", "Location changed");
                prefs = new MyPrefs(getApplicationContext());
                if (isBetterLocation(loc, previousBestLocation)) {
                    if (prefs.get_Val("loc").equals("start")) {
                        latLng[0] = loc.getLatitude();
                        latLng[1] = loc.getLongitude();

                        SetDriverFirebase();
                        intent.putExtra("Latitude", loc.getLatitude());
                        intent.putExtra("Longitude", loc.getLongitude());
                        intent.putExtra("Provider", loc.getProvider());
                        sendBroadcast(intent);
                    }else if (prefs.get_Val("loc").equals("end")) {
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference ref = database.getReference("Bus/").child(prefs.get_Val("Bus")).child(prefs.get_Val("FINAL_ROUTE_KEY"));
                        ref.child("CurrentLat").setValue("0.0");
                        ref.child("CurrentLong").setValue("0.0");
                    }
                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "onLocationChanged"+ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderDisabled(String provider) {
            //Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            //Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }
    }
}
    /*
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
*/