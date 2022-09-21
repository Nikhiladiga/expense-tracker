package com.nikhil.expensetracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;

import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.receiver.NotificationReceiver;
import com.nikhil.expensetracker.receiver.SmsReceiver;
import com.nikhil.expensetracker.utils.Util;

public class SMSReaderService extends Service {

    private Notification.Builder notification;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        Util.smsReceiver = new SmsReceiver();
        registerReceiver(Util.smsReceiver, intentFilter);
        buildNotification();
        startForeground(1001, notification.build());

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(Util.smsReceiver);
    }

    private void buildNotification() {
        final String CHANNELID = "Foreground Service ID";
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);

        Intent closeIntent = new Intent(this, NotificationReceiver.class);
        closeIntent.putExtra("action", "closeAction");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_IMMUTABLE);

        notification = new Notification.Builder(this, CHANNELID)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Close", pendingIntent)
                .setContentText("SMS Service is running")
                .setContentTitle("Expense tracker enabled")
                .setSmallIcon(R.drawable.ic_launcher_foreground);
    }

}