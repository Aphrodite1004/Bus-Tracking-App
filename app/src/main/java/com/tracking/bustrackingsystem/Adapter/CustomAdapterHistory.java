package com.tracking.bustrackingsystem.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tracking.bustrackingsystem.AddRouteActivity;
import com.tracking.bustrackingsystem.Model.Model_History;
import com.tracking.bustrackingsystem.Model.Model_Route;
import com.tracking.bustrackingsystem.R;

import java.util.ArrayList;

public class CustomAdapterHistory extends RecyclerView.Adapter<CustomAdapterHistory.ViewHolder> {
    private Context context;
    ArrayList<Model_History> my_model;

    public CustomAdapterHistory(Context context, ArrayList<Model_History> my_model) {
        this.context = context;
        this.my_model = my_model;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.row_history, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_driver.setText(my_model.get(position).getDriver());
        holder.tv_source.setText(my_model.get(position).getSource());
        holder.tv_destination.setText(my_model.get(position).getDestination());
        holder.tv_start.setText(my_model.get(position).getStart().replace("at",context.getResources().getString(R.string.at)));
        holder.tv_end.setText(my_model.get(position).getEnd().replace("at", context.getResources().getString(R.string.at)));

        holder.tv_title_driver.setText(context.getResources().getString(R.string.driver_name) + ": ");
        holder.tv_title_start.setText(context.getResources().getString(R.string.started_at) + ": ");
        holder.tv_title_end.setText(context.getResources().getString(R.string.ended_at) + ": ");
        holder.tv_title_source.setText(context.getResources().getString(R.string.source) + ": ");
        holder.tv_title_destination.setText(context.getResources().getString(R.string.destination) + ": ");


    }

    @Override
    public int getItemCount() {
        return my_model.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_driver, tv_start, tv_end, tv_source, tv_destination;
        TextView tv_title_driver, tv_title_start, tv_title_end, tv_title_source, tv_title_destination;

        ViewHolder(View itemView) {
            super(itemView);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(lp);

            try {
                tv_driver = itemView.findViewById(R.id.tv_name);
                tv_source = itemView.findViewById(R.id.tv_source);
                tv_destination = itemView.findViewById(R.id.tv_destination);
                tv_start = itemView.findViewById(R.id.tv_start);
                tv_end = itemView.findViewById(R.id.tv_end);

                tv_title_driver = itemView.findViewById(R.id.tv_title_driver);
                tv_title_start = itemView.findViewById(R.id.tv_title_start);
                tv_title_end = itemView.findViewById(R.id.tv_title_end);
                tv_title_source = itemView.findViewById(R.id.tv_title_source);
                tv_title_destination = itemView.findViewById(R.id.tv_title_destination);
            } catch (Exception ex) {
            }
        }
    }
}