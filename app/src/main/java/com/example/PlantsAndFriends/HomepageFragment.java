package com.example.PlantsAndFriends;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
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

public class HomepageFragment extends Fragment implements NotesGridAdapter.OnNoteClickListener {
    private List<Plant> plantsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NotesGridAdapter adapter;
    private Toolbar toolbar;
    private static final String TAG = "HomepageFragment";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration notesListener;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase appDatabase;
    private LiveData<List<NoteEntity>> localNotes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new NotesGridAdapter(requireContext(), new ArrayList<>(), appDatabase);
        recyclerView.setAdapter(adapter);

        setHasOptionsMenu(true);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.notes_repo_menu);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        if (isNetworkConnected()) {
            // load the notes from firestore at startup
            loadNotesFromFirebase();
            // listen for changes in the notes collection in firestore to update the notes (for example, if the title of a note is changed)
            loadNotesAfterUpdatesFirebase();
            //saveToLocalStorage();
        } else {
            mainHandler.post(() -> {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            });
            loadNotesFromLocalStorage();
        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(requireContext());
        localNotes = appDatabase.noteDao().getAllNotes();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notesListener != null) {
            notesListener.remove(); // prevent memory leaks
        }
    }

    @Override
    public void onNoteClick(Plant plant) {
        openEditPlant(plant.getNumber(), plant.getId());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_note) {
            showCreateNoteDialog();
            return true;
        }

        if (id == R.id.action_search_note) {
            showSearchDialog();
            return true;
        }

        if (id == R.id.refresh) {
            if (isNetworkConnected())
                loadNotesFromFirebase();
            else
                loadNotesFromLocalStorage();
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

    private void loadNotesFromFirebase() {
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
                if (appDatabase.noteDao().getNoteByNumber(plant.getNumber()) == null) {
                    Log.d(TAG, "Deleting plant from Firebase: " + plant.getNumber());
                    deleteNoteFromFirestore(plant);
                }
            }

            Log.d(TAG, "loadNotesFromFirebase: " + currentUserUid);
            db.collection("users").document(currentUserUid).collection("notes")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            adapter = new NotesGridAdapter(requireContext(), plantsList, appDatabase);
                            adapter.setOnNoteClickListener(HomepageFragment.this);
                            recyclerView.setAdapter(adapter);

                            plantsList.clear(); // Clear the list before adding updated notes

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String noteId = document.getId();
                                String noteTitle = document.getString("title");
                                String noteContent = document.getString("content");
                                String number = document.getString("number");

                                Plant plant = new Plant(noteId, number, noteTitle, noteContent != null ? noteContent : "");
                                plantsList.add(plant); // Add note to the list
                            }

                            updateFirebase();
                            saveToLocalStorage();

                        } else {
                            mainHandler.post(() -> {
                                Toast.makeText(requireContext(), "Failed to retrieve notes from Firebase", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });
    }

    private void updateFirebase() {
        // Iterate through the local notes and check if there are any notes that are not in the Firebase database
        // If there are, upload them into the Firebase database
        localNotes = appDatabase.noteDao().getAllNotes();
        localNotes.observe(getViewLifecycleOwner(), noteEntities -> {
            noteEntities.forEach(
                    noteEntity -> {
                        boolean exists = false;
                        for (Plant plant : plantsList) {
                            if (plant.getNumber().equals(noteEntity.getNumber())) {
                                exists = true;
                                // update note title and content
                                Log.d(TAG, "Note entity: " + noteEntity.getTitle() + " " + noteEntity.getContent());
                                Log.d(TAG, "Note: " + plant.getTitle() + " " + plant.getContent());
                                if (!plant.getTitle().equals(noteEntity.getTitle()) || !plant.getContent().equals(noteEntity.getContent() == null ? "" : noteEntity.getContent())) {
                                    Log.d(TAG, "Updating note in Firebase: " + noteEntity.getNumber());
                                    updateNoteTitleInFirebase(plant, noteEntity.getTitle());
                                }

                                if (!plant.getContent().equals(noteEntity.getContent()) || !plant.getContent().equals(noteEntity.getContent() == null ? "" : noteEntity.getContent())) {
                                    Log.d(TAG, "Updating note content in Firebase: " + noteEntity.getNumber());
                                    updateNoteContentInFirebase(plant, noteEntity.getContent());
                                }
                                break;
                            }
                        }

                        if (!exists) {
                            Log.d(TAG, "New note in Firebase: " + noteEntity.getNumber());
                            createNewNoteInDatabase(noteEntity.getNumber(), noteEntity.getTitle(), noteEntity.getContent());
                            plantsList.add(convertToNote(noteEntity));
                        }
                    }
            );
        });


    }

    private void updateNoteContentInFirebase(Plant plant, String newContent) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        // Get a reference to the note document in Firestore and update the title
        Log.d(TAG, "updateNoteContentInFirebase: " + plant.getNumber() + " " + newContent);
        db.collection("users").document(currentUserUid).collection("notes")
                .document(plant.getId())
                .update("content", newContent)
                .addOnSuccessListener(aVoid -> {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Note content updated in Firestore", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updateNoteContentInFirebase: " + e.getMessage());
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to update note content in Firestore", Toast.LENGTH_SHORT).show());
                });
    }

    private void deleteNoteFromFirestore(Plant plant) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();
        db.collection("users").document(currentUserUid).collection("notes")
                .document(plant.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("deleteNoteFromFirestore", "Note deleted from Firestore");
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Note deleted from Firebase", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to delete note from Firebase", Toast.LENGTH_SHORT).show());
                });
    }

    private void updateNoteTitleInFirebase(Plant plant, String newTitle) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        // Get a reference to the note document in Firestore and update the title
        Log.d(TAG, "updateNoteTitleInFirebase: " + plant.getNumber() + " " + newTitle);
        db.collection("users").document(currentUserUid).collection("notes")
                .document(plant.getId())
                .update("title", newTitle)
                .addOnSuccessListener(aVoid -> {
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Note title updated in Firestore", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updateNoteTitleInFirebase: " + e.getMessage());
                    mainHandler.post(() -> Toast.makeText(requireContext(), "Failed to update note title in Firestore", Toast.LENGTH_SHORT).show());
                });

    }

    private void saveToLocalStorage() {
        plantsList.forEach(plant -> {
            String noteId = plant.getId();
            String noteNumber = plant.getNumber();
            String noteTitle = plant.getTitle();
            String noteContent = plant.getContent();

            createNewNoteInLocalStorage(noteNumber, noteTitle, noteContent);
        });
    }

    private void loadNotesAfterUpdatesFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        notesListener = db.collection("users").document(currentUserUid).collection("notes")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        plantsList.clear(); // Clear the list before adding updated notes
                        for (QueryDocumentSnapshot document : value) {
                            String noteId = document.getId();
                            String noteTitle = document.getString("title");
                            String number = document.getString("number");
                            String noteContent = document.getString("content");

                            Plant plant = new Plant(noteId, number, noteTitle, noteContent);
                            plantsList.add(plant); // Add note to the list
                        }

                        adapter = new NotesGridAdapter(requireContext(), plantsList, appDatabase);
                        adapter.setOnNoteClickListener(HomepageFragment.this);
                        recyclerView.setAdapter(adapter);
                    }
                });

    }

    private void createNewNoteInDatabase(String noteId, String noteTitle, String noteContent) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadLoginFragment();
            return;
        }

        String currentUserUid = currentUser.getUid();

        Map<String, Object> note = new HashMap<>();
        note.put("number", noteId);
        note.put("title", noteTitle);
        note.put("content", noteContent != null ? noteContent : "");


        db.collection("users").document(currentUserUid).collection("notes")
                .add(note)
                .addOnSuccessListener(documentReference -> {
                    mainHandler.post(() -> {
                        Log.d(TAG, "Note created and saved to Firebase");
                        Toast.makeText(requireContext(), "Note created and saved to Firebase", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Failed to save note to Firebase", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void showCreateNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Please enter a title for your note");

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
            String noteNumber = String.valueOf(System.currentTimeMillis());
            String noteTitle = input.getText().toString();

            createNewNoteInDatabase(noteNumber, noteTitle, ""); // Create and store the note in Firebase
            createNewNoteInLocalStorage(noteNumber, noteTitle, ""); // Create and store the note locally
            dialog.dismiss();
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> dialog.cancel());
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Search Note by Title");

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
                loadNotesFromFirebase();
            } else {
                loadNotesFromLocalStorage();
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

            db.collection("users").document(currentUserUid).collection("notes").orderBy("title")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            plantsList.clear(); // Clear the list before adding updated notes

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String noteId = document.getId();
                                String noteTitle = document.getString("title");
                                String number = document.getString("number");

                                // case insensitive search
                                String lowercaseSearchText = searchText.toLowerCase();
                                assert noteTitle != null;
                                String lowercaseNoteTitle = noteTitle.toLowerCase();

                                if (lowercaseNoteTitle.startsWith(lowercaseSearchText)) {
                                    Plant plant = new Plant(noteId, number, noteTitle, "");
                                    plantsList.add(plant);
                                }
                            }

                            adapter = new NotesGridAdapter(requireContext(), plantsList, appDatabase);
                            adapter.setOnNoteClickListener(HomepageFragment.this);
                            recyclerView.setAdapter(adapter);

                        } else {
                            mainHandler.post(() -> {
                                Toast.makeText(requireContext(), "Failed to retrieve notes from Firebase", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } else {
            localNotes = appDatabase.noteDao().getAllNotes();
            localNotes.observe(getViewLifecycleOwner(), noteEntities -> {
                List<Plant> plants = convertToNoteList(noteEntities);
                List<Plant> filteredPlants = new ArrayList<>();

                for (Plant plant : plants) {
                    String noteTitle = plant.getTitle();
                    String lowercaseSearchText = searchText.toLowerCase();
                    assert noteTitle != null;
                    String lowercaseNoteTitle = noteTitle.toLowerCase();

                    if (lowercaseNoteTitle.startsWith(lowercaseSearchText)) {
                        filteredPlants.add(plant);
                    }
                }

                adapter.updateNotes(filteredPlants);
            });
        }
    }

    private void openEditPlant(String noteNumber, String noteId) {
        PlantDetailsFragment EditPlantFragment = new PlantDetailsFragment();
        Bundle args = new Bundle();
        args.putString("noteNumber", noteNumber);
        args.putString("noteId", noteId);
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

    private void loadNotesFromLocalStorage() {
        localNotes = appDatabase.noteDao().getAllNotes();
        localNotes.observe(getViewLifecycleOwner(), noteEntities -> {
            List<Plant> plants = convertToNoteList(noteEntities);
            adapter.updateNotes(plants);
        });

        List<Plant> plantsAux = convertToNoteList(localNotes.getValue());

        adapter = new NotesGridAdapter(requireContext(), plantsAux, appDatabase);
        adapter.setOnNoteClickListener(HomepageFragment.this);
        recyclerView.setAdapter(adapter);
    }

    private void createNewNoteInLocalStorage(String noteNumber, String noteTitle, String noteContent) {
        executor.execute(() -> {
            if (appDatabase.noteDao().getNoteByNumber(noteNumber) != null) {
                Log.d(TAG, "createNewNoteInLocalStorage: " + "Note already exists");
                return;
            }

            try {
                NoteEntity noteEntity = new NoteEntity();
                noteEntity.setNumber(noteNumber);
                noteEntity.setTitle(noteTitle);
                noteEntity.setContent(noteContent != null ? noteContent : "");
                appDatabase.noteDao().insert(noteEntity);

                if (!isNetworkConnected()) {
                    loadNotesFromLocalStorage();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private List<Plant> convertToNoteList(@Nullable List<NoteEntity> noteEntities) {
        if (noteEntities == null) {
            return new ArrayList<>();
        }

        List<Plant> plants = new ArrayList<>();

        for (NoteEntity noteEntity : noteEntities) {
            String noteId = String.valueOf(noteEntity.getId());

            Plant plant = new Plant(
                    noteId,
                    noteEntity.getNumber(),
                    noteEntity.getTitle(),
                    noteEntity.getContent()
            );
            plants.add(plant);
        }
        return plants;
    }

    private Plant convertToNote(NoteEntity noteEntity) {
        String noteId = String.valueOf(noteEntity.getId());

        Plant plant = new Plant(
                noteId,
                noteEntity.getNumber(),
                noteEntity.getTitle(),
                noteEntity.getContent()
        );

        return plant;
    }
}
