package com.example.PlantsAndFriends;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DataRepository {
    private static DataRepository instance;
    private final MutableLiveData<String> temperatureData = new MutableLiveData<>();
    private final MutableLiveData<String> humidityData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> lightState = new MutableLiveData<>();


    private DataRepository() {
    }

    public static DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    public LiveData<String> getTemperatureData() {
        return temperatureData;
    }

    public LiveData<String> getHumidityData() {
        return humidityData;
    }

    public LiveData<Boolean> getLightState() {
        return lightState;
    }

    public void updateData(String topic, String payload) {
        // update LiveData based on the received MQTT data
        String temperatureTopic = "plants_and_friends_temperature_topic";
        String humidityTopic = "plants_and_friends_humidity_topic";
//        Log.e("DataRepository", "Received data on topic: " + topic + ", payload: " + payload);
        if (temperatureTopic.equals(topic)) {
            temperatureData.postValue(payload);
        } else if (humidityTopic.equals(topic)) {
            humidityData.postValue(payload);
        }
    }

    public void setLightState(boolean isLightOn) {
        lightState.setValue(isLightOn);
    }
}