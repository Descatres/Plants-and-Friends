package com.example.PlantsAndFriends;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import org.eclipse.paho.android.service.MqttService;

public class CustomAlarmPingSender {

    private static final String TAG = "CustomAlarmPingHelper";

    private final MqttMonitorService service;
    private final String clientId;
    private PendingIntent pendingIntent;
    private boolean hasStarted = false;

    public CustomAlarmPingSender(MqttMonitorService service, String clientId) {
        this.service = service;
        this.clientId = clientId;
    }

    public void start() {
        String action = MqttService.BindServiceFlags.class + clientId;
        service.registerReceiver(alarmReceiver, new IntentFilter(action));

        // Include the necessary flags for Android version 31 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(service, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(service, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT);
        }

        schedule();
        hasStarted = true;
    }

    public void stop() {
        if (hasStarted) {
            if (pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager) service.getSystemService(Service.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }

            hasStarted = false;
            try {
                service.unregisterReceiver(alarmReceiver);
            } catch (IllegalArgumentException e) {
                // Ignore unregister errors.
            }
        }
    }

    private void schedule() {
        // Implement your scheduling logic here
        // ...

        Log.d(TAG, "Scheduled ping");
    }

    private final BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received alarm");

            // Implement your logic for handling the alarm reception
            // ...
        }
    };
}