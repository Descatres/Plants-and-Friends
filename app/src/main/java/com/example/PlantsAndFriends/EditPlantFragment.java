package com.example.PlantsAndFriends;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;

import java.util.Collections;
import java.util.List;

public class EditPlantFragment extends AppCompatActivity {

    private TextView minTemperatureTextView;
    private TextView maxTemperatureTextView;
    private TextView minHumidityTextView;
    private TextView maxHumidityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_plant);

        minTemperatureTextView = findViewById(R.id.minTemperature);
        maxTemperatureTextView = findViewById(R.id.maxTemperature);
        minHumidityTextView = findViewById(R.id.minHumidity);
        maxHumidityTextView = findViewById(R.id.maxHumidity);

        RangeSlider temperatureRangeSlider = findViewById(R.id.temperatureRangeSlider);
        RangeSlider humidityRangeSlider = findViewById(R.id.humidityRangeSlider);

        // Set initial values
        updateTemperatureRangeText(temperatureRangeSlider.getValues());
        updateHumidityRangeText(humidityRangeSlider.getValues());

        // Set listeners for the two RangeSliders
        temperatureRangeSlider.addOnChangeListener((slider, values, fromUser) -> {
            // Update the TextViews with the current temperature range values
            updateTemperatureRangeText(Collections.singletonList(values));
        });

        humidityRangeSlider.addOnChangeListener((slider, values, fromUser) -> {
            // Update the TextViews with the current humidity range values
            updateHumidityRangeText(Collections.singletonList(values));
        });
    }

    // update the TextViews with the current temperature and humidity values
    private void updateTemperatureRangeText(List<Float> values) {
        int minValue = values.get(0).intValue();
        int maxValue = values.get(1).intValue();

        String minRangeText = getString(R.string.min_temperature, minValue);
        String maxRangeText = getString(R.string.max_temperature, maxValue);

        minTemperatureTextView.setText(minRangeText);
        maxTemperatureTextView.setText(maxRangeText);
    }

    private void updateHumidityRangeText(List<Float> values) {
        int minValue = values.get(0).intValue();
        int maxValue = values.get(1).intValue();

        String minRangeText = getString(R.string.min_humidity, minValue);
        String maxRangeText = getString(R.string.max_humidity, maxValue);

        minHumidityTextView.setText(minRangeText);
        maxHumidityTextView.setText(maxRangeText);
    }
}
