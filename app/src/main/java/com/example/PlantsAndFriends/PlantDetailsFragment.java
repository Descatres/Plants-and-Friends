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
    private EditText noteContentEditText;
    private RangeSlider temperatureRangeSlider;
    private RangeSlider humidityRangeSlider;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase appDatabase;
    private LiveData<List<NoteEntity>> localNotes;
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
        noteContentEditText = view.findViewById(R.id.plantsEditText); // Plant Description

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
            String noteNumber = getArguments().getString("noteNumber");
            String noteId = getArguments().getString("noteId");
            Log.d("GetArguments", "GetArguments: " + noteNumber);
            Log.d("GetArguments", "GetArguments: " + noteId);

            if (isNetworkConnected()) {
                if (noteId != null && !noteId.isEmpty()) {
                    displayNoteTitleFromFirestore(noteId);
                    displayContentFromFirestore(noteId);
                }
            } else {
                if (noteNumber != null && !noteNumber.isEmpty()) {
                    displayNoteTitleFromLocalStorage(noteNumber);
                    displayContentFromLocalStorage(noteNumber);
                }
            }
        }

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.note_details_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                if (getArguments() != null) {
                    String noteNumber = getArguments().getString("noteNumber");
                    String noteId = getArguments().getString("noteId");

                    if (isNetworkConnected()) {
                        if (noteId != null && !noteId.isEmpty()) {
                            saveNoteToFirestore(noteId);
                        }
                    }

                    if (noteNumber != null && !noteNumber.isEmpty()) {
                        saveNoteToLocalStorage(noteNumber);
                    }

                }
                return true;
            } else if (item.getItemId() == R.id.action_back) {
                navigateToNotesRepoFragment();
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
        localNotes = appDatabase.noteDao().getAllNotes();
    }

    private String getCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return "";
        }

        return currentUser.getUid();
    }

    private void displayNoteTitleFromLocalStorage(String noteNumber) {
        executor.execute(() -> {
            String noteTitle = appDatabase.noteDao().getNoteByNumber(noteNumber).getTitle();
            mainHandler.post(() -> toolbar.setTitle(noteTitle));
        });
    }

    private void displayNoteTitleFromFirestore(String noteId) {
        String currentUserUid = getCurrentUser();
        executor.execute(() -> {
            db.collection("users")
                    .document(currentUserUid)
                    .collection("notes")
                    .document(noteId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String noteTitle = documentSnapshot.getString("title");
                            Log.d("Title", "Note title: " + noteTitle);
                            mainHandler.post(() -> toolbar.setTitle(noteTitle));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NoteDetailsFragment", "Error while fetching note title: " + e.getMessage());
                    });
        });
    }

    private void displayContentFromLocalStorage(String noteNumber) {
        executor.execute(() -> {
            String noteContent = appDatabase.noteDao().getNoteByNumber(noteNumber).getContent();
            mainHandler.post(() -> noteContentEditText.setText(noteContent));
        });
    }

    private void displayContentFromFirestore(String noteNumber) {
        String currentUserUid = getCurrentUser();

        executor.execute(() -> {
            db.collection("users")
                    .document(currentUserUid)
                    .collection("notes")
                    .document(noteNumber)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String noteContent = documentSnapshot.getString("content");
                            mainHandler.post(() -> noteContentEditText.setText(noteContent));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NoteDetailsFragment", "Error while fetching note content: " + e.getMessage());
                    });
        });
    }

    private void saveNoteToLocalStorage(String noteNumber) {
        executor.execute(() -> {
            appDatabase.noteDao().updateNoteContent(noteNumber, noteContentEditText.getText().toString());
            mainHandler.post(() -> Toast.makeText(requireContext(), "Note saved successfully", Toast.LENGTH_SHORT).show());
        });
    }

    private void saveNoteToFirestore(String noteId) {
        String currentUserUid = getCurrentUser();

        executor.execute(() -> {
            db.collection("users")
                    .document(currentUserUid)
                    .collection("notes")
                    .document(noteId)
                    .update("content", noteContentEditText.getText().toString())
                    .addOnSuccessListener(aVoid -> {
                        mainHandler.post(() -> Toast.makeText(requireContext(), "Note saved successfully", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> {
                        mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to save the note: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });
        });
    }

    private void navigateToNotesRepoFragment() {
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