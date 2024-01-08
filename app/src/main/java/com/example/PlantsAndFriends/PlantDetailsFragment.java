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
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private static final String TAG = "PlantDetailsFragment";

    private final AtomicInteger return_value = new AtomicInteger(1);

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
            int plantId = getArguments().getInt("plantId");
            Log.d("GetArguments", "GetArguments: " + plantId);

            displayPlantFromLocalStorage(plantId);
        }

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.plant_details_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                if (getArguments() != null) {
                    int plantId = getArguments().getInt("plantId");
//                    if (isNetworkConnected()) {
//                        savePlantToFirestore();
//                    }

                    savePlantToLocalStorage(plantId, selectedImageUri);


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
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null) {
            // Handle the selected image URI
            selectedImageUri = data.getData();
            loadImage(selectedImageUri);
        }
    }

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
    }

    private void displayPlantFromLocalStorage(int plantId) {
        executor.execute(() -> {
            // check if the plant exists in the local storage by plantId
            if (appDatabase.plantDao().getPlantByNumber(plantId) == null) {
                Log.d("PlantDetailsFragment", "Plant does not exist in local storage");
                return;
            }
            String plantName = appDatabase.plantDao().getPlantByNumber(plantId).getName();
            String plantContent = appDatabase.plantDao().getPlantByNumber(plantId).getDescription();
            String plantSpecies = appDatabase.plantDao().getPlantByNumber(plantId).getSpecies();
            double plantMinTemp = appDatabase.plantDao().getPlantByNumber(plantId).getMin_temp();
            double plantMaxTemp = appDatabase.plantDao().getPlantByNumber(plantId).getMax_temp();
            double plantMinHumidity = appDatabase.plantDao().getPlantByNumber(plantId).getMin_humidity();
            double plantMaxHumidity = appDatabase.plantDao().getPlantByNumber(plantId).getMax_humidity();
            Uri imageUri = getImageUriFromLocalStorage(String.valueOf(plantId));

            mainHandler.post(() -> {
                nameEditText.setText(plantName);
                speciesEditText.setText(plantSpecies);
                plantDescriptionEditText.setText(plantContent);
                temperatureRangeSlider.setValues((float) plantMinTemp, (float) plantMaxTemp);
                humidityRangeSlider.setValues((float) plantMinHumidity, (float) plantMaxHumidity);
                if (imageUri != null) {
                    loadImage(imageUri);
                }
            });
        });
    }

    private Uri getImageUriFromLocalStorage(String plantId) {
        String imageUriString = appDatabase.plantDao().getPlantImageUri(plantId);
        if (imageUriString != null) {
            return Uri.parse(imageUriString);
        }
        return null;
    }

    private void savePlantToLocalStorage(int plantId, Uri imageUri) {
        executor.execute(() -> {
            appDatabase.plantDao().updatePlantName(plantId, nameEditText.getText().toString());
            appDatabase.plantDao().updatePlantSpecies(plantId, speciesEditText.getText().toString());
            appDatabase.plantDao().updatePlantMinTemp(plantId, temperatureRangeSlider.getValues().get(0));
            appDatabase.plantDao().updatePlantMaxTemp(plantId, temperatureRangeSlider.getValues().get(1));
            appDatabase.plantDao().updatePlantMinHumidity(plantId, humidityRangeSlider.getValues().get(0));
            appDatabase.plantDao().updatePlantMaxHumidity(plantId, humidityRangeSlider.getValues().get(1));
            appDatabase.plantDao().updatePlantDescription(plantId, plantDescriptionEditText.getText().toString());
            if (imageUri != null) {
                appDatabase.plantDao().updatePlantImageUri(plantId, String.valueOf(imageUri));
            }
            // check if the plant name is set
            if (nameEditText.getText().toString().isEmpty()) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Plant not saved. Plant of name required", Toast.LENGTH_SHORT).show());
                return;
            }

            if (!isNetworkConnected()) {
                Log.d("PlantDetailsFragment", String.format("Plant %s saved successfully", plantId));
                mainHandler.post(() -> Toast.makeText(requireContext(), "Plant saved but failed to backup to Firestore", Toast.LENGTH_SHORT).show());
            } else {
                Log.d("PlantDetailsFragment", String.format("Plant %s saved successfully (1)", plantId));
                backupPlantToFirestore(plantId);
                if (return_value.get() == 1) {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Plant saved and backed up to Firestore", Toast.LENGTH_SHORT).show());
                } else if (return_value.get() == 2) {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Plant saved but failed to backup to Firestore (1)", Toast.LENGTH_SHORT).show());
                } else if (return_value.get() == 3) {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Plant saved and updated to Firestore", Toast.LENGTH_SHORT).show());
                }

            }
        });
    }

    private void backupPlantToFirestore(int plantId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No user logged in");
            return;
        }

        String currentUserUid = currentUser.getUid();

        Map<String, Object> plant = new HashMap<>();
        plant.put("number", plantId);
        plant.put("name", nameEditText.getText().toString());
        plant.put("species", speciesEditText.getText().toString());
        plant.put("min_temp", (double) temperatureRangeSlider.getValues().get(0));
        plant.put("max_temp", (double) temperatureRangeSlider.getValues().get(1));
        plant.put("min_humidity", (double) humidityRangeSlider.getValues().get(0));
        plant.put("max_humidity", (double) humidityRangeSlider.getValues().get(1));
        plant.put("description", plantDescriptionEditText.getText().toString());
        plant.put("imgUri", getImageUriFromLocalStorage(String.valueOf(plantId)));
        Log.d(TAG, "imgUri: " + getImageUriFromLocalStorage(String.valueOf(plantId)));
        // check if the number of the plant already exists in firestore and save it to firebase only if it does not exist
        db.collection("users").document(currentUserUid).collection("plants").document(String.valueOf(plantId)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Log.d(TAG, "Plant already exists in Firestore");
                    return_value.set(1);
                } else {
                    db.collection("users").document(currentUserUid).collection("plants").document(String.valueOf(plantId)).set(plant).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.d(TAG, "Plant saved successfully to Firestore");
                            return_value.set(1);
                        } else {
                            Log.d(TAG, "Plant failed to save to Firestore");
                            return_value.set(2);
                        }
                    });
                }
            } else {
                Log.d(TAG, "Failed to check if plant exists in Firestore");
                return_value.set(2);
            }
        });

        // update the plant in firestore if any of the fields have changed on a plant that already exists in firestore
        db.collection("users").document(currentUserUid).collection("plants").document(String.valueOf(plantId)).update(plant).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Plant updated successfully in Firestore");
                return_value.set(3);
            } else {
                Log.d(TAG, "Plant failed to update in Firestore");
                return_value.set(2);
            }
        });
        Log.d(TAG, "return_value: " + return_value.get());
    }

    private void navigateToHomepage() {
        requireActivity().getSupportFragmentManager().popBackStack();
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