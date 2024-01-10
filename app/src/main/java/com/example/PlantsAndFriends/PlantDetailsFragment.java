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
import java.util.concurrent.CountDownLatch;
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

    private StringBuilder consolidatedResultBuilder = new StringBuilder();


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

        plantImageView.setOnClickListener(v -> {
            // Handle the click event to open the image picker
            openGallery();
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
        temperatureRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() >= 2) {
                Log.d("From", values.get(0).toString());
                Log.d("To", values.get(1).toString());

                // Call your method to update humidity range text
                updateTemperatureRangeText(values);
            }
        });

        humidityRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() >= 2) {
                Log.d("From", values.get(0).toString());
                Log.d("To", values.get(1).toString());

                // Call your method to update humidity range text
                updateHumidityRangeText(values);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        if (getArguments() != null) {
            String plantNumber = getArguments().getString("plantNumber");
            Log.d("GetArguments", "GetArguments: " + plantNumber);

            if (plantNumber != null && !plantNumber.isEmpty() && nameEditText.getText().toString().isEmpty() && speciesEditText.getText().toString().isEmpty() && plantDescriptionEditText.getText().toString().isEmpty()) {
                createNewPlantInLocalStorage(getArguments().getString("plantNumber"));
            }
            displayPlantFromLocalStorage(plantNumber);
        }

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.plant_details_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                if (getArguments() != null) {
                    String plantNumber = getArguments().getString("plantNumber");
                    if (plantNumber != null && !plantNumber.isEmpty()) {
                        savePlantToLocalStorage(plantNumber, selectedImageUri, () -> {
                            if (isAdded() && !isNetworkConnected()) {
                                mainHandler.post(() -> {
                                    Toast.makeText(requireContext(), consolidatedResultBuilder.toString(), Toast.LENGTH_SHORT).show();
                                    navigateToHomepage();
                                });
                            }

                        });
                        if (isNetworkConnected()) {
                            backupPlantToFirestore(plantNumber, () -> {
                                Log.d(TAG, "onMenuItemClick: " + consolidatedResultBuilder.toString());
                                mainHandler.post(() -> {
                                    // Only show the last message set to consolidatedResult
                                    String consolidatedMessage = consolidatedResultBuilder.toString();
                                    if (!consolidatedMessage.isEmpty()) {
                                        Toast.makeText(requireContext(), consolidatedMessage, Toast.LENGTH_SHORT).show();
                                        consolidatedResultBuilder.setLength(0);
                                    }
                                });
                            });
                        }
                    }
                }
                return true;
            } else if (item.getItemId() == R.id.action_back) {
                // TODO verify if there is a selected image or not (otherwise, if it is only saved the name and image and the name is deleted, the plant will not be deleted entirely, as opposed to the other fields
                //  check for (selectedImageUri == null or empty) won't work;
                //  something like (getImageUriFromLocalStorage(getArguments().getString("plantNumber")) == null) would but it locks the main thread
                if (nameEditText.getText().toString().isEmpty() && speciesEditText.getText().toString().isEmpty() && plantDescriptionEditText.getText().toString().isEmpty()
                        && temperatureRangeSlider.getValues().get(0) == -40 && temperatureRangeSlider.getValues().get(1) == 80 && humidityRangeSlider.getValues().get(0) == 0 && humidityRangeSlider.getValues().get(1) == 100) {
                    executor.execute(() -> {
                        appDatabase.plantDao().deletePlantByNumber(getArguments().getString("plantNumber"));
                    });
                }
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

    private void createNewPlantInLocalStorage(String plantNumber) {
        executor.execute(() -> {
            if (appDatabase.plantDao().getPlantByNumber(plantNumber) != null) {
                Log.d(TAG, "createNewPlantInLocalStorage: " + "Plant already exists");
                return;
            }

            try {
                PlantEntity plantEntity = new PlantEntity();
                plantEntity.setNumber(plantNumber);
                plantEntity.setName("");
                plantEntity.setSpecies("");
                plantEntity.setMin_temp(-40);
                plantEntity.setMax_temp(80);
                plantEntity.setMin_humidity(0);
                plantEntity.setMax_humidity(100);
                plantEntity.setDescription("");
                appDatabase.plantDao().insert(plantEntity);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void displayPlantFromLocalStorage(String plantNumber) {
        executor.execute(() -> {
            // check if the plant exists in the local storage by plantNumber
            if (appDatabase.plantDao().getPlantByNumber(plantNumber) == null) {
                Log.d("PlantDetailsFragment", "Plant does not exist in local storage");
                return;
            }
            String plantName = appDatabase.plantDao().getPlantByNumber(plantNumber).getName();
            String plantContent = appDatabase.plantDao().getPlantByNumber(plantNumber).getDescription();
            String plantSpecies = appDatabase.plantDao().getPlantByNumber(plantNumber).getSpecies();
            double plantMinTemp = appDatabase.plantDao().getPlantByNumber(plantNumber).getMin_temp();
            double plantMaxTemp = appDatabase.plantDao().getPlantByNumber(plantNumber).getMax_temp();
            double plantMinHumidity = appDatabase.plantDao().getPlantByNumber(plantNumber).getMin_humidity();
            double plantMaxHumidity = appDatabase.plantDao().getPlantByNumber(plantNumber).getMax_humidity();
            Uri imageUri = getImageUriFromLocalStorage(plantNumber);
            if (isAdded()) {
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
            }
        });
    }

    private Uri getImageUriFromLocalStorage(String plantNumber) {
        String imageUriString = appDatabase.plantDao().getPlantImageUri(plantNumber);
        if (imageUriString != null) {
            return Uri.parse(imageUriString);
        }
        return null;
    }

    private void savePlantToLocalStorage(String plantNumber, Uri imageUri, Runnable callback) {
        executor.execute(() -> {
            if (nameEditText.getText().toString().isEmpty()) {
                if (isAdded()) {
                    mainHandler.post(() -> {
                        consolidatedResultBuilder.append("Plant not saved. Name of plant required");
                        callback.run();
                    });
                    Log.d("PlantDetailsFragment", "Plant not saved. Name of plant required");
                }
                return;
            }
            Log.e("PlantDetailsFragment", "savePlantToLocalStorage: " + plantNumber);
            appDatabase.plantDao().updatePlantName(plantNumber, nameEditText.getText().toString());
            appDatabase.plantDao().updatePlantSpecies(plantNumber, speciesEditText.getText().toString());
            appDatabase.plantDao().updatePlantMinTemp(plantNumber, temperatureRangeSlider.getValues().get(0));
            appDatabase.plantDao().updatePlantMaxTemp(plantNumber, temperatureRangeSlider.getValues().get(1));
            appDatabase.plantDao().updatePlantMinHumidity(plantNumber, humidityRangeSlider.getValues().get(0));
            appDatabase.plantDao().updatePlantMaxHumidity(plantNumber, humidityRangeSlider.getValues().get(1));
            appDatabase.plantDao().updatePlantDescription(plantNumber, plantDescriptionEditText.getText().toString());
            if (imageUri != null) {
                // check if the imageUri leads to a valid image on the phone and, if so, save it to the local storage
//                try {
//                    MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                if (isAdded()) {
//                    mainHandler.post(() -> {
//                        consolidatedResultBuilder.append("Plant not saved. Invalid image!");
//                        callback.run();
//                    });
//                    Log.d("PlantDetailsFragment", "Plant not saved. Invalid image!");
//                }
//                    return;
//                }
                appDatabase.plantDao().updatePlantImageUri(plantNumber, String.valueOf(imageUri));
            }
            // check if the plant name is set

            if (!isNetworkConnected()) {
                if (isAdded()) {
                    mainHandler.post(() -> {
                        consolidatedResultBuilder.append("Plant saved but failed to backup to Firestore (No internet connection)");
                        callback.run();
                    });
                    Log.d("PlantDetailsFragment", "Plant saved but failed to backup to Firestore (No internet connection)");
                }
            }
        });
    }

    private void backupPlantToFirestore(String plantNumber, Runnable callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No user logged in");
            return;
        }

        String currentUserUid = currentUser.getUid();

        Map<String, Object> plant = new HashMap<>();
        executor.execute(() -> {
                    if (nameEditText.getText().toString().isEmpty()) {
                        Log.d(TAG, "Plant not saved. Name of plant required (online)");
                        return;
                    }
                    plant.put("number", plantNumber);
                    plant.put("name", nameEditText.getText().toString());
                    plant.put("species", speciesEditText.getText().toString());
                    plant.put("min_temp", (double) temperatureRangeSlider.getValues().get(0));
                    plant.put("max_temp", (double) temperatureRangeSlider.getValues().get(1));
                    plant.put("min_humidity", (double) humidityRangeSlider.getValues().get(0));
                    plant.put("max_humidity", (double) humidityRangeSlider.getValues().get(1));
                    plant.put("description", plantDescriptionEditText.getText().toString());
                    plant.put("imgUri", getImageUriFromLocalStorage(plantNumber));
                    Log.d(TAG, "imgUri online: " + getImageUriFromLocalStorage(plantNumber));

                    // check if the number of the plant already exists in firestore and save it to firebase only if it does not exist
                    db.collection("users").document(currentUserUid).collection("plants").document(plantNumber).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                Log.d(TAG, "Plant exists in Firestore, only updates will be done");
                            } else {
                                db.collection("users").document(currentUserUid).collection("plants").document(plantNumber).set(plant).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (isAdded()) {
                                            mainHandler.post(() -> {
                                                consolidatedResultBuilder.append("Plant saved and backed up to Firestore");
                                                callback.run();
                                            });
                                            Log.d("PlantDetailsFragment", "Plant saved and backed up to Firestore");
                                        }
                                    } else {
                                        if (isAdded()) {
                                            mainHandler.post(() -> {
                                                consolidatedResultBuilder.append("Plant saved but failed to backup to Firestore");
                                                callback.run();
                                            });
                                            Log.d("PlantDetailsFragment", "Plant saved but failed to backup to Firestore (1)");
                                        }
                                    }
                                });
                            }
                        }
//                        else {
//                            if (isAdded()) {
//                                mainHandler.post(() -> {
//                                    consolidatedResultBuilder.append("Plant saved but failed to backup to Firestore");
//                                    callback.run();
//                                });
//                                Log.d("PlantDetailsFragment", "Plant saved but failed to backup to Firestore (2)");
//                            }
//                        }
                    });

                    // update the plant in firestore if any of the fields have changed on a plant that already exists in firestore
                    db.collection("users").document(currentUserUid).collection("plants").document(plantNumber).update(plant).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (isAdded()) {
                                mainHandler.post(() -> {
                                    consolidatedResultBuilder.append("Plant saved and updated to Firestore");
                                    callback.run();
                                });
                                Log.d("PlantDetailsFragment", "Plant saved and updated to Firestore");
                            }
                        } else {
                            if (isAdded()) {
                                mainHandler.post(() -> {
                                    consolidatedResultBuilder.append("Plant saved but failed to backup to Firestore");
                                    callback.run();
                                });
                                Log.d("PlantDetailsFragment", "Plant saved but failed to backup to Firestore (3)");
                            }
                        }
                    });
                }
        );
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