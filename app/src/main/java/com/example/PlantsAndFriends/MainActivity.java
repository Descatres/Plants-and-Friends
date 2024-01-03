package com.example.PlantsAndFriends;

import android.animation.AnimatorSet;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private AppDatabase appDatabase;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        ImageView imageView = findViewById(R.id.splash_image);
        TextView textView = findViewById(R.id.splash_text);

        float translationDistance = -400;

        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(imageView, View.TRANSLATION_Y, 0, translationDistance);
        ObjectAnimator textAnimator = ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, 0, translationDistance);

        imageAnimator.setDuration(1500);
        textAnimator.setDuration(1500);

        // Create alpha animators for fading in
        ObjectAnimator imageAlphaAnimator = ObjectAnimator.ofFloat(imageView, View.ALPHA, 1f, 0f);
        ObjectAnimator textAlphaAnimator = ObjectAnimator.ofFloat(textView, View.ALPHA, 1f, 0f);

        // Set duration for the alpha animation
        imageAlphaAnimator.setDuration(1500);
        textAlphaAnimator.setDuration(1500);

        // translation and alpha animators
        AnimatorSet translationSet = new AnimatorSet();
        translationSet.playTogether(imageAnimator, textAnimator);

        AnimatorSet alphaSet = new AnimatorSet();
        alphaSet.playTogether(imageAlphaAnimator, textAlphaAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(translationSet, alphaSet);

        animatorSet.start();

        new Handler().postDelayed(() -> {
            checkUser();
        }, 2000); // 3 seconds delay
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
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, 0)
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