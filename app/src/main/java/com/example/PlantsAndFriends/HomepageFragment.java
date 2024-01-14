package com.example.PlantsAndFriends;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomepageFragment extends Fragment implements PlantsGridAdapter.OnPlantClickListener {
    private RecyclerView recyclerView;
    private PlantsGridAdapter adapter;
    private Toolbar toolbar;
    private static final String TAG = "HomepageFragment";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DataRepository dataRepository;
    private ListenerRegistration plantsListener;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase appDatabase;
    private LiveData<List<PlantEntity>> localPlants;
    public static final String READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE";
    StorageReference storageReference;
    private ImageView searchIcon;
    // Layout Changes
    private SwitchMaterial switchButton;
    private boolean isGridLayout = true;

    private TextView currentTempTextView;
    private TextView currentHumTextView;

    private Button addPlantButton;
    private MqttViewModel mqttViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        searchIcon = view.findViewById(R.id.searchIcon);

        currentTempTextView = view.findViewById(R.id.temperatureTextView);
        currentHumTextView = view.findViewById(R.id.humidityTextView);

        addPlantButton = view.findViewById(R.id.add_plant);

        addPlantButton.setOnClickListener(v -> {
            openEditPlant(String.valueOf(System.currentTimeMillis()), true);
        });

        adapter = new PlantsGridAdapter(requireContext(), new ArrayList<>(), appDatabase);
        recyclerView.setAdapter(adapter);

        setHasOptionsMenu(true);

        //Switch Layout
        switchButton = view.findViewById(R.id.switchButton);

        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (adapter != null) {
                adapter.switchLayoutMode();
                isGridLayout = !isChecked; // Toggle the layout mode
                setLayoutManager(); // Set the appropriate layout manager
            }
        });
        setLayoutManager();

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.homepage_menu);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        //search
        searchIcon.setOnClickListener(v -> showSearchDialog());

        // MQTT ViewModel
        mqttViewModel = new ViewModelProvider(this).get(MqttViewModel.class);
        mqttViewModel.setDataRepository(DataRepository.getInstance());

        mqttViewModel.getFormattedTemperature().observe(getViewLifecycleOwner(), temperature -> {
            Log.d(TAG, "Formatted Temperature: " + temperature);
            currentTempTextView.setText(temperature);
        });

        mqttViewModel.getFormattedHumidity().observe(getViewLifecycleOwner(), humidity -> {
            Log.d(TAG, "Formatted Humidity: " + humidity);
            currentHumTextView.setText(humidity);
        });


        // load the plants from local storage at startup
        loadPlantsFromLocalStorage();

//        if (!isNetworkConnected()) {
//            mainHandler.post(() -> {
//                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
//            });
//        }

        if (!isGalleryPermissionGranted()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{READ_EXTERNAL_STORAGE}, 101);
            } else {
                ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{READ_MEDIA_IMAGES}, 101);
            }
        } else {
            Log.d(TAG, "onCreateView: " + "Permission granted");
        }

//        if (!isWriteExternalStoragePermissionGranted())
//            requestWriteExternalStoragePermission();

        startMqttMonitorService();
        return view;
    }

    private BroadcastReceiver mqttUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("mqtt_update")) {
                float temperature = intent.getFloatExtra("temperature", Float.NaN);
                float humidity = intent.getFloatExtra("humidity", Float.NaN);

                currentTempTextView.setText(String.format("%.2f°C", temperature));
                currentHumTextView.setText(String.format("%.2f%%", humidity));
            }
        }
    };

    @Override
    public void onResume() {
        // Register the receiver
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S && isNetworkConnected()) {
            LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(mqttUpdateReceiver, new IntentFilter("mqtt_update"));
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        // Unregister the receiver to avoid memory leaks
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S && isNetworkConnected()) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mqttUpdateReceiver);
        }
        super.onPause();
    }

    private void setLayoutManager() {
        if (isGridLayout) {
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    public class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int horizontalSpace;

        public HorizontalSpaceItemDecoration(int horizontalSpace) {
            this.horizontalSpace = horizontalSpace;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.right = horizontalSpace;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(requireContext());
        localPlants = appDatabase.plantDao().getAllPlants();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (plantsListener != null) {
            plantsListener.remove(); // prevent memory leaks
        }
    }

    @Override
    public void onPlantClick(Plant plant) {
        openEditPlant(plant.getNumber(), false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_plant) {
            openEditPlant(String.valueOf(System.currentTimeMillis()), true);
            return true;
        }

        if (id == R.id.action_search_plant) {
            showSearchDialog();
            return true;
        }

        if (id == R.id.action_alerts) {
            loadAlertsFragment();
            return true;
        }

        if (id == R.id.load_plants_from_firestore) {
            showDownloadDialog();
            return true;
        }

        if (id == R.id.save_plants_to_firestore) {
            showUploadDialog();
        }

        if (id == R.id.action_logout) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(requireContext(), firebaseUser.getEmail() + "Logged out successfully", Toast.LENGTH_SHORT).show();
                loadLoginFragment();
                return true;
            }

            // Logout the user
            GoogleSignInOptions gso = new GoogleSignInOptions.
                    Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                    build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                        loadLoginFragment();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startMqttMonitorService() {
        // if api >= 31 return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S || !isNetworkConnected()) {
            return;
        }
        Intent serviceIntent = new Intent(getActivity(), MqttMonitorService.class);
        requireActivity().startService(serviceIntent);
    }

    private boolean isGalleryPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return requireContext().checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return requireContext().checkSelfPermission(READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private boolean isWriteExternalStoragePermissionGranted() {
        return requireContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isManageExternalStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    private void requestWriteExternalStoragePermission() {
        if (!isWriteExternalStoragePermissionGranted()) {
            ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{WRITE_EXTERNAL_STORAGE}, 102);
        } else {
            requestManageExternalStoragePermission();
        }
    }

    private void requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2 && !isManageExternalStoragePermissionGranted()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, 103);
        } else {
            Log.d(TAG, "requestManageExternalStoragePermission: " + "Permission granted");
        }
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

    private void updateFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }
        String currentUserUid = currentUser.getUid();

        localPlants = appDatabase.plantDao().getAllPlants();
        localPlants.observe(getViewLifecycleOwner(), plantEntities -> {
            List<Plant> plants = convertToPlantList(plantEntities);

            for (Plant localPlant : plants) {
                executor.execute(() -> {
                    db.collection("users").document(currentUserUid).collection("plants")
                            .document(localPlant.getNumber()).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (task.getResult().exists()) {
                                        Log.d(TAG, "Plant exists in Firestore, only updates will be done");
                                        Map<String, Object> plant = new HashMap<>();
                                        plant.put("number", localPlant.getNumber());
                                        plant.put("name", localPlant.getName());
                                        plant.put("species", localPlant.getSpecies());
                                        plant.put("min_temp", localPlant.getMin_temp());
                                        plant.put("max_temp", localPlant.getMax_temp());
                                        plant.put("min_humidity", localPlant.getMin_humidity());
                                        plant.put("max_humidity", localPlant.getMax_humidity());
                                        plant.put("description", localPlant.getDescription());
                                        plant.put("imgUri", localPlant.getImgUri());

//                                        uploadImage(Uri.parse(localPlant.getImgUri()), localPlant.getNumber());

                                        // if plant exists in firestore but not locally to delete it else update it
                                        Log.d(TAG, "Plant does not exist locally, it will be deleted from Firestore");
                                        executor.execute(() -> {
                                            db.collection("users").document(currentUserUid).collection("plants")
                                                    .document(localPlant.getNumber())
                                                    .update(plant)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Plant updated in Firestore");
                                                        // TODO - create a single toast message for all the plants
                                                        mainHandler.post(() -> Toast.makeText(requireContext(), "Plant updated in Firestore", Toast.LENGTH_SHORT).show());
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.w(TAG, "Error updating plant in Firestore", e);
                                                        mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to update plant in Firestore", Toast.LENGTH_SHORT).show());
                                                    });
                                        });


                                    } else {
                                        Log.d(TAG, "Plant does not exist in Firestore, it will be added");
                                        Map<String, Object> plant = new HashMap<>();
                                        plant.put("number", localPlant.getNumber());
                                        plant.put("name", localPlant.getName());
                                        plant.put("species", localPlant.getSpecies());
                                        plant.put("min_temp", localPlant.getMin_temp());
                                        plant.put("max_temp", localPlant.getMax_temp());
                                        plant.put("min_humidity", localPlant.getMin_humidity());
                                        plant.put("max_humidity", localPlant.getMax_humidity());
                                        plant.put("description", localPlant.getDescription());
                                        plant.put("imgUri", localPlant.getImgUri());
                                        executor.execute(() -> {
                                            db.collection("users").document(currentUserUid).collection("plants")
                                                    .document(localPlant.getNumber())
                                                    .set(plant)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Plant added to Firestore");
                                                        mainHandler.post(() -> Toast.makeText(requireContext(), "Plant added to Firestore", Toast.LENGTH_SHORT).show());
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.w(TAG, "Error adding plant to Firestore", e);
                                                        mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to add plant to Firestore", Toast.LENGTH_SHORT).show());
                                                    });
                                        });
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents", task.getException());
                                }
                            });
                });
            }

            // delete the plants from firestore that are not in local storage
            executor.execute(() -> {
                db.collection("users").document(currentUserUid).collection("plants").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String plantNumber = document.getString("number");
                            AtomicBoolean plantExists = new AtomicBoolean(false);
                            for (Plant localPlant : plants) {
                                if (localPlant.getNumber().equals(plantNumber)) {
                                    plantExists.set(true);
                                    break;
                                }
                            }
                            if (!plantExists.get()) {
                                deletePlantFromFirestore(convertToPlant(document.toObject(PlantEntity.class)));
                            }
                        }
                    } else {
                        Log.w(TAG, "Error getting documents", task.getException());
                    }
                });
            });

        });
    }

    private void createNewPlantInLocalStorage(String plantNumber, String plantName, String plantSpecies, float minTemp, float maxTemp, float minHumidity, float maxHumidity, String plantDescription, String plantImgUri) {
        executor.execute(() -> {
            if (appDatabase.plantDao().getPlantByNumber(plantNumber) != null) {
                Log.d(TAG, "createNewPlantInLocalStorage: " + "Plant already exists");
                return;
            }

            try {
                PlantEntity plantEntity = new PlantEntity();
                plantEntity.setNumber(plantNumber);
                plantEntity.setName(plantName);
                plantEntity.setSpecies(plantSpecies);
                plantEntity.setMin_temp(minTemp);
                plantEntity.setMax_temp(maxTemp);
                plantEntity.setMin_humidity(minHumidity);
                plantEntity.setMax_humidity(maxHumidity);
                plantEntity.setDescription(plantDescription);
                plantEntity.setImgUri(plantImgUri);
                appDatabase.plantDao().insert(plantEntity);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // TODO - create a method to load the plants images from firebase bucket to local storage
    private void loadFromFirestore() {
        // load the plants from firebase to local storage
        // clear the local storage and add the plants from firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }
        String currentUserUid = currentUser.getUid();
        executor.execute(() -> {
            // check if the local database is empty or not
            appDatabase.plantDao().deleteAllPlants();
        });


        executor.execute(() -> {
            db.collection("users").document(currentUserUid).collection("plants").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Plant plant = convertToPlant(document.toObject(PlantEntity.class));
                        executor.execute(() -> {
                            String imgUri = plant.getImgUri();
                            createNewPlantInLocalStorage(plant.getNumber(), plant.getName(), plant.getSpecies(), (float) plant.getMin_temp(), (float) plant.getMax_temp(),
                                    (float) plant.getMin_humidity(), (float) plant.getMax_humidity(), plant.getDescription(), imgUri == null ? null : imgUri.isEmpty() ? null : isValidUri(imgUri) ? imgUri : null);
                        });
                    }
                } else {
                    Log.w(TAG, "Error getting documents", task.getException());
                }
            });
        });


    }

    private boolean isValidUri(String uriString) {
        try {
            Uri uri = Uri.parse(uriString);

            if ("content".equals(uri.getScheme())) {
                ContentResolver contentResolver = requireContext().getContentResolver();

                try {
                    ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
                    if (parcelFileDescriptor != null) {
                        parcelFileDescriptor.close();
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error opening file descriptor for URI: " + uri, e);
                }
            } else {
                Log.e(TAG, "Invalid URI scheme: " + uri.getScheme());
            }
        } catch (Exception e) {
//            e.printStackTrace();
            Log.i(TAG, "Error parsing URI: " + uriString);
        }
        return false;
    }

    private void deletePlantFromFirestore(Plant plant) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();
        executor.execute(() -> {
            db.collection("users").document(currentUserUid).collection("plants")
                    .document(plant.getNumber())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("deletePlantFromFirestore", "Plant deleted from Firestore");
                        mainHandler.post(() -> Toast.makeText(requireContext(), "Plant deleted from Firestore", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> {
                        mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to delete plant from Firestore", Toast.LENGTH_SHORT).show());
                    });
        });
    }


    private void showUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Save plants to Firestore?");
        builder.setMessage("Careful! This will overwrite the plants on Firestore with the ones on local storage.");

        builder.setPositiveButton("Confirm", null);
        builder.setNeutralButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (isNetworkConnected()) {
                updateFirestore();
                dialog.cancel();
            } else {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                });
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> dialog.cancel());
    }

    private void showDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Load plants from Firestore?");
        builder.setMessage("Careful! This will overwrite the plants locally with the ones on Firestore.");

        builder.setPositiveButton("Confirm", null);
        builder.setNeutralButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            // set color to grey of buttons
            if (isNetworkConnected()) {
                loadFromFirestore();
                dialog.cancel();
            } else {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                });
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> dialog.cancel());
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Search plant by name or species");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Search", null);
        builder.setNegativeButton("Clear Search", null);
        builder.setNeutralButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String searchText = input.getText().toString();
            performSearch(searchText);
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
            input.setText("");
            performSearch("");
        });
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> dialog.cancel());
    }

    private void performSearch(String searchText) {
        localPlants = appDatabase.plantDao().getAllPlants();
        localPlants.observe(getViewLifecycleOwner(), plantEntities -> {
            List<Plant> plants = convertToPlantList(plantEntities);
            List<Plant> filteredPlants = new ArrayList<>();

            for (Plant plant : plants) {
                String plantName = plant.getName();
                String plantSpecies = plant.getSpecies();
                String lowercaseSearchText = searchText.toLowerCase();
                assert plantName != null;
                String lowercasePlantName = plantName.toLowerCase();
                assert plantSpecies != null;
                String lowercasePlantSpecies = plantSpecies.toLowerCase();

                if (lowercasePlantName.startsWith(lowercaseSearchText)) {
                    filteredPlants.add(plant);
                } else if (lowercasePlantSpecies.startsWith(lowercaseSearchText)) {
                    filteredPlants.add(plant);
                }

            }

            adapter.updatePlants(filteredPlants);
        });
//        }
    }

    private void openEditPlant(String plantNumber, boolean isCreate) {
        PlantDetailsFragment EditPlantFragment = new PlantDetailsFragment();
        Bundle args = new Bundle();
        args.putString("plantNumber", plantNumber);
        args.putBoolean("isCreate", isCreate);
        EditPlantFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EditPlantFragment, "PlantDetailsFragment")
                .addToBackStack(TAG)
                .commit();
    }

    private void loadAlertsFragment() {
        AlertsFragment alertsFragment = new AlertsFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, alertsFragment)
                .addToBackStack(TAG)
                .commit();
    }

    private void loadLoginFragment() {
        Log.d(TAG, "loadLoginFragment: " + requireActivity().getSupportFragmentManager().getBackStackEntryCount());

        // Load the login fragment
        LoginFragment loginFragment = new LoginFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, loginFragment)
                .commit();
    }

    private void loadPlantsFromLocalStorage() {
        localPlants = appDatabase.plantDao().getAllPlants();

        localPlants.observe(getViewLifecycleOwner(), plantEntities -> {
            Log.d(TAG, "loadPlantsFromLocalStorage: " + plantEntities);

            List<Plant> plants = convertToPlantList(plantEntities);
            // TODO - create the plant only if saved on PlantDetailsFragment to avoid rerendering the list
            adapter.updatePlants(plants);
        });


        List<Plant> plantsAux = convertToPlantList(localPlants.getValue());
        adapter = new PlantsGridAdapter(requireContext(), plantsAux, appDatabase);
        adapter.setOnPlantClickListener(HomepageFragment.this);
        recyclerView.setAdapter(adapter);
    }

    private List<Plant> convertToPlantList(@Nullable List<PlantEntity> plantEntities) {
        if (plantEntities == null) {
            return new ArrayList<>();
        }

        List<Plant> plants = new ArrayList<>();

        for (PlantEntity plantEntity : plantEntities) {
            String plantId = String.valueOf(plantEntity.getId());

            Plant plant = new Plant(
                    plantId,
                    plantEntity.getNumber(),
                    plantEntity.getName(),
                    plantEntity.getSpecies(),
                    plantEntity.getMin_temp(),
                    plantEntity.getMax_temp(),
                    plantEntity.getMin_humidity(),
                    plantEntity.getMax_humidity(),
                    plantEntity.getDescription(),
                    plantEntity.getImgUri()
            );
            plants.add(plant);
        }
        return plants;
    }

    private Plant convertToPlant(PlantEntity plantEntity) {
        String PlantId = String.valueOf(plantEntity.getId());

        return new Plant(
                PlantId,
                plantEntity.getNumber(),
                plantEntity.getName(),
                plantEntity.getSpecies(),
                plantEntity.getMin_temp(),
                plantEntity.getMax_temp(),
                plantEntity.getMin_humidity(),
                plantEntity.getMax_humidity(),
                plantEntity.getDescription(),
                plantEntity.getImgUri()
        );
    }
}
