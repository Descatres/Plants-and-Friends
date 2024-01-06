package com.example.PlantsAndFriends;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HomepageFragment extends Fragment implements PlantsGridAdapter.OnPlantClickListener {
    private List<Plant> plantsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PlantsGridAdapter adapter;
    private Toolbar toolbar;
    private static final String TAG = "HomepageFragment";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration plantsListener;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase appDatabase;
    private LiveData<List<PlantEntity>> localPlants;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new PlantsGridAdapter(requireContext(), new ArrayList<>(), appDatabase);
        recyclerView.setAdapter(adapter);

        setHasOptionsMenu(true);

        // Use GridLayoutManager with 3 columns
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.horizontal_spacing);
        recyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(spacingInPixels));

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.plants_repo_menu);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        if (isNetworkConnected()) {
            // load the plants from firestore at startup
            loadPlantsFromFirebase();
            // listen for changes in the plants collection in firestore to update the plants (for example, if the title of a plant is changed)
            loadPlantsAfterUpdatesFirebase();
            //saveToLocalStorage();
        } else {
            mainHandler.post(() -> {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            });
            loadPlantsFromLocalStorage();
        }

        return view;
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
    public void onPlantClick(Plant plant) {
        openEditPlant(plant.getNumber(), plant.getId());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_plant) {
            showCreatePlantDialog();
            return true;
        }

        if (id == R.id.action_search_plant) {
            showSearchDialog();
            return true;
        }

        if (id == R.id.refresh) {
            if (isNetworkConnected())
                loadPlantsFromFirebase();
            else
                loadPlantsFromLocalStorage();
            return true;
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

    private void loadPlantsFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        executor.execute(() -> {
            // delete plant from firebase if it does not exist on the local storage
            for (Plant plant : plantsList) {
                Log.e(TAG, "Delete plant number: " + plant.getNumber());
                if (appDatabase.plantDao().getPlantByNumber(plant.getNumber()) == null) {
                    Log.d(TAG, "Deleting plant from Firebase: " + plant.getNumber());
                    deletePlantFromFirestore(plant);
                }
            }

            Log.d(TAG, "loadPlantsFromFirebase: " + currentUserUid);
            db.collection("users").document(currentUserUid).collection("plants")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            adapter = new PlantsGridAdapter(requireContext(), plantsList, appDatabase);
                            adapter.setOnPlantClickListener(HomepageFragment.this);
                            recyclerView.setAdapter(adapter);

                            plantsList.clear(); // Clear the list before adding updated plants

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String plantId = document.getId();
                                String plantTitle = document.getString("title");
                                String plantContent = document.getString("content");
                                String number = document.getString("number");

                                Plant plant = new Plant(plantId, number, plantTitle, plantContent != null ? plantContent : "");
                                plantsList.add(plant); // Add plant to the list
                            }

                            updateFirebase();
                            saveToLocalStorage();

                        } else {
                            mainHandler.post(() -> {
                                Toast.makeText(requireContext(), "Failed to retrieve plants from Firebase", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });
    }

    private void updateFirebase() {
        // Iterate through the local plants and check if there are any plants that are not in the Firebase database
        // If there are, upload them into the Firebase database
        localPlants = appDatabase.plantDao().getAllPlants();
        localPlants.observe(getViewLifecycleOwner(), plantEntities -> {
            plantEntities.forEach(
                    plantEntity -> {
                        boolean exists = false;
                        for (Plant plant : plantsList) {
                            if (plant.getNumber().equals(plantEntity.getNumber())) {
                                exists = true;
                                // update plant title and content
                                Log.d(TAG, "Plant entity: " + plantEntity.getTitle() + " " + plantEntity.getContent());
                                Log.d(TAG, "Plant: " + plant.getTitle() + " " + plant.getContent());
                                if (!plant.getTitle().equals(plantEntity.getTitle()) || !plant.getContent().equals(plantEntity.getContent() == null ? "" : plantEntity.getContent())) {
                                    Log.d(TAG, "Updating plant in Firebase: " + plantEntity.getNumber());
                                    updatePlantTitleInFirebase(plant, plantEntity.getTitle());
                                }

                                if (!plant.getContent().equals(plantEntity.getContent()) || !plant.getContent().equals(plantEntity.getContent() == null ? "" : plantEntity.getContent())) {
                                    Log.d(TAG, "Updating plant content in Firebase: " + plantEntity.getNumber());
                                    updatePlantContentInFirebase(plant, plantEntity.getContent());
                                }
                                break;
                            }
                        }

                        if (!exists) {
                            Log.d(TAG, "New plant in Firebase: " + plantEntity.getNumber());
                            createNewPlantInDatabase(plantEntity.getNumber(), plantEntity.getTitle(), plantEntity.getContent());
                            plantsList.add(convertToPlant(plantEntity));
                        }
                    }
            );
        });


    }

    private void updatePlantContentInFirebase(Plant plant, String newContent) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        // Get a reference to the plant document in Firestore and update the title
        Log.d(TAG, "updatePlantContentInFirebase: " + plant.getNumber() + " " + newContent);
        db.collection("users").document(currentUserUid).collection("plants")
                .document(plant.getId())
                .update("content", newContent)
                .addOnSuccessListener(aVoid -> {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Plant content updated in Firestore", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updatePlantContentInFirebase: " + e.getMessage());
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to update plant content in Firestore", Toast.LENGTH_SHORT).show());
                });
    }

    private void deletePlantFromFirestore(Plant plant) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();
        db.collection("users").document(currentUserUid).collection("plants")
                .document(plant.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("deletePlantFromFirestore", "Plant deleted from Firestore");
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Plant deleted from Firebase", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to delete plant from Firebase", Toast.LENGTH_SHORT).show());
                });
    }

    private void updatePlantTitleInFirebase(Plant plant, String newTitle) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        // Get a reference to the plant document in Firestore and update the title
        Log.d(TAG, "updatePlantTitleInFirebase: " + plant.getNumber() + " " + newTitle);
        db.collection("users").document(currentUserUid).collection("plants")
                .document(plant.getId())
                .update("title", newTitle)
                .addOnSuccessListener(aVoid -> {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Plant title updated in Firestore", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updatePlantTitleInFirebase: " + e.getMessage());
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to update plant title in Firestore", Toast.LENGTH_SHORT).show());
                });

    }

    private void saveToLocalStorage() {
        plantsList.forEach(plant -> {
            String plantId = plant.getId();
            String plantNumber = plant.getNumber();
            String plantTitle = plant.getTitle();
            String plantContent = plant.getContent();

            createNewPlantInLocalStorage(plantNumber, plantTitle, plantContent);
        });
    }

    private void loadPlantsAfterUpdatesFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        plantsListener = db.collection("users").document(currentUserUid).collection("plants")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        plantsList.clear(); // Clear the list before adding updated plants
                        for (QueryDocumentSnapshot document : value) {
                            String plantId = document.getId();
                            String plantTitle = document.getString("title");
                            String number = document.getString("number");
                            String plantContent = document.getString("content");

                            Plant plant = new Plant(plantId, number, plantTitle, plantContent);
                            plantsList.add(plant); // Add plant to the list
                        }

                        adapter = new PlantsGridAdapter(requireContext(), plantsList, appDatabase);
                        adapter.setOnPlantClickListener(HomepageFragment.this);
                        recyclerView.setAdapter(adapter);
                    }
                });

    }

    private void createNewPlantInDatabase(String plantId, String plantTitle, String plantContent) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        Map<String, Object> plant = new HashMap<>();
        plant.put("number", plantId);
        plant.put("title", plantTitle);
        plant.put("content", plantContent != null ? plantContent : "");


        db.collection("users").document(currentUserUid).collection("plants")
                .add(plant)
                .addOnSuccessListener(documentReference -> {
                    mainHandler.post(() -> {
                        Log.d(TAG, "Plant created and saved to Firebase");
                        Toast.makeText(requireContext(), "Plant created and saved to Firebase", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Failed to save plant to Firebase", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void showCreatePlantDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Please enter a title for your plant");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String plantNumber = String.valueOf(System.currentTimeMillis());
            String plantTitle = input.getText().toString();

            createNewPlantInDatabase(plantNumber, plantTitle, ""); // Create and store the plant in Firebase
            createNewPlantInLocalStorage(plantNumber, plantTitle, ""); // Create and store the plant locally
            dialog.dismiss();
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> dialog.cancel());
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Search Plant by Title");

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
            if (isNetworkConnected()) {
                loadPlantsFromFirebase();
            } else {
                loadPlantsFromLocalStorage();
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> dialog.cancel());
    }

    private void performSearch(String searchText) {
        if (isNetworkConnected()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            String currentUserUid = null;
            if (currentUser == null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.
                        Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                        build();

                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
                currentUserUid = googleSignInClient.getSignInIntent().getIdentifier();
                Log.d(TAG, "Google User Id: " + currentUserUid);

                if (currentUserUid == null) {
                    loadLoginFragment();
                    return;
                }
            } else {
                currentUserUid = currentUser.getUid();
            }

            db.collection("users").document(currentUserUid).collection("plants").orderBy("title")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            plantsList.clear(); // Clear the list before adding updated plants

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String plantId = document.getId();
                                String plantTitle = document.getString("title");
                                String number = document.getString("number");

                                // case insensitive search
                                String lowercaseSearchText = searchText.toLowerCase();
                                assert plantTitle != null;
                                String lowercasePlantTitle = plantTitle.toLowerCase();

                                if (lowercasePlantTitle.startsWith(lowercaseSearchText)) {
                                    Plant plant = new Plant(plantId, number, plantTitle, "");
                                    plantsList.add(plant);
                                }
                            }

                            adapter = new PlantsGridAdapter(requireContext(), plantsList, appDatabase);
                            adapter.setOnPlantClickListener(HomepageFragment.this);
                            recyclerView.setAdapter(adapter);

                        } else {
                            mainHandler.post(() -> {
                                Toast.makeText(requireContext(), "Failed to retrieve plants from Firebase", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } else {
            localPlants = appDatabase.plantDao().getAllPlants();
            localPlants.observe(getViewLifecycleOwner(), plantEntities -> {
                List<Plant> plants = convertToPlantList(plantEntities);
                List<Plant> filteredPlants = new ArrayList<>();

                for (Plant plant : plants) {
                    String plantTitle = plant.getTitle();
                    String lowercaseSearchText = searchText.toLowerCase();
                    assert plantTitle != null;
                    String lowercasePlantTitle = plantTitle.toLowerCase();

                    if (lowercasePlantTitle.startsWith(lowercaseSearchText)) {
                        filteredPlants.add(plant);
                    }
                }

                adapter.updatePlants(filteredPlants);
            });
        }
    }

    private void openEditPlant(String plantNumber, String plantId) {
        PlantDetailsFragment EditPlantFragment = new PlantDetailsFragment();
        Bundle args = new Bundle();
        args.putString("plantNumber", plantNumber);
        args.putString("plantId", plantId);
        EditPlantFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EditPlantFragment, "PlantDetailsFragment")
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
            List<Plant> plants = convertToPlantList(plantEntities);
            adapter.updatePlants(plants);
        });

        List<Plant> plantsAux = convertToPlantList(localPlants.getValue());

        adapter = new PlantsGridAdapter(requireContext(), plantsAux, appDatabase);
        adapter.setOnPlantClickListener(HomepageFragment.this);
        recyclerView.setAdapter(adapter);
    }

    private void createNewPlantInLocalStorage(String plantNumber, String plantTitle, String plantContent) {
        executor.execute(() -> {
            if (appDatabase.plantDao().getPlantByNumber(plantNumber) != null) {
                Log.d(TAG, "createNewPlantInLocalStorage: " + "Plant already exists");
                return;
            }

            try {
                PlantEntity plantEntity = new PlantEntity();
                plantEntity.setNumber(plantNumber);
                plantEntity.setTitle(plantTitle);
                plantEntity.setContent(plantContent != null ? plantContent : "");
                appDatabase.plantDao().insert(plantEntity);

                if (!isNetworkConnected()) {
                    loadPlantsFromLocalStorage();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
                    plantEntity.getTitle(),
                    plantEntity.getContent()
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
                plantEntity.getTitle(),
                plantEntity.getContent()
        );
    }
}
