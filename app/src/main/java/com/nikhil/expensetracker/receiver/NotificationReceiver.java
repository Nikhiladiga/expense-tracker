package com.nikhil.expensetracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nikhil.expensetracker.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        if (action.equals("closeAction")) {
            MainActivity.getInstance().finishAffinity();
            System.exit(0);
        }
    }
}
