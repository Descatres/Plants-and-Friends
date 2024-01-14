package com.example.PlantsAndFriends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private static final String TAG = "PlantDetailsFragment";
    private StringBuilder consolidatedResultBuilder = new StringBuilder();
    public static final String READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 101;
    StorageReference storageReference;
    private Button saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plant_details, container, false);
        toolbar = view.findViewById(R.id.toolbar);

        //Plant Attributes
        nameEditText = view.findViewById(R.id.name);
        speciesEditText = view.findViewById(R.id.species);

        // Image
        plantImageView = view.findViewById(R.id.add_image);

        plantImageView.setOnClickListener(v -> {
            openGallery();
        });


        saveButton = view.findViewById(R.id.save);

        saveButton.setOnClickListener(v -> {
            if (getArguments() != null) {
                String plantNumber = getArguments().getString("plantNumber");
                boolean isCreate = getArguments().getBoolean("isCreate");
                if (plantNumber != null && !plantNumber.isEmpty()) {
                    if (selectedImageUri != null) {
                        uploadImage(selectedImageUri);
                    }
                    savePlantToLocalStorage(plantNumber, selectedImageUri, isCreate);
                    if (isNetworkConnected()) {
                        backupPlantToFirestore(plantNumber, () -> {
                            Log.d(TAG, "onMenuItemClick: " + consolidatedResultBuilder.toString());
                            mainHandler.post(() -> {
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
            boolean isCreate = getArguments().getBoolean("isCreate");

            if (!isCreate) {
                if (plantNumber != null && !plantNumber.isEmpty()) {
                    displayPlantFromLocalStorage(plantNumber);
                }
            }
        }


        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.plant_details_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                if (getArguments() != null) {
                    String plantNumber = getArguments().getString("plantNumber");
                    boolean isCreate = getArguments().getBoolean("isCreate");
                    if (plantNumber != null && !plantNumber.isEmpty()) {
                        if (selectedImageUri != null) {
                            uploadImage(selectedImageUri);
                        }
//                        else {
//                            // get the imageUri from the local storage
//                            executor.execute(() -> {
//                                selectedImageUri = getImageUriFromLocalStorage(plantNumber);
//                                uploadImage(selectedImageUri);
//                            });
//                        }

                        savePlantToLocalStorage(plantNumber, selectedImageUri, isCreate);
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
//                            backupPlantToFirestore(plantNumber, () -> {
//                                Log.d(TAG, "onMenuItemClick: " + consolidatedResultBuilder.toString());
//                                mainHandler.post(() -> {
//                                    // Only show the last message set to consolidatedResult
//                                    String consolidatedMessage = consolidatedResultBuilder.toString();
//                                    if (!consolidatedMessage.isEmpty()) {
//                                        Toast.makeText(requireContext(), consolidatedMessage, Toast.LENGTH_SHORT).show();
//                                        consolidatedResultBuilder.setLength(0);
//                                    }
//                                });
//                            });
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

        storageReference = FirebaseStorage.getInstance().getReference();


        return view;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (nameEditText.getText().toString().isEmpty() && speciesEditText.getText().toString().isEmpty() && plantDescriptionEditText.getText().toString().isEmpty()
                && temperatureRangeSlider.getValues().get(0) == -40 && temperatureRangeSlider.getValues().get(1) == 80
                && humidityRangeSlider.getValues().get(0) == 0 && humidityRangeSlider.getValues().get(1) == 100) {
            executor.execute(() -> {
                assert getArguments() != null;
                appDatabase.plantDao().deletePlantByNumber(getArguments().getString("plantNumber"));
            });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nameEditText.getText().toString().isEmpty() && speciesEditText.getText().toString().isEmpty() && plantDescriptionEditText.getText().toString().isEmpty()
                && temperatureRangeSlider.getValues().get(0) == -40 && temperatureRangeSlider.getValues().get(1) == 80
                && humidityRangeSlider.getValues().get(0) == 0 && humidityRangeSlider.getValues().get(1) == 100) {
            executor.execute(() -> {
                assert getArguments() != null;
                appDatabase.plantDao().deletePlantByNumber(getArguments().getString("plantNumber"));
            });
        }

    }

    private boolean isGalleryPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
            return requireContext().checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return requireContext().checkSelfPermission(READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Image Picker Gallery
    private void openGallery() {

        if (!isGalleryPermissionGranted()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
                ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{READ_MEDIA_IMAGES}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null) {
            // Handle the selected image URI
            selectedImageUri = data.getData();
            // if permission to read the image is negative dont load it
            loadImage(selectedImageUri);

        }
    }

    private void loadImage(Uri imageUri) {
        if (isGalleryPermissionGranted()) {
            Glide.with(this).load(imageUri).into(plantImageView);
        } else {
            Toast.makeText(requireContext(), "Enable permission to show images", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadImage(Uri imageUri) {
        Log.e(TAG, "uploadImage: " + imageUri);
        if (imageUri == null || !isNetworkConnected()) {
            Log.e(TAG, "uploadImage: " + "imageUri null or no internet connection");
            return;
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String currentUserUid = currentUser.getUid();

        assert getArguments() != null;
        String plantNumber = getArguments().getString("plantNumber");
        StorageReference ref = storageReference.child("images/" + currentUser + "/" + plantNumber);
        Log.e(TAG, "uploadImage: " + ref);
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Log.e(TAG, "uploadImage sucesso");
            if (isAdded()) {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Image uploaded to Firestore bucket", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "uploadImage sem sucesso");
            mainHandler.post(() -> {
                Toast.makeText(requireContext(), "Failed to upload to bucket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }


    // update the TextViews with the current temperature and humidity values
    private void updateTemperatureRangeText(List<Float> values) {
        if (values != null && values.size() >= 2) {
            float minValue = values.get(0);
            float maxValue = values.get(1);

            String minTemperature = getString(R.string.min_temperature, minValue);
            String maxTemperature = getString(R.string.max_temperature, maxValue);

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

            String minHumidity = getString(R.string.min_humidity, minValue);
            String maxHumidity = getString(R.string.max_humidity, maxValue);

            String minRangeText = "Min: " + minHumidity + "%";
            String maxRangeText = "Max: " + maxHumidity + "%";

            minHumidityTextView.setText(minRangeText);
            maxHumidityTextView.setText(maxRangeText);
        } else {
            minHumidityTextView.setText("");
            maxHumidityTextView.setText("");
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(requireContext());
    }

    private void createNewPlantInLocalStorage(String plantNumber, Runnable callback) {
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

                // Execute the callback after successful completion
                callback.run();
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
        if (imageUriString != null && isGalleryPermissionGranted()) {
            return Uri.parse(imageUriString);
        }
        return null;
    }

    private void savePlantToLocalStorage(String plantNumber, Uri imageUri, boolean isCreate) {
        executor.execute(() -> {
            Runnable afterCreateCallback = () -> {
                if (nameEditText.getText().toString().isEmpty()) {
                    if (isAdded()) {
                        mainHandler.post(() -> {
                            Toast.makeText(requireContext(), "Plant not saved. Name of plant required", Toast.LENGTH_SHORT).show();
                            Log.d("PlantDetailsFragment", "Plant not saved. Name of plant required");
                        });
                    }
                    return;
                }
                Log.e("PlantDetailsFragment", "savePlantToLocalStorage: " + plantNumber);
                appDatabase.plantDao().updatePlantName(plantNumber, nameEditText.getText().toString());
                appDatabase.plantDao().updatePlantSpecies(plantNumber, speciesEditText.getText().toString());
                appDatabase.plantDao().updatePlantDescription(plantNumber, plantDescriptionEditText.getText().toString());
                appDatabase.plantDao().updatePlantMinTemp(plantNumber, temperatureRangeSlider.getValues().get(0));
                appDatabase.plantDao().updatePlantMaxTemp(plantNumber, temperatureRangeSlider.getValues().get(1));
                appDatabase.plantDao().updatePlantMinHumidity(plantNumber, humidityRangeSlider.getValues().get(0));
                appDatabase.plantDao().updatePlantMaxHumidity(plantNumber, humidityRangeSlider.getValues().get(1));
                if (imageUri != null && isGalleryPermissionGranted()) {
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

                if (!isNetworkConnected()) {
                    if (isAdded()) {
                        mainHandler.post(() -> {
                            Toast.makeText(requireContext(), "Plant saved but failed to backup to Firestore (No internet connection)", Toast.LENGTH_SHORT).show();
                            Log.d("PlantDetailsFragment", "Plant saved but failed to backup to Firestore (No internet connection)");
                        });
                    }
                }
            };
            if (isCreate && !nameEditText.getText().toString().isEmpty()) {
                createNewPlantInLocalStorage(plantNumber, afterCreateCallback);
            } else {
                afterCreateCallback.run();
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
//                            if (isAdded()) {
//                                mainHandler.post(() -> {
//                                    consolidatedResultBuilder.append("Plant saved but failed to backup to Firestore");
//                                    callback.run();
//                                });
                            Log.d("PlantDetailsFragment", "Plant saved but failed to backup to Firestore (3)");
//                        }
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