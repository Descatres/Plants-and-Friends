package com.example.PlantsAndFriends;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
public class MqttViewModel extends ViewModel {
    private final MutableLiveData<String> temperatureLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> humidityLiveData = new MutableLiveData<>();

    // Add the following LiveData objects
    private LiveData<String> formattedTemperature;
    private LiveData<String> formattedHumidity;

    public void setDataRepository(DataRepository dataRepository) {

        // Initialize formattedTemperature and formattedHumidity LiveData
        formattedTemperature = Transformations.map(temperatureLiveData, temperature -> {
            // Perform any formatting logic here
            return String.format("%.2f°C", temperature);
        });

        formattedHumidity = Transformations.map(humidityLiveData, humidity -> {
            // Perform any formatting logic here
            return String.format("%.2f%%", humidity);
        });
    }

    public LiveData<String> getTemperatureData() {
        return temperatureLiveData;
    }

    public LiveData<String> getHumidityData() {
        return humidityLiveData;
    }

    // Add getters for formattedTemperature and formattedHumidity
    public LiveData<String> getFormattedTemperature() {
        return formattedTemperature;
    }

    public LiveData<String> getFormattedHumidity() {
        return formattedHumidity;
    }

    public void onSensorDataReceived(String temperature, String humidity) {
        // Update LiveData with new sensor values
        temperatureLiveData.postValue(temperature);
        humidityLiveData.postValue(humidity);
    }
}
