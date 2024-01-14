package com.example.PlantsAndFriends;

import android.animation.AnimatorSet;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.animation.ObjectAnimator;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        ImageView imageView = findViewById(R.id.splash_image);
        TextView textView = findViewById(R.id.splash_text);

        float translationDistance = -400;

        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(imageView, View.TRANSLATION_Y, 0, translationDistance);
        ObjectAnimator textAnimator = ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, 0, translationDistance);

        imageAnimator.setDuration(1000);
        textAnimator.setDuration(1000);

        // Create alpha animators for fading in
        ObjectAnimator imageAlphaAnimator = ObjectAnimator.ofFloat(imageView, View.ALPHA, 1f, 0f);
        ObjectAnimator textAlphaAnimator = ObjectAnimator.ofFloat(textView, View.ALPHA, 1f, 0f);

        // Set duration for the alpha animation
        imageAlphaAnimator.setDuration(1000);
        textAlphaAnimator.setDuration(1000);

        // translation  and alpha animators
        AnimatorSet translationSet = new AnimatorSet();
        translationSet.playTogether(imageAnimator, textAnimator);

        AnimatorSet alphaSet = new AnimatorSet();
        alphaSet.playTogether(imageAlphaAnimator, textAlphaAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(translationSet, alphaSet);

        animatorSet.start();

        if (!isNetworkConnected()) {
            mainHandler.post(() -> Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show());
        }
        new Handler().postDelayed(() -> {
            setContentView(R.layout.activity_main);
            if (savedInstanceState == null) {
                checkUser();
            }
        }, 2000); // 2 seconds
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    private void loadLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, 0)
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void checkUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            openHomepage();
        } else {
            GoogleSignInOptions gso = new GoogleSignInOptions.
                    Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                    build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleSignInClient.silentSignIn().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    openHomepage();
                } else {
                    loadLoginFragment();
                }
            });
        }
    }

    private void openHomepage() {
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomepageFragment(), "HomepageFragment")
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
        return false;
    }

}