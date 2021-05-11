package com.tracking.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.Model.Model_Bus;
import com.tracking.bustrackingsystem.Utils.Dialog_Loading;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class AddRouteActivity extends AppCompatActivity {
    MyPrefs prefs;
    Context context;
    String user_type;
    ImageView img_source, img_destination, img_departure;
    String apiKey;
    private static final int RESULT_SOURCE = 0;
    private static final int RESULT_DESTINATION = 1;

    EditText et_departure;
    String source_lat_lng = "", dest_lat_lng = "";
    Button btn_route;
    TextView tv_bus_no,tv_source,tv_dest;
    EditText et_distance;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    FirebaseDatabase database;
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

    String driver="";
    String db_key="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);
        try {
            context = AddRouteActivity.this;
            setTitle(context.getResources().getString(R.string.bus_tracking_system));
            langPrefs=new LangPrefs(context);
            loadLocale();
            database= FirebaseDatabase.getInstance();
            loading=new Dialog_Loading(context);
            prefs = new MyPrefs(context);
            btn_route = findViewById(R.id.btnAddRoute);
            btn_route.setHint(context.getResources().getString(R.string.add_route));
            tv_source = findViewById(R.id.tv_source);
            tv_source.setHint(context.getResources().getString(R.string.source));
            tv_dest = findViewById(R.id.tv_destination);
            tv_dest.setHint(context.getResources().getString(R.string.destination));
            tv_bus_no = findViewById(R.id.tv_bus_no);
            tv_bus_no.setHint(context.getResources().getString(R.string.bus_number));
            et_distance = findViewById(R.id.et_distance);
            et_distance.setHint(context.getResources().getString(R.string.distance));
            et_departure=findViewById(R.id.et_departure_time);
            et_departure.setHint(context.getResources().getString(R.string.departure_time));
            user_type = prefs.get_Val("user_type");

            //user_type="d";
            if (user_type.equalsIgnoreCase("a")) {
                btn_route.setVisibility(View.VISIBLE);
            } else {
                btn_route.setVisibility(View.GONE);
            }
            from = getIntent().getStringExtra("from");
            if (from.equalsIgnoreCase("fab")) {
                btn_route.setText(context.getResources().getString(R.string.add_route));
            } else {
                btn_route.setText(context.getResources().getString(R.string.update_route));
                tv_bus_no.setText(getIntent().getStringExtra("bus_no"));
                tv_source.setText(getIntent().getStringExtra("source_name"));
                tv_dest.setText(getIntent().getStringExtra("dest_name"));
                et_departure.setText(getIntent().getStringExtra("departure"));
                driver=getIntent().getStringExtra("driver");
                db_key=getIntent().getStringExtra("db_key");
                String[] dist=getIntent().getStringExtra("distance").split(" ");
                et_distance.setText(dist[0]);

                source_lat_lng=getIntent().getStringExtra("source");
                dest_lat_lng=getIntent().getStringExtra("dest");

            }

            btn_route.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Add_Route();
                   /* if(!tv_bus_no.getText().toString().trim().isEmpty() &&
                            !tv_source.getText().toString().trim().isEmpty() &&
                            !tv_dest.getText().toString().trim().isEmpty() &&
                            !et_distance.getText().toString().trim().isEmpty()){
                        if (from.equalsIgnoreCase("fab")) {
                            Add_Route();
                        } else {
                            Update_Route();
                        }
                    }else {
                        DialogClass dialog = new DialogClass(context,
                                context.getResources().getString(R.string.message), context.getResources().getString(R.string.enter_values));
                        dialog.show();
                    }*/


                }
            });
            img_source = findViewById(R.id.img_source);
            img_destination = findViewById(R.id.img_dest);
            img_departure = findViewById(R.id.img_departure);
            img_departure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timePicker();
                }
            });

            apiKey = getResources().getString(R.string.MAP_KEY);
            img_source.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MapActivity.class);
                    intent.putExtra("from", "source");
                    startActivityForResult(intent, RESULT_SOURCE);
                }
            });
            img_destination.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MapActivity.class);
                    intent.putExtra("from", "dest");
                    startActivityForResult(intent, RESULT_DESTINATION);
                }
            });

            tv_bus_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog = builder.create();
                    dialog.show();
                }
            });

            get_Buses();
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    String time = "";

    private String timePicker() {
        try {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(AddRouteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    time = selectedHour + ":" + selectedMinute;
                    et_departure.setText(time);
                }
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle(context.getResources().getString(R.string.select_time));
            mTimePicker.show();
        } catch (Exception ex) {

        }
        return time;
    }
    private void Distance(double latitude, double longitude,
                          double prelatitute, double prelongitude){
        String result_in_kms = "";
        String url = "http://maps.google.com/maps/api/directions/xml?origin="
                + latitude + "," + longitude + "&destination=" + prelatitute
                + "," + prelongitude + "&sensor=false&units=metric";

    }
    /*private String getDistanceOnRoad(double latitude, double longitude,
                                     double prelatitute, double prelongitude) {
        String result_in_kms = "";
        String url = "http://maps.google.com/maps/api/directions/xml?origin="
                + latitude + "," + longitude + "&destination=" + prelatitute
                + "," + prelongitude + "&sensor=false&units=metric";
        String tag[] = { "text" };
        HttpResponse response = null;
        try {

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);
            response = httpClient.execute(httpPost, localContext);
            InputStream is = response.getEntity().getContent();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(is);
            if (doc != null) {
                NodeList nl;
                ArrayList args = new ArrayList();
                for (String s : tag) {
                    nl = doc.getElementsByTagName(s);
                    if (nl.getLength() > 0) {
                        Node node = nl.item(nl.getLength() - 1);
                        args.add(node.getTextContent());
                    } else {
                        args.add(" - ");
                    }
                }
                result_in_kms = String.format("%s", args.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result_in_kms;
    }*/
    RequestQueue queue;
    public static float getKmFromLatLong(float lat1, float lng1, float lat2, float lng2){
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters/1000;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SOURCE) {
            if (resultCode == RESULT_OK) {
                String returnString = data.getStringExtra("source");
                source_lat_lng = data.getStringExtra("lat_lng");
                tv_source.setText(returnString);
            }
        }
        if (requestCode == RESULT_DESTINATION) {
            if (resultCode == RESULT_OK) {
                String returnString = data.getStringExtra("dest");
                dest_lat_lng = data.getStringExtra("lat_lng");
                tv_dest.setText(returnString);
            }
        }
    }

    List<Model_Bus> temp;
    String[] lst_bus;
    String[] lst_driver;
    Dialog_Loading loading;

    void Add_Route(){
        try {
            loading.show();
            DatabaseReference ref=database.getReference();
            String val= ref.push().getKey();//CurrentLat
            /*database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("CurrentLat").setValue("0.0");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("CurrentLong").setValue("0.0");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Driver").setValue(SELECTED_FINAL_DRIVER);
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Full").setValue("0");

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Dest_name").setValue("Al Salam branch");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Source_name").setValue("Al Ula Governorate university branch");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Distance").setValue("275.43");

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Source").setValue("26.484433, 37.972632");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Destination").setValue("24.470368, 39.570180");

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Departure").setValue("");*/

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("CurrentLat").setValue("0.0");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("CurrentLong").setValue("0.0");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Driver").setValue(SELECTED_FINAL_DRIVER);
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Full").setValue("0");

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Source_name").setValue(tv_source.getText().toString().trim());
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Dest_name").setValue(tv_dest.getText().toString().trim());
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Distance").setValue(et_distance.getText().toString());

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Destination").setValue(dest_lat_lng);
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Source").setValue(source_lat_lng);

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(val).child("Departure").setValue(et_departure.getText().toString());

            Toast.makeText(context,context.getResources().getString(R.string.route_added), Toast.LENGTH_LONG).show();

            loading.dismiss();
            //get_Buses();
        }catch (Exception ex){
            loading.dismiss();
            Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    void Update_Route(){
        try {
            loading.show();

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("CurrentLat").setValue("0.0");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("CurrentLong").setValue("0.0");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("Driver").setValue(driver);
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("Full").setValue("0");
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("Source_name").setValue(tv_source.getText().toString().trim());
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("Dest_name").setValue(tv_dest.getText().toString().trim());
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("Distance").setValue(et_distance.getText().toString());

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("Destination").setValue(dest_lat_lng);
            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("Source").setValue(source_lat_lng);

            database.getReference("Bus/").child(tv_bus_no.getText().toString()).child(db_key).child("Departure").setValue(et_departure.getText().toString());

            Toast.makeText(context,context.getResources().getString(R.string.route_updated), Toast.LENGTH_LONG).show();
            loading.dismiss();
            //get_Buses();
        }catch (Exception ex){
            loading.dismiss();
            Toast.makeText(context,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    String SELECTED_FINAL_DRIVER="";

    void get_Buses() {
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Buses");
        temp = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("Bus/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String bus_no="";
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        bus_no=ds.getKey();
                        for (DataSnapshot ds_1 : ds.getChildren()) {
                            Model_Bus bus = new Model_Bus();
                            bus.setBus_no(bus_no);
                            bus.setDriver(ds_1.child("Driver").getValue().toString());
                            boolean flag=false;
                            if (temp.size() > 0) {
                                for (int i = 0; i < temp.size(); i++) {
                                    if (temp.get(i).getBus_no().equals(bus_no)) {
                                        flag=true;
                                    }
                                }
                            }
                            if(!flag){
                                temp.add(bus);
                            }
                        }
                    }

                    lst_bus = new String[temp.size()];
                    lst_driver= new String[temp.size()];
                    for (int i = 0; i < temp.size(); i++) {
                        lst_bus[i] = temp.get(i).getBus_no();
                        lst_driver[i]=temp.get(i).getDriver();
                    }
                    builder.setItems(lst_bus, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tv_bus_no.setText(lst_bus[which]);
                            SELECTED_FINAL_DRIVER=lst_driver[which];
                        }
                    });
                } catch (Exception ex) {
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


}