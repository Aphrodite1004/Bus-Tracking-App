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

import com.tracking.bustrackingsystem.DriverProfileActivity;
import com.tracking.bustrackingsystem.Model.Model_Driver;
import com.tracking.bustrackingsystem.Model.Model_User;
import com.tracking.bustrackingsystem.R;
import com.tracking.bustrackingsystem.UserProfileActivity;

import java.util.ArrayList;

public class CustomAdapterUser extends RecyclerView.Adapter<CustomAdapterUser.ViewHolder> {
   private Context context;
   ArrayList<Model_User> my_model;

   public CustomAdapterUser(Context context, ArrayList<Model_User> my_model) {
       this.context = context;
       this.my_model = my_model;
   }

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.row_user, null);
       return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       holder.tv_name.setText(my_model.get(position).getName());
       holder.tv_id.setText(my_model.get(position).getId());
   }

   @Override
   public int getItemCount() {
       return my_model.size();
   }

   class ViewHolder extends RecyclerView.ViewHolder {
       TextView tv_name,tv_id;
       ViewHolder(View itemView) {
           super(itemView);
           RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                   , ViewGroup.LayoutParams.WRAP_CONTENT);
           itemView.setLayoutParams(lp);
           itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent=new Intent(context, UserProfileActivity.class);
                   intent.putExtra("id",tv_id.getText().toString().trim());
                   context.startActivity(intent);
               }
           });
           try {
               tv_name = itemView.findViewById(R.id.tv_name);
               tv_id= itemView.findViewById(R.id.tv_id);
           } catch (Exception ex) {
           }
       }
   }
}