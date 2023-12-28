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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NoteDetailsFragment extends Fragment {
    private Toolbar toolbar;
    private EditText noteContentEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase appDatabase;
    private LiveData<List<NoteEntity>> localNotes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_details_layout, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        noteContentEditText = view.findViewById(R.id.noteContentEditText);
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