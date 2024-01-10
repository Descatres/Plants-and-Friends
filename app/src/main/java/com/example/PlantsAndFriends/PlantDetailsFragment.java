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
    private EditText plantDescriptionEditText;
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

        plantDescriptionEditText = view.findViewById(R.id.plantsEditText); // Plant Description

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
                        savePlantToLocalStorage(plantNumber, selectedImageUri);
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
                if (nameEditText.getText().toString().isEmpty() && speciesEditText.getText().toString().isEmpty() && plantDescriptionEditText.getText().toString().isEmpty()) {
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
            Uri imageUri = getImageUriFromLocalStorage(plantNumber);
            if (isAdded()) {
                mainHandler.post(() -> {
                    nameEditText.setText(plantName);
                    speciesEditText.setText(plantSpecies);
                    plantDescriptionEditText.setText(plantContent);
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

    private void savePlantToLocalStorage(String plantNumber, Uri imageUri) {
        executor.execute(() -> {
            if (nameEditText.getText().toString().isEmpty()) {
                if (isAdded()) {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Plant not saved. Name of plant required", Toast.LENGTH_SHORT).show();
                    });
                    Log.d("PlantDetailsFragment", "Plant not saved. Name of plant required");
                }
                return;
            }
            Log.e("PlantDetailsFragment", "savePlantToLocalStorage: " + plantNumber);
            appDatabase.plantDao().updatePlantName(plantNumber, nameEditText.getText().toString());
            appDatabase.plantDao().updatePlantSpecies(plantNumber, speciesEditText.getText().toString());
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
                        Toast.makeText(requireContext(), "Plant saved but failed to backup to Firestore (No internet connection)", Toast.LENGTH_SHORT).show();
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