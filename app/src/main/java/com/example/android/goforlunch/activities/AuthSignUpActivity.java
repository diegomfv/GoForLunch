package com.example.android.goforlunch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */

public class AuthSignUpActivity extends AppCompatActivity {

    private static final String TAG = "AuthSignUpActivity";

    private FirebaseAuth auth;

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;

    private Button buttonSignIn;
    private Button buttonSignUp;
    private Button buttonResetPassword;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_signup);

        inputEmail = (TextInputEditText) findViewById(R.id.signup_email_layout_id);
        inputPassword = (TextInputEditText) findViewById(R.id.signup_password_layout_id);

        buttonResetPassword = (Button) findViewById(R.id.signup_reset_button_id);
        buttonSignUp = (Button) findViewById(R.id.signup_register_button_id);
        buttonSignIn = (Button) findViewById(R.id.signup_registered_button_id);

        progressBar = (ProgressBar) findViewById(R.id.signup_progressbar);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AuthSignUpActivity.this, AuthResetPassworkActivity.class));
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    Log.d(TAG, "onClick: no email ");
                    ToastHelper.toastShort(AuthSignUpActivity.this, "Please, enter email");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Log.d(TAG, "onClick: no password");
                    ToastHelper.toastShort(AuthSignUpActivity.this, "Please, enter password");
                    return;
                }

                if(password.length() < 6) {
                    Log.d(TAG, "onClick: password too short, only " + password.length() + " characters" );
                    ToastHelper.toastShort(AuthSignUpActivity.this, "Password is too short");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //we create the user
                auth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(AuthSignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.

                                if (!task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: task was NOT SUCCESSFUL");
                                    ToastHelper.toastShort(AuthSignUpActivity.this, "Something went wrong");
                                } else {
                                    startActivity(new Intent(AuthSignUpActivity.this, MainActivity.class));
                                }

                            }
                        });

            }
        });







    }
}
