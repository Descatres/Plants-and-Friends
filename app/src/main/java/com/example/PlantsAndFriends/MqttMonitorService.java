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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MqttMonitorService extends Service {
    private final String humidityTopic = "plants_and_friends_humidity_topic";
    private final String temperatureTopic = "plants_and_friends_temperature_topic";
    private DataRepository dataRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "MqttMonitorService";
    private MqttAndroidClient mqttAndroidClient;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final int CHECK_INTERVAL = 300000; // 5 minutes
    private static final String CHANNEL_ID = "MyChannel";
    private MqttViewModel mqttViewModel;
    private float currentTemperature = Float.NaN;
    private float currentHumidity = Float.NaN;
    private boolean notificationTemperatureSent = false;
    private boolean notificationHumiditySent = false;

    @Override
    public void onCreate() {
        super.onCreate();

        String serverUri = "tcp://broker.hivemq.com:1883";
        String clientId = "androidClient_" + UUID.randomUUID().toString();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dataRepository = DataRepository.getInstance();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "Connection lost");
                updateMqttData(humidityTopic, "Connection Lost");
                updateMqttData(temperatureTopic, "Connection Lost");
            }

            @Override
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                String payload = new String(message.getPayload());
//                Log.e(TAG, "Message arrived" + payload);

                if (topic.equals(temperatureTopic)) {
                    currentTemperature = parseFloatWithDefault(payload);
                } else if (topic.equals(humidityTopic)) {
                    currentHumidity = parseFloatWithDefault(payload);
                }

                fetchAndVerifyThresholds();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        createNotificationChannel();
        connectMQTT();
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
                            Map<String, Object> thresholdsData = documentSnapshot.getData();
                            if (thresholdsData != null) {
                                // TODO - isolate humitidy and temperature separately for room and plants
                                checkAndSendRoomNotifications(thresholdsData);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching thresholds from Firestore", e);
                    });

            db.collection("users").document(currentUserUid).collection("plants")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty() && !notificationTemperatureSent && !notificationHumiditySent) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Map<String, Object> plantData = document.getData();
                                if (plantData != null) {
//                                    Log.e(TAG, "Plant data: " + document.getData());
                                    checkAndSendPlantNotifications(plantData);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching plant data from Firestore", e);
                    });
        }
    }

    private void startMonitoring() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationTemperatureSent = false;
                notificationHumiditySent = false;
                fetchAndVerifyThresholds();
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

    private void checkAndSendRoomNotifications(Map<String, Object> thresholdsData) {
        if (thresholdsData.containsKey("minTemperature") && thresholdsData.containsKey("maxTemperature")
                && !notificationTemperatureSent) {
            float minRoomTemperature = parseFloatWithDefault(thresholdsData.get("minTemperature"));
            float maxRoomTemperature = parseFloatWithDefault(thresholdsData.get("maxTemperature"));
            checkRoomTemperatureInterval(minRoomTemperature, maxRoomTemperature);
        }

        if (thresholdsData.containsKey("minHumidity") && thresholdsData.containsKey("maxHumidity")
                && !notificationHumiditySent) {
            float minRoomHumidity = parseFloatWithDefault(thresholdsData.get("minHumidity"));
            float maxRoomHumidity = parseFloatWithDefault(thresholdsData.get("maxHumidity"));
            checkRoomHumidityInterval(minRoomHumidity, maxRoomHumidity);
        }
    }

    private void checkRoomTemperatureInterval(float minRoomTemperature, float maxRoomTemperature) {
        if (!Float.isNaN(currentTemperature) && (currentTemperature < minRoomTemperature || currentTemperature > maxRoomTemperature)) {
            showNotification("Temperature Alert", "Room temperature (" + currentTemperature + "ºC) is outside the specified interval", 1);
            notificationTemperatureSent = true;
        }
    }

    private void checkRoomHumidityInterval(float minRoomHumidity, float maxRoomHumidity) {
        if (!Float.isNaN(currentHumidity) && (currentHumidity < minRoomHumidity || currentHumidity > maxRoomHumidity)) {
            showNotification("Humidity Alert", "Room humidity (" + currentHumidity + "%)  is outside the specified interval", 1);
            notificationHumiditySent = true;
        }
    }

    private void checkAndSendPlantNotifications(Map<String, Object> plantData) {
        if (plantData.containsKey("min_temp") && plantData.containsKey("max_temp")) {
            float minTemperature = parseFloatWithDefault(plantData.get("min_temp"));
            float maxTemperature = parseFloatWithDefault(plantData.get("max_temp"));
            checkPlantTemperatureInterval(minTemperature, maxTemperature, plantData);
        }

        if (plantData.containsKey("min_humidity") && plantData.containsKey("max_humidity")) {
            float minHumidity = parseFloatWithDefault(plantData.get("min_humidity"));
            float maxHumidity = parseFloatWithDefault(plantData.get("max_humidity"));
            checkPlantHumidityInterval(minHumidity, maxHumidity, plantData);
        }
    }

    private void checkPlantTemperatureInterval(float minTemperature, float maxTemperature, Map<String, Object> plantData) {
        if (!Float.isNaN(currentTemperature) && (currentTemperature < minTemperature || currentTemperature > maxTemperature)) {
            showNotification("Temperature Alert", "(" + currentTemperature + "ºC) is outside the threshold for plant: " + getPlantName(plantData), getPlantNumber(plantData));
        }
    }

    private void checkPlantHumidityInterval(float minHumidity, float maxHumidity, Map<String, Object> plantData) {
        if (!Float.isNaN(currentHumidity) && (currentHumidity < minHumidity || currentHumidity > maxHumidity)) {
            showNotification("Humidity Alert", "(" + currentHumidity + "%) is outside the threshold for plant: " + getPlantName(plantData), getPlantNumber(plantData));
        }
    }

    private String getPlantName(Map<String, Object> plantData) {
        return Objects.requireNonNull(plantData.get("name")).toString();
    }

    private int getPlantNumber(Map<String, Object> plantData) {
        Object numberObject = plantData.get("number");
        try {
            return Integer.parseInt(String.valueOf(numberObject));
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    private void connectMQTT() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle failure
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
                    Log.i(TAG, "Subscribed to temperature topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle failure
                }
            });

            IMqttToken subToken2 = mqttAndroidClient.subscribe(humidityTopic, qos);
            subToken2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "Subscribed to humidity topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle failure
                }
            });
        } catch (MqttSecurityException e) {
            throw new RuntimeException(e);
        } catch (MqttException e) {
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

    private void updateMqttData(String topic, String payload) {
        dataRepository.updateData(topic, payload);
    }

    private void showNotification(String title, String message, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (title.equals("Temperature Alert")) {
            notificationManager.notify(notificationId, builder.build());
        } else if (title.equals("Humidity Alert")) {
            notificationManager.notify(notificationId + 1, builder.build());
        }
    }

    private void createNotificationChannel() {
        CharSequence name = "MyChannel";
        String description = "Temperature and Humidity Alerts";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
