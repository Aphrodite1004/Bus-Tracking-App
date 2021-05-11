package com.tracking.bustrackingsystem.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tracking.bustrackingsystem.AddRouteActivity;
import com.tracking.bustrackingsystem.Model.Model_Route;
import com.tracking.bustrackingsystem.R;
import com.tracking.bustrackingsystem.Utils.LangPrefs;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;
import java.util.Locale;

public class CustomAdapterRoute extends
        RecyclerSwipeAdapter<CustomAdapterRoute.ViewHolder> {
    private Context context;
    ArrayList<Model_Route> my_model;
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
    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public CustomAdapterRoute(Context context, ArrayList<Model_Route> my_model) {
        this.context = context;
        this.my_model = my_model;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.row_route, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.tv_name.setText(my_model.get(position).getDriver());
        holder.tv_bus_no.setText(my_model.get(position).getBus_no());
        holder.tv_source.setText(my_model.get(position).getSource());
        holder.tv_source_name.setText(my_model.get(position).getSource_name());
        holder.tv_dest.setText(my_model.get(position).getDest());
        holder.tv_dest_name.setText(my_model.get(position).getDest_name());
        if (my_model.get(position).getDistance().equals("")) {
            holder.tv_distance.setText("0 Km");
        } else {
            holder.tv_distance.setText(my_model.get(position).getDistance() + " Km");
        }
        holder.tv_departure.setText(my_model.get(position).getDeparture());
        holder.tv_title_name.setText(context.getResources().getString(R.string.driver_name)+": ");
        holder.tv_title_bus_no.setText(context.getResources().getString(R.string.bus_number)+": ");
        holder.tv_title_source_name.setText(context.getResources().getString(R.string.source)+": ");
        holder.tv_title_dest_name.setText(context.getResources().getString(R.string.destination)+": ");
        holder.tv_title_distance.setText(context.getResources().getString(R.string.distance)+": ");
        holder.tv_title_departure.setText(context.getResources().getString(R.string.departure)+": ");
        MyPrefs prefs=new MyPrefs(context);

        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewById(R.id.bottom_wraper));
        if(!prefs.get_Val("user_type").equals("a")){
            ViewGroup.LayoutParams params = holder.wrapper_delete.getLayoutParams();
            params.height = 0;
            params.width = 0;
            holder.wrapper_delete.setLayoutParams(params);
        }
        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference("Bus/").child(my_model.get(position).getBus_no())
                            .child(my_model.get(position).getDb_key())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                   /* database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("Source_name").setValue("");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("Dest_name").setValue("");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("Distance").setValue("");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("Destination").setValue("");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("Source").setValue("");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("Departure").setValue("");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("CurrentLat").setValue("");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("CurrentLong").setValue("");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("Full").setValue("0");
                    database.getReference("Bus/").child(my_model.get(position).getBus_no()).child("Driver").setValue("");
*/
                }catch (Exception ex){

                }
            }
        });

        holder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyPrefs prefs = new MyPrefs(context);
                String user_type = prefs.get_Val("user_type");
                if (user_type.equalsIgnoreCase("a")) {
                    Intent i = new Intent(context, AddRouteActivity.class);
                    i.putExtra("from", "adapter");
                    i.putExtra("bus_no", my_model.get(position).getBus_no());
                    i.putExtra("source", my_model.get(position).getSource());
                    i.putExtra("source_name", my_model.get(position).getSource_name());
                    i.putExtra("dest", my_model.get(position).getDest());
                    i.putExtra("dest_name", my_model.get(position).getDest_name());
                    i.putExtra("departure", my_model.get(position).getDeparture());
                    i.putExtra("distance", my_model.get(position).getDistance());
                    i.putExtra("driver",my_model.get(position).getDriver());
                    i.putExtra("db_key",my_model.get(position).getDb_key());
                    context.startActivity(i);
                }
            }
        });
        mItemManger.bindView(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return my_model.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_bus_no, tv_source_name, tv_dest_name, tv_distance, tv_source, tv_dest, tv_departure;
        TextView tv_title_name, tv_title_bus_no, tv_title_source_name, tv_title_dest_name, tv_title_distance, tv_title_source, tv_title_dest, tv_title_departure;
        SwipeLayout swipeLayout;
        LinearLayout Delete;
        LinearLayout wrapper_delete;
        ViewHolder(View itemView) {
            super(itemView);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(lp);
            Delete = itemView.findViewById(R.id.Delete);
            swipeLayout = itemView.findViewById(R.id.swipe);
            wrapper_delete= itemView.findViewById(R.id.bottom_wraper);
/*
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyPrefs prefs = new MyPrefs(context);
                    //String user_type="d";

                    String user_type = prefs.get_Val("user_type");

                    if (user_type.equalsIgnoreCase("a")) {
                        Intent i = new Intent(context, AddRouteActivity.class);
                        i.putExtra("from", "adapter");
                        i.putExtra("bus_no", tv_bus_no.getText().toString().trim());
                        i.putExtra("source", tv_source.getText().toString().trim());
                        i.putExtra("source_name", tv_source_name.getText().toString().trim());
                        i.putExtra("dest", tv_dest.getText().toString().trim());
                        i.putExtra("dest_name", tv_dest_name.getText().toString().trim());
                        i.putExtra("departure", tv_departure.getText().toString().trim());
                        i.putExtra("distance", tv_distance.getText().toString().trim());

                        context.startActivity(i);
                    }
                }
            });
*/
            try {
                tv_name = itemView.findViewById(R.id.tv_name);
                tv_bus_no = itemView.findViewById(R.id.tv_bus_no);
                tv_source_name = itemView.findViewById(R.id.tv_source_name);
                tv_source = itemView.findViewById(R.id.tv_source);
                tv_dest = itemView.findViewById(R.id.tv_destination);
                tv_dest_name = itemView.findViewById(R.id.tv_destination_name);
                tv_distance = itemView.findViewById(R.id.tv_distance);
                tv_departure = itemView.findViewById(R.id.tv_departure);

                tv_title_name = itemView.findViewById(R.id.tv_title_name);
                tv_title_bus_no = itemView.findViewById(R.id.tv_title_bus_no);
                tv_title_source_name = itemView.findViewById(R.id.tv_title_source_name);
                tv_title_dest_name = itemView.findViewById(R.id.tv_title_dest_name);
                tv_title_distance = itemView.findViewById(R.id.tv_title_distance);
                tv_title_departure = itemView.findViewById(R.id.tv_title_departure);

            } catch (Exception ex) {
            }
        }
    }
}