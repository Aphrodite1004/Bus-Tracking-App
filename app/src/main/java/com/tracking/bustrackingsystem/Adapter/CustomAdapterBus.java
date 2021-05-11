package com.tracking.bustrackingsystem.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.tracking.bustrackingsystem.DialogClass;
import com.tracking.bustrackingsystem.HistoryActivity;
import com.tracking.bustrackingsystem.Model.Model_Bus;
import com.tracking.bustrackingsystem.R;
import com.tracking.bustrackingsystem.Utils.MyPrefs;

import java.util.ArrayList;

public class CustomAdapterBus
        extends RecyclerSwipeAdapter<CustomAdapterBus.ViewHolder> {
   private Context context;
   ArrayList<Model_Bus> my_model;

   public CustomAdapterBus(Context context, ArrayList<Model_Bus> my_model) {
       this.context = context;
       this.my_model = my_model;
   }
    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }
   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.row_bus, null);
       return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
       holder.tv_text.setText(my_model.get(position).getBus_no());
       holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
       holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewById(R.id.bottom_wraper));
       MyPrefs prefs=new MyPrefs(context);
       if(!prefs.get_Val("user_type").equals("a")){
           ViewGroup.LayoutParams params = holder.wrapper_delete.getLayoutParams();
           params.height = 0;
           params.width = 0;
           holder.wrapper_delete.setLayoutParams(params);
       }
       holder.Delete.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               final FirebaseDatabase database = FirebaseDatabase.getInstance();
               database.getReference("Bus/").addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       try {
                           for (DataSnapshot ds : dataSnapshot.getChildren()) {
                               if(ds.getKey().equals(my_model.get(position).getBus_no())){
                                   ds.getRef().removeValue();
                                   DialogClass dialogClass=new DialogClass(context,
                                           context.getResources().getString(R.string.message),
                                           context.getResources().getString(R.string.deleted));
                                   dialogClass.show();
                               }
                           }

                       } catch (Exception ex) {

                       }
                   }
                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {
                   }
               });
           }
       });

       holder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent=new Intent(context, HistoryActivity.class);
               intent.putExtra("bus",my_model.get(position).getBus_no());
               context.startActivity(intent);
           }
       });
       mItemManger.bindView(holder.itemView, position);
   }

   @Override
   public int getItemCount() {
       return my_model.size();
   }

   class ViewHolder extends RecyclerView.ViewHolder {
       TextView tv_text;
       LinearLayout Delete;
       SwipeLayout swipeLayout;
       LinearLayout wrapper_delete;
       ViewHolder(View itemView) {
           super(itemView);
           RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                   , ViewGroup.LayoutParams.WRAP_CONTENT);
           itemView.setLayoutParams(lp);
           Delete = itemView.findViewById(R.id.Delete);
           swipeLayout = itemView.findViewById(R.id.swipe);
           wrapper_delete= itemView.findViewById(R.id.bottom_wraper);
          /* itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent=new Intent(context, HistoryActivity.class);
                   intent.putExtra("bus",tv_text.getText().toString().trim());
                   context.startActivity(intent);
               }
           });*/
           try {
               tv_text = itemView.findViewById(R.id.tv_bus_no);
           } catch (Exception ex) {
           }
       }
   }
}