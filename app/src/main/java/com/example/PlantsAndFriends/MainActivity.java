package com.example.PlantsAndFriends;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private AppDatabase appDatabase;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getApplicationContext());
        setContentView(R.layout.activity_main);

        // Load the LoginFragment initially
        if (savedInstanceState == null) {
            checkUser();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.execute(() -> {
            if (appDatabase != null && isNetworkConnected()) {
                Log.d("onDestroy", "onCreateView: " + appDatabase.noteDao().getAllNotes());
                appDatabase.noteDao().deleteAllNotes();
            }
        });
    }

    public void onPause() {
        super.onPause();

        executor.execute(() -> {
            if (appDatabase != null && isNetworkConnected()) {
                Log.d("onPause", "onCreateView: " + appDatabase.noteDao().getAllNotes());
                appDatabase.noteDao().deleteAllNotes();
            }
        });
    }

    public void onStop() {
        super.onStop();

        executor.execute(() -> {
            if (appDatabase != null && isNetworkConnected()) {
                Log.d("onStop", "onCreateView: " + appDatabase.noteDao().getAllNotes());
                appDatabase.noteDao().deleteAllNotes();
            }
        });
    }

    private void loadLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void checkUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            //User is logged in already. You can proceed with your next screen
            openUserDashboard();
        } else {
            GoogleSignInOptions gso = new GoogleSignInOptions.
                    Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                    build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleSignInClient.silentSignIn().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // User is logged in with Google. You can proceed with your next screen
                    openUserDashboard();
                } else {
                    // User is not logged in. You can show the login fragment here
                    loadLoginFragment();
                }
            });
        }
    }

    private void openUserDashboard() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotesRepoFragment(), "NotesRepoFragment")
                .commit();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
            }
        }

        // If connectivity manager is null, assume there is no active network connection
        return false;
    }
}