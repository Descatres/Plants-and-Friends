package com.example.PlantsAndFriends;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlertsFragment extends Fragment {
    private Toolbar toolbar;
    private TextView minTemperatureTextView;
    private String minTemperature;
    private TextView maxTemperatureTextView;
    private String maxTemperature;
    private TextView minHumidityTextView;
    private String minHumidity;
    private TextView maxHumidityTextView;
    private String maxHumidity;
    TextView warningInternetUnavailable;
    TextView warningAndroidVersion;

    TextView comboWarning;
    private RangeSlider temperatureRangeSlider;
    private RangeSlider humidityRangeSlider;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Button saveButton;
    private View save_layout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alerts, container, false);
        toolbar = view.findViewById(R.id.toolbar);


        minTemperatureTextView = view.findViewById(R.id.minTemperature);
        maxTemperatureTextView = view.findViewById(R.id.maxTemperature);
        minHumidityTextView = view.findViewById(R.id.minHumidity);
        maxHumidityTextView = view.findViewById(R.id.maxHumidity);

        // Temperature and Humidity Ranges
        temperatureRangeSlider = view.findViewById(R.id.temperatureRangeSlider);
        humidityRangeSlider = view.findViewById(R.id.humidityRangeSlider);
        updateTemperatureRangeText(temperatureRangeSlider.getValues());
        updateHumidityRangeText(humidityRangeSlider.getValues());

        // Set listeners for the two RangeSliders
        temperatureRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() >= 2) {
//                Log.d("From", values.get(0).toString());
//                Log.d("To", values.get(1).toString());

                updateTemperatureRangeText(values);
            }
        });

        humidityRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() >= 2) {
//                Log.d("From", values.get(0).toString());
//                Log.d("To", values.get(1).toString());

                updateHumidityRangeText(values);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fetchAndDisplayThresholds();

        saveButton = view.findViewById(R.id.save);

        saveButton.setOnClickListener(v -> {
            saveThresholdsFirestore();
        });

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.plant_details_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveThresholdsFirestore();
                return true;
            } else if (item.getItemId() == R.id.action_back) {
                navigateToHomepage();
                return true;
            } else {
                return false;
            }
        });

        warningInternetUnavailable = view.findViewById(R.id.warning_text);
        warningInternetUnavailable.setVisibility(View.GONE);
        warningAndroidVersion = view.findViewById(R.id.warning_text1);
        warningAndroidVersion.setVisibility(View.GONE);
        comboWarning = view.findViewById(R.id.warning_text2);
        comboWarning.setVisibility(View.GONE);


        if (!isNetworkConnected() && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            warningInternetUnavailable.setVisibility(View.VISIBLE);
        } else if (isNetworkConnected() && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            warningAndroidVersion.setVisibility(View.VISIBLE);
        } else if (!isNetworkConnected() && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            comboWarning.setVisibility(View.VISIBLE);
        }


        return view;
    }

    private void saveThresholdsFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();

            Map<String, Object> thresholds = new HashMap<>();
            thresholds.put("minTemperature", minTemperature);
            thresholds.put("maxTemperature", maxTemperature);
            thresholds.put("minHumidity", minHumidity);
            thresholds.put("maxHumidity", maxHumidity);
            executor.execute(() -> {
                db.collection("users").document(currentUserUid).collection("thresholds")
                        .document("sensorThresholds")
                        .set(thresholds)
                        .addOnSuccessListener(aVoid -> {
                            if (isAdded()) {
                                mainHandler.post(() -> {
                                    Toast.makeText(requireContext(), "Thresholds saved successfully", Toast.LENGTH_SHORT).show();
                                });
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (isAdded()) {
                                mainHandler.post(() -> {
                                    Toast.makeText(requireContext(), "Error: thresholds not saved", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
            });
        }
    }

    private void fetchAndDisplayThresholds() {
        if (!isNetworkConnected()) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

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
                                updateUIWithThresholds(thresholdsData);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AlertsFragment", "Error fetching thresholds from Firestore", e);
                    });
        }
    }

    private void updateUIWithThresholds(Map<String, Object> thresholdsData) {
        // Update UI elements with retrieved thresholds
        if (thresholdsData.containsKey("minTemperature")) {
            float minTemperature = parseFloatWithDefault(thresholdsData.get("minTemperature"));
            float maxTemperature = parseFloatWithDefault(thresholdsData.get("maxTemperature"));
            List<Float> temperatureValues = Arrays.asList(minTemperature, maxTemperature);
            temperatureRangeSlider.setValues(temperatureValues);
            updateTemperatureRangeText(temperatureValues);
        }

        if (thresholdsData.containsKey("minHumidity")) {
            float minHumidity = parseFloatWithDefault(thresholdsData.get("minHumidity"));
            float maxHumidity = parseFloatWithDefault(thresholdsData.get("maxHumidity"));
            List<Float> humidityValues = Arrays.asList(minHumidity, maxHumidity);
            humidityRangeSlider.setValues(humidityValues);
            updateHumidityRangeText(humidityValues);
        }
    }

    private void updateTemperatureRangeText(List<Float> values) {
        if (values != null && values.size() >= 2) {
            float minValue = values.get(0);
            float maxValue = values.get(1);

            minTemperature = getString(R.string.min_temperature, minValue);
            maxTemperature = getString(R.string.max_temperature, maxValue);

            String minRangeText = "Min: " + minTemperature + "°C";
            String maxRangeText = "Max: " + maxTemperature + "°C";

            minTemperatureTextView.setText(minRangeText);
            maxTemperatureTextView.setText(maxRangeText);
        } else {
            minTemperatureTextView.setText("");
            maxTemperatureTextView.setText("");
        }
    }

    private void updateHumidityRangeText(List<Float> values) {
        if (values != null && values.size() >= 2) {
            float minValue = values.get(0);
            float maxValue = values.get(1);

            minHumidity = getString(R.string.min_humidity, minValue);
            maxHumidity = getString(R.string.max_humidity, maxValue);

            String minRangeText = "Min: " + minHumidity + "%";
            String maxRangeText = "Max: " + maxHumidity + "%";

            minHumidityTextView.setText(minRangeText);
            maxHumidityTextView.setText(maxRangeText);
        } else {
            minHumidityTextView.setText("");
            maxHumidityTextView.setText("");
        }
    }

    private void navigateToHomepage() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private float parseFloatWithDefault(Object value) {
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (NumberFormatException e) {
            return Float.NaN;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // remove any remaining callbacks to avoid memory leaks
        handler.removeCallbacksAndMessages(null);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                Log.d("PlantDetailsFragment", "Network connection detected");
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
            }
        }
        Log.d("PlantDetailsFragment", "No network connection");
        return false;
    }

}
