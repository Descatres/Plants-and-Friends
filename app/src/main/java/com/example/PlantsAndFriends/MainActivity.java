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
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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

        new Handler().postDelayed(() -> {
            // This method will be executed once the timer is over
            // Start your app main activity
            setContentView(R.layout.activity_main);
            // Load the LoginFragment initially
            if (savedInstanceState == null) {
                checkUser();
            }
        }, 2000); // 2 seconds
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            //User is logged in already. You can proceed with your next screen
            openHomepage();
        } else {
            GoogleSignInOptions gso = new GoogleSignInOptions.
                    Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                    build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleSignInClient.silentSignIn().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // User is logged in with Google. You can proceed with your next screen
                    openHomepage();
                } else {
                    // User is not logged in. You can show the login fragment here
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
}