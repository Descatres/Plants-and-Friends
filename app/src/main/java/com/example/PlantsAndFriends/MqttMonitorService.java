package com.example.PlantsAndFriends;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MqttMonitorService extends Service {
    private final String humidityTopic = "plants_and_friends_humidity_topic";
    private final String temperatureTopic = "plants_and_friends_temperature_topic";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "MqttMonitorService";
    private MqttAndroidClient mqttAndroidClient;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final int CHECK_INTERVAL = 60000; // 60 seconds
    private static final String CHANNEL_ID = "MyChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        String serverUri = "tcp://broker.hivemq.com:1883";
        String clientId = "androidClient_" + UUID.randomUUID().toString();


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Handle connection lost
                Log.e("MainActivity", "Connection lost");
            }

            @Override
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                // Log.e("MainActivity", "Message arrived" + message.toString());
                String payload = new String(message.getPayload());
                // make the notification here

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Log.e("MainActivity", "Delivery complete");
            }
        });

        createNotificationChannel();

        connectMQTT();

        fetchAndVerifyThresholds();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMonitoring();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectMQTT();
    }

    private void fetchAndVerifyThresholds() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();

            db.collection("users").document(currentUserUid).collection("thresholds")
                    .document("sensorThresholds")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Thresholds document exists, update UI with retrieved values
                            Map<String, Object> thresholdsData = documentSnapshot.getData();
                            if (thresholdsData != null) {
                                checkAndSendNotifications(thresholdsData);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Log.e("AlertsFragment", "Error fetching thresholds from Firestore", e);
                    });
        }
    }

    private void startMonitoring() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check MQTT values and send notifications
//                checkAndSendNotifications();

                handler.postDelayed(this, CHECK_INTERVAL);
            }
        }, CHECK_INTERVAL);
    }

    private float parseFloatWithDefault(Object value) {
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (NumberFormatException e) {
            return Float.NaN;
        }
    }

    private void checkAndSendNotifications(Map<String, Object> thresholdsData) {
        // Update UI elements with retrieved thresholds
        if (thresholdsData.containsKey("minTemperature")) {
            float minTemperature = parseFloatWithDefault(thresholdsData.get("minTemperature"));
            float maxTemperature = parseFloatWithDefault(thresholdsData.get("maxTemperature"));
            List<Float> temperatureValues = Arrays.asList(minTemperature, maxTemperature);
        }

        if (thresholdsData.containsKey("minHumidity")) {
            float minHumidity = parseFloatWithDefault(thresholdsData.get("minHumidity"));
            float maxHumidity = parseFloatWithDefault(thresholdsData.get("maxHumidity"));
            List<Float> humidityValues = Arrays.asList(minHumidity, maxHumidity);
            //
        }

//        if (currentTemperature < minTemperatureThreshold || currentTemperature > maxTemperatureThreshold) {
//            // Temperature is outside the threshold, send a temperature alert notification
//            showNotification("Temperature Alert", "Temperature is outside the threshold");
//        }

        // Check if humidity is outside the threshold
//        if (currentHumidity < minHumidityThreshold || currentHumidity > maxHumidityThreshold) {
//            // Humidity is outside the threshold, send a humidity alert notification
//            showNotification("Humidity Alert", "Humidity is outside the threshold");
//        }
    }

    private void connectMQTT() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Subscribe to MQTT
                    // Log.e("MainActivity", "Connected to MQTT broker");
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to connect to MQTT broker");
//                    unsubscribeFromTopics();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopics() {
        try {
            int qos = 1;

            IMqttToken subToken1 = mqttAndroidClient.subscribe(temperatureTopic, qos);
            subToken1.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Log.e("MainActivity", "Subscribed to temperature topic");
                    // Handle successful subscription to temperature topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to subscribe to temperature topic");
                    // Handle failure
                }
            });

            IMqttToken subToken2 = mqttAndroidClient.subscribe(humidityTopic, qos);
            subToken2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Log.e("MainActivity", "Subscribed to humidity topic");
                    // Handle successful subscription to humidity topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to subscribe to humidity topic");
                    // Handle failure
                }
            });
        } catch (MqttSecurityException e) {
            // Log.e("MainActivity", "security exception", e);
            throw new RuntimeException(e);
        } catch (MqttException e) {
            // Log.e("MainActivity", "mqtt exception", e);
            e.printStackTrace();
        }
    }

    private void disconnectMQTT() {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void unsubscribeFromTopics() {
        try {

            IMqttToken unsubToken1 = mqttAndroidClient.unsubscribe(temperatureTopic);
            unsubToken1.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Log.e("MainActivity", "Unsubscribed from temperature topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to unsubscribe from temperature topic");
                }
            });

            IMqttToken unsubToken2 = mqttAndroidClient.unsubscribe(humidityTopic);
            unsubToken2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Log.e("MainActivity", "Unsubscribed from humidity topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to unsubscribe from humidity topic");
                }
            });
        } catch (MqttException e) {
            // Log.e("MainActivity", "Connection exception", e);
            e.printStackTrace();
        }

    }


    private void showNotification(String title, String message) {
        // Use NotificationCompat.Builder to create and display notifications

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: call ActivityCompat#requestPermissions here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            return;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (title.equals("Temperature Alert")) {
            notificationManager.notify(1, builder.build());
        } else if (title.equals("Humidity Alert")) {
            notificationManager.notify(2, builder.build());
        }
    }

    private void createNotificationChannel() {
        // Create the notification channel if it doesn't exist
        CharSequence name = "MyChannel";
        String description = "Temperature and Humidity Alerts";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

    }

}