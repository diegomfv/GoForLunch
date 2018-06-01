package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Diego Fajardo on 31/05/2018.
 */
public class AuthChooseLoginActivity extends AppCompatActivity{

    private static final String TAG = "AuthChooseLoginActivity";

    //Google Sign In Request Code
    private static int RC_SIGN_IN = 101;

    private SignInButton buttonGoogle;
    private Button buttonFacebook;
    private Button buttonPassword;

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth auth;
    private FirebaseUser user;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(AuthChooseLoginActivity.this);
        editor = sharedPref.edit();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            //We delete Shared preferences info
            deleteSharedPreferencesInfo();

        } else {

            if (user.getDisplayName() != null){

                //go directly to MainActivity
                startActivity(new Intent(AuthChooseLoginActivity.this, MainActivity.class));
                finish();


            } else {

                //go to AuthEnterNameActivity
                startActivity(new Intent(AuthChooseLoginActivity.this, AuthEnterNameActivity.class));
                finish();

            }

        }

        /** We set the content view after checking if the user is logged in
         * */
        setContentView(R.layout.activity_auth_choose_login);

        buttonGoogle = findViewById(R.id.choose_google_sign_in_button);
        buttonFacebook = findViewById(R.id.choose_facebook_sign_in_button);
        buttonPassword = findViewById(R.id.choose_sign_in_password_button_id);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Google Button clicked!");

                signIn();

            }
        });


        buttonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Facebook Button clicked!");

                ToastHelper.toastShort(AuthChooseLoginActivity.this, "Facebook Button clicked");

            }
        });


        buttonPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Password Button clicked!");

                startActivity(new Intent(AuthChooseLoginActivity.this, AuthSignInPasswordActivity.class));
                finish();

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    public void signIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = auth.getCurrentUser();

                            if (user != null) {
                                Log.d(TAG, "onComplete: user != null");

                                if (user.getDisplayName() == null) {
                                    Log.d(TAG, "onComplete: user.getDisplayName() == null");
                                    /** If the user has not chosen yet a first name and last name we
                                     * launch AuthEnterNameActivity
                                     * */
                                    Intent intent = new Intent(AuthChooseLoginActivity.this, AuthEnterNameActivity.class);
                                    startActivity(intent);

                                } else {
                                    Log.d(TAG, "onComplete: user.getDisplayName() = " + user.getDisplayName());
                                    /** If the user has already set a first name and last name we can fill SharedPreferences with
                                     * the user's info and launch MainActivity
                                     * */
                                    fillSharedPreferencesWithFirstNameAndLastName();

                                    Intent intent = new Intent(AuthChooseLoginActivity.this, MainActivity.class);
                                    startActivity(intent);

                                }

                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            ToastHelper.toastShort(AuthChooseLoginActivity.this, "Authentication Failed");
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    /** Method that deletes all the sharedPreferences info
     * */
    public void deleteSharedPreferencesInfo() {
        Log.d(TAG, "deleteSharedPreferencesInfo: called!");

        if (sharedPref.getAll().size() > 0) {

            Map<String, ?> map = sharedPref.getAll();

            for (Map.Entry<String, ?> entry :
                    map.entrySet()) {

                editor.putString(entry.getKey(), "");
                editor.apply();

            }
        }
    }

    /** Method that fills SharedPreferences with user's info
     * */
    public void fillSharedPreferencesWithFirstNameAndLastName() {
        Log.d(TAG, "fillSharedPreferencesWithFirstNameAndLastName: called!");

        String names = user.getDisplayName();

        if (names != null) {
            String[] tokens = names.split(" ");

            sharedPref = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(AuthChooseLoginActivity.this);
            editor.putString(
                    RepoStrings.SharedPreferences.USER_FIRST_NAME,
                    Objects.requireNonNull(tokens[0]));
            editor.putString(
                    RepoStrings.SharedPreferences.USER_LAST_NAME,
                    Objects.requireNonNull(tokens[1]));
            editor.apply();

        }
    }

}
