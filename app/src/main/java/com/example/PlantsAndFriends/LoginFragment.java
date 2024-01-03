package com.example.PlantsAndFriends;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private static final int REQ_ONE_TAP = 2;
    private FirebaseAuth auth;
    private EditText emailEditText, passwordEditText;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_plant, container, false);

        // Configure Google Sign In
        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
/*
        emailEditText = view.findViewById(R.id.login_email);
        passwordEditText = view.findViewById(R.id.login_password);

        Button loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> loginUser());

        TextView signUpButton = view.findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(v -> loadSignUpFragment());


        SignInButton googleSignInBtn = view.findViewById(R.id.googleBtn);
        googleSignInBtn.setOnClickListener(v -> googleSignIn());

        */

        return view;
    }

    private void googleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(requireActivity(), result -> {
                    try {
                        startIntentSenderForResult(
                                result.getPendingIntent().getIntentSender(),
                                REQ_ONE_TAP,
                                null,
                                0,
                                0,
                                0,
                                null);
                    } catch (IntentSender.SendIntentException e) {
                        Toast.makeText(requireActivity(), "Couldn't start One Tap UI", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                    }
                })
                .addOnFailureListener(requireActivity(), (OnFailureListener) e -> {
                    Log.d(TAG, "One Tap Client Failure" + e.getLocalizedMessage());
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                String email = credential.getId();
                String password = credential.getPassword();
                if (idToken != null) {
                    // Got an ID token from Google.
                    Toast.makeText(requireContext(), email + " signed in with success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Got ID token.");
                    openUserDashboard();
                } else if (password != null) {
                    Log.d(TAG, "Got password.");
                }
            } catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case CommonStatusCodes.CANCELED:
                        Log.d(TAG, "One-tap dialog was closed.");
                        break;
                    case CommonStatusCodes.NETWORK_ERROR:
                        Log.d(TAG, "One-tap encountered a network error.");
                        break;
                    default:
                        Log.d(TAG, "Couldn't get credential from result."
                                + e.getLocalizedMessage());
                        break;
                }
            }
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Warning: Email cannot be empty.");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Warning: Password cannot be empty.");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login successful, proceed to app's main functionality or user dashboard
                        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                        // Navigate to the user's dashboard or main activity here
                        openUserDashboard();
                    } else {
                        // Login failed, handle the error by displaying an error message
                        String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(requireContext(), "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSignUpFragment() {
        SignupFragment signUpFragment = new SignupFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, signUpFragment);
        transaction.commit();
    }

    private void openUserDashboard() {
        NotesRepoFragment notesRepoFragment = new NotesRepoFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, notesRepoFragment);
        transaction.commit();
    }

    /*@Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);

        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(this::handleSignInResult);
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            openUserDashboard();
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(requireContext(), "Signed Out", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        googleApiClient.stopAutoManage(requireActivity());
        googleApiClient.disconnect();
    }*/
}