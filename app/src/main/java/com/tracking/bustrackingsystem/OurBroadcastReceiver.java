package com.tracking.bustrackingsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


    public class OurBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent i) {
            try {
                Intent background = new Intent(context, LocationService.class);
                Log.e("Testing broadcast ", "testing called broadcast called");
                context.startService(background);
            } catch (Exception ex) {

            }
        }



    }
