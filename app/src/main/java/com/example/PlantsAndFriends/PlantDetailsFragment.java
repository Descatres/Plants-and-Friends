package com.example.PlantsAndFriends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.bumptech.glide.Glide;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlantDetailsFragment extends Fragment {
    private Toolbar toolbar;
    private EditText nameEditText;
    private EditText speciesEditText;
    private ImageView plantImageView;
    private TextView minTemperatureTextView;
    private TextView maxTemperatureTextView;
    private TextView minHumidityTextView;
    private TextView maxHumidityTextView;
    private EditText plantDescriptionEditText;
    private RangeSlider temperatureRangeSlider;
    private RangeSlider humidityRangeSlider;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase appDatabase;
    private LiveData<List<PlantEntity>> localPlants;
    private static final int PICK_IMAGE_REQUEST = 1;

    private static final String TAG = "PlantDetailsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_plant, container, false);
        toolbar = view.findViewById(R.id.toolbar);

        //Plant Attributes
        nameEditText = view.findViewById(R.id.name);
        speciesEditText = view.findViewById(R.id.species);

        // Image

        plantImageView = view.findViewById(R.id.add_image);

        plantImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event to open the image picker
                openGallery();
            }
        });


        minTemperatureTextView = view.findViewById(R.id.minTemperature);
        maxTemperatureTextView = view.findViewById(R.id.maxTemperature);
        minHumidityTextView = view.findViewById(R.id.minHumidity);
        maxHumidityTextView = view.findViewById(R.id.maxHumidity);
        plantDescriptionEditText = view.findViewById(R.id.plantsEditText); // Plant Description

        // Temperature and Humidity Ranges
        temperatureRangeSlider = view.findViewById(R.id.temperatureRangeSlider);
        humidityRangeSlider = view.findViewById(R.id.humidityRangeSlider);
        updateTemperatureRangeText(temperatureRangeSlider.getValues());
        updateHumidityRangeText(humidityRangeSlider.getValues());

        // Set listeners for the two RangeSliders
        temperatureRangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> values = slider.getValues();
                if (values.size() >= 2) {
                    Log.d("From", values.get(0).toString());
                    Log.d("To", values.get(1).toString());

                    // Call your method to update humidity range text
                    updateTemperatureRangeText(values);
                }
            }
        });

        humidityRangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> values = slider.getValues();
                if (values.size() >= 2) {
                    Log.d("From", values.get(0).toString());
                    Log.d("To", values.get(1).toString());

                    // Call your method to update humidity range text
                    updateHumidityRangeText(values);
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            String plantNumber = getArguments().getString("plantNumber");
            String plantId = getArguments().getString("plantId");
            Log.d("GetArguments", "GetArguments: " + plantNumber);
            Log.d("GetArguments", "GetArguments: " + plantId);
        }

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.plant_details_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                if (getArguments() != null) {
                    String plantNumber = getArguments().getString("plantNumber");
                    String plantId = getArguments().getString("plantId");

//                    if (isNetworkConnected()) {
//                        if (plantId != null && !plantId.isEmpty()) {
//                            savePlantToFirestore(plantId);
//                        }
//                    }

                    if (plantNumber != null && !plantNumber.isEmpty()) {
                        savePlantToLocalStorage(plantNumber);
                    }

                }
                return true;
            } else if (item.getItemId() == R.id.action_back) {
                navigateToHomepage();
                return true;
            } else {
                return false;
            }
        });

        return view;
    }


    // Image Picker Gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            if (data != null) {
                // Handle the selected image URI
                Uri selectedImageUri = data.getData();
                loadImage(selectedImageUri);
            }
        }
    }

    // Method to load the selected image into the ImageView
    private void loadImage(Uri imageUri) {
        Glide.with(this).load(imageUri).into(plantImageView);
    }


    // update the TextViews with the current temperature and humidity values
    private void updateTemperatureRangeText(List<Float> values) {
        if (values != null && values.size() >= 2) {
            float minTemperature = values.get(0);
            float maxTemperature = values.get(1);

            String minRangeText = getString(R.string.min_temperature, minTemperature);
            String maxRangeText = getString(R.string.max_temperature, maxTemperature);

            // TODO set local min and max temperature values on localPlants


            minTemperatureTextView.setText(minRangeText);
            maxTemperatureTextView.setText(maxRangeText);
        } else {
            // Handle the case where there are not enough values in the list
            minTemperatureTextView.setText("");
            maxTemperatureTextView.setText("");
        }
    }


    private void updateHumidityRangeText(List<Float> values) {
        if (values != null && values.size() >= 2) {
            float minValue = values.get(0);
            float maxValue = values.get(1);

            String minRangeText = getString(R.string.min_humidity, minValue);
            String maxRangeText = getString(R.string.max_humidity, maxValue);

            // TODO set local min and max humidity values on localPlants

            minHumidityTextView.setText(minRangeText);
            maxHumidityTextView.setText(maxRangeText);
        } else {
            // Handle the case where there are not enough values in the list
            minHumidityTextView.setText("");
            maxHumidityTextView.setText("");
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(requireContext());
        localPlants = appDatabase.plantDao().getAllPlants();
    }

    private String getCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return "";
        }

        return currentUser.getUid();
    }


//    private void displayPlantTitleFromFirestore(String plantId) {
//        String currentUserUid = getCurrentUser();
//        executor.execute(() -> {
//            db.collection("users")
//                    .document(currentUserUid)
//                    .collection("plants")
//                    .document(plantId)
//                    .get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            String plantTitle = documentSnapshot.getString("title");
//                            Log.d("Title", "Plant title: " + plantTitle);
//                            mainHandler.post(() -> toolbar.setTitle(plantTitle));
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e("PlantDetailsFragment", "Error while fetching plant title: " + e.getMessage());
//                    });
//        });
//    }

    private void displayDescriptionFromLocalStorage(String plantNumber) {
        executor.execute(() -> {
            String plantContent = appDatabase.plantDao().getPlantByNumber(plantNumber).getDescription();
            mainHandler.post(() -> plantDescriptionEditText.setText(plantContent));
        });
    }

//    private void displayContentFromFirestore(String plantNumber) {
//        String currentUserUid = getCurrentUser();
//
//        executor.execute(() -> {
//            db.collection("users")
//                    .document(currentUserUid)
//                    .collection("plants")
//                    .document(plantNumber)
//                    .get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            String plantContent = documentSnapshot.getString("content");
//                            mainHandler.post(() -> plantContentEditText.setText(plantContent));
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e("PlantDetailsFragment", "Error while fetching plant content: " + e.getMessage());
//                    });
//        });
//    }

    private void savePlantToLocalStorage(String plantNumber) {
        executor.execute(() -> {
            appDatabase.plantDao().updatePlantName(plantNumber, nameEditText.getText().toString());
//            appDatabase.plantDao().updatePlantSpecies(plantNumber, speciesEditText.getText().toString());
//            appDatabase.plantDao().updatePlantMinTemperature(plantNumber, temperatureRangeSlider.getValues().get(0));
//            appDatabase.plantDao().updatePlantMaxTemperature(plantNumber, temperatureRangeSlider.getValues().get(1));
//            appDatabase.plantDao().updatePlantMinHumidity(plantNumber, humidityRangeSlider.getValues().get(0));
//            appDatabase.plantDao().updatePlantMaxHumidity(plantNumber, humidityRangeSlider.getValues().get(1));
            appDatabase.plantDao().updatePlantDescription(plantNumber, plantDescriptionEditText.getText().toString());
            mainHandler.post(() -> Toast.makeText(requireContext(), "Plant saved successfully", Toast.LENGTH_SHORT).show());
        });
    }

//    private void savePlantToFirestore(String plantId) {
//        String currentUserUid = getCurrentUser();
//
//        executor.execute(() -> {
//            db.collection("users")
//                    .document(currentUserUid)
//                    .collection("plants")
//                    .document(plantId)
//                    .update("content", plantContentEditText.getText().toString())
//                    .addOnSuccessListener(aVoid -> {
//                        mainHandler.post(() -> Toast.makeText(requireContext(), "Plant saved successfully", Toast.LENGTH_SHORT).show());
//                    })
//                    .addOnFailureListener(e -> {
//                        mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to save the plant: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//                    });
//        });
//    }

    private void navigateToHomepage() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
            }
        }

        return false;
    }
}