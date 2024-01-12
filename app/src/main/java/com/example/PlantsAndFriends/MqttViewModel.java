package com.example.PlantsAndFriends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class MqttViewModel extends ViewModel {
    private DataRepository dataRepository;

    public void setDataRepository(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public LiveData<String> getTemperature() {
        return dataRepository.getTemperatureData();
    }

    public LiveData<String> getHumidity() {
        return dataRepository.getHumidityData();
    }
}