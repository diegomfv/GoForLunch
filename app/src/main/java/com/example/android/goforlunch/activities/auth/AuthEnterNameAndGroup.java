package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class AuthEnterNameAndGroup extends AppCompatActivity{

    private static final String TAG = "AuthEnterNameAndGroup";

    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;

    private Button buttonStart;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_enter_name_and_group);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        inputFirstName = (TextInputEditText) findViewById(R.id.enter_first_name_id);
        inputLastName = (TextInputEditText) findViewById(R.id.enter_last_name_id);

        buttonStart = (Button) findViewById(R.id.enter_start_button_id);

        progressBar = (ProgressBar) findViewById(R.id.enter_progressbar);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (user != null) {

                    progressBar.setVisibility(View.VISIBLE);

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(inputFirstName.getText().toString() + " " + inputLastName.getText().toString())
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: task was NOT SUCCESSFUL");

                                        //We get the exception and display why it was not succesful
                                        FirebaseAuthException e = (FirebaseAuthException) task.getException();

                                        if (e != null) {
                                            Log.e(TAG, "onComplete: task NOT SUCCESSFUL: " + e.getMessage());
                                        }

                                        ToastHelper.toastShort(AuthEnterNameAndGroup.this, "Something went wrong. Please, sign up again");

                                    } else {
                                        Log.d(TAG, "onComplete: task was succesful");
                                        startActivity(new Intent(AuthEnterNameAndGroup.this, MainActivity.class));
                                    }
                                }
                            });

                } else {
                    ToastHelper.toastShort(AuthEnterNameAndGroup.this, "An error has occurred. Please, sign up again");
                    startActivity(new Intent(AuthEnterNameAndGroup.this, AuthSignUpActivity.class));
                }

            }
        });

    }
}
